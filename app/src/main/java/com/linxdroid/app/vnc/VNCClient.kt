package com.linxdroid.app.vnc

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

enum class VNCConnectionState { DISCONNECTED, CONNECTING, HANDSHAKING, CONNECTED, ERROR }

data class VNCState(
    val connectionState: VNCConnectionState = VNCConnectionState.DISCONNECTED,
    val width: Int  = 0,
    val height: Int = 0,
    val errorMessage: String? = null
)

class VNCClient(
    private val host: String = "127.0.0.1",
    private val port: Int    = 5900,
    private val password: String = ""
) {
    private val _state = MutableStateFlow(VNCState())
    val state: StateFlow<VNCState> = _state

    private var socket: Socket? = null
    private var input: DataInputStream? = null
    private var output: DataOutputStream? = null
    private var framebuffer: Bitmap? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var serverWidth  = 0
    private var serverHeight = 0

    fun connect() {
        scope.launch {
            try {
                _state.value = VNCState(VNCConnectionState.CONNECTING)
                Timber.d("Connecting to VNC at $host:$port")

                socket = Socket(host, port)
                input  = DataInputStream(socket!!.getInputStream())
                output = DataOutputStream(socket!!.getOutputStream())

                _state.value = VNCState(VNCConnectionState.HANDSHAKING)
                performHandshake()
                readServerInit()

                _state.value = VNCState(
                    connectionState = VNCConnectionState.CONNECTED,
                    width  = serverWidth,
                    height = serverHeight
                )

                framebuffer = Bitmap.createBitmap(serverWidth, serverHeight, Bitmap.Config.ARGB_8888)
                requestFullUpdate()
                messageLoop()

            } catch (e: Exception) {
                Timber.e(e, "VNC connection failed")
                _state.value = VNCState(
                    connectionState = VNCConnectionState.ERROR,
                    errorMessage    = e.message
                )
                disconnect()
            }
        }
    }

    fun disconnect() {
        scope.launch {
            try {
                input?.close()
                output?.close()
                socket?.close()
            } catch (_: Exception) {}
            socket = null
            input  = null
            output = null
            _state.value = VNCState(VNCConnectionState.DISCONNECTED)
            Timber.i("VNC disconnected")
        }
    }

    fun sendPointerEvent(x: Int, y: Int, buttonMask: Int) {
        scope.launch {
            try {
                val out = output ?: return@launch
                out.writeByte(5)         // PointerEvent
                out.writeByte(buttonMask)
                out.writeShort(x)
                out.writeShort(y)
                out.flush()
            } catch (e: Exception) {
                Timber.w(e, "Failed to send pointer event")
            }
        }
    }

    fun sendKeyEvent(key: Int, down: Boolean) {
        scope.launch {
            try {
                val out = output ?: return@launch
                out.writeByte(4)         // KeyEvent
                out.writeByte(if (down) 1 else 0)
                out.writeShort(0)        // padding
                out.writeInt(key)
                out.flush()
            } catch (e: Exception) {
                Timber.w(e, "Failed to send key event")
            }
        }
    }

    fun getFramebuffer(): Bitmap? = framebuffer

    private fun performHandshake() {
        val inp = input!!
        val out = output!!

        val serverVersion = ByteArray(12)
        inp.readFully(serverVersion)
        val versionStr = String(serverVersion)
        Timber.d("VNC server version: $versionStr")

        out.write("RFB 003.008\n".toByteArray())
        out.flush()

        val numSecTypes = inp.readByte().toInt() and 0xFF
        if (numSecTypes == 0) {
            val reasonLen = inp.readInt()
            val reason = ByteArray(reasonLen)
            inp.readFully(reason)
            throw Exception("VNC server refused: ${String(reason)}")
        }

        val secTypes = ByteArray(numSecTypes)
        inp.readFully(secTypes)

        val useNoneAuth = secTypes.contains(1.toByte())
        if (useNoneAuth) {
            out.writeByte(1)
        } else {
            out.writeByte(secTypes[0].toInt())
        }
        out.flush()

        val secResult = inp.readInt()
        if (secResult != 0) {
            val reasonLen = inp.readInt()
            val reason = ByteArray(reasonLen)
            inp.readFully(reason)
            throw Exception("Authentication failed: ${String(reason)}")
        }

        out.writeByte(1)
        out.flush()
        Timber.i("VNC handshake successful")
    }

    private fun readServerInit() {
        val inp = input!!
        serverWidth  = inp.readShort().toInt() and 0xFFFF
        serverHeight = inp.readShort().toInt() and 0xFFFF

        inp.skip(16)

        val nameLen = inp.readInt()
        val name = ByteArray(nameLen)
        inp.readFully(name)
        Timber.i("VNC server: ${String(name)} (${serverWidth}x${serverHeight})")
    }

    private fun requestFullUpdate() {
        val out = output ?: return
        out.writeByte(3)  // FramebufferUpdateRequest
        out.writeByte(0)  // incremental = false
        out.writeShort(0)
        out.writeShort(0)
        out.writeShort(serverWidth)
        out.writeShort(serverHeight)
        out.flush()
    }

    private fun requestIncrementalUpdate() {
        val out = output ?: return
        out.writeByte(3)
        out.writeByte(1)  // incremental = true
        out.writeShort(0)
        out.writeShort(0)
        out.writeShort(serverWidth)
        out.writeShort(serverHeight)
        out.flush()
    }

    private fun messageLoop() {
        val inp = input ?: return
        while (socket?.isConnected == true) {
            try {
                when (inp.readByte().toInt() and 0xFF) {
                    0 -> handleFramebufferUpdate()
                    2 -> { inp.skip(3); inp.readInt() }  // SetColourMapEntries (skip)
                    3 -> {}  // Bell (ignore)
                    4 -> {  // ServerCutText
                        inp.skip(3)
                        val len = inp.readInt()
                        inp.skip(len.toLong())
                    }
                }
            } catch (e: Exception) {
                if (socket?.isConnected == true) {
                    Timber.e(e, "VNC message loop error")
                }
                break
            }
        }
    }

    private fun handleFramebufferUpdate() {
        val inp = input ?: return
        inp.skip(1)  // padding
        val numRects = inp.readShort().toInt() and 0xFFFF

        repeat(numRects) {
            val x = inp.readShort().toInt() and 0xFFFF
            val y = inp.readShort().toInt() and 0xFFFF
            val w = inp.readShort().toInt() and 0xFFFF
            val h = inp.readShort().toInt() and 0xFFFF
            val encoding = inp.readInt()

            when (encoding) {
                0 -> decodeRaw(x, y, w, h)
                2 -> decodeRRE(x, y, w, h)
                else -> {
                    Timber.w("Unsupported VNC encoding: $encoding — skipping")
                }
            }
        }

        requestIncrementalUpdate()
    }

    private fun decodeRaw(x: Int, y: Int, w: Int, h: Int) {
        val inp = input ?: return
        val fb  = framebuffer ?: return
        val pixels = IntArray(w * h)
        for (i in pixels.indices) {
            val r = inp.readByte().toInt() and 0xFF
            val g = inp.readByte().toInt() and 0xFF
            val b = inp.readByte().toInt() and 0xFF
            inp.skip(1)
            pixels[i] = Color.rgb(r, g, b)
        }
        fb.setPixels(pixels, 0, w, x, y, w, h)
    }

    private fun decodeRRE(x: Int, y: Int, w: Int, h: Int) {
        val inp = input ?: return
        val fb  = framebuffer ?: return
        val numSubRects = inp.readInt()
        val bgR = inp.readByte().toInt() and 0xFF
        val bgG = inp.readByte().toInt() and 0xFF
        val bgB = inp.readByte().toInt() and 0xFF
        inp.skip(1)
        val bgColor = Color.rgb(bgR, bgG, bgB)

        val canvas = Canvas(fb)
        val paint = Paint()
        paint.color = bgColor
        canvas.drawRect(x.toFloat(), y.toFloat(), (x + w).toFloat(), (y + h).toFloat(), paint)

        repeat(numSubRects) {
            val r = inp.readByte().toInt() and 0xFF
            val g = inp.readByte().toInt() and 0xFF
            val b = inp.readByte().toInt() and 0xFF
            inp.skip(1)
            val sx = x + (inp.readShort().toInt() and 0xFFFF)
            val sy = y + (inp.readShort().toInt() and 0xFFFF)
            val sw = inp.readShort().toInt() and 0xFFFF
            val sh = inp.readShort().toInt() and 0xFFFF

            paint.color = Color.rgb(r, g, b)
            canvas.drawRect(sx.toFloat(), sy.toFloat(), (sx + sw).toFloat(), (sy + sh).toFloat(), paint)
        }
    }
}
