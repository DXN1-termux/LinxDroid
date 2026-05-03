package com.linxdroid.app.vnc

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlinx.coroutines.*
import timber.log.Timber

class VNCView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var client: VNCClient? = null
    private var renderJob: Job? = null

    private var scaleFactor = 1f
    private var translateX  = 0f
    private var translateY  = 0f
    private val matrix = Matrix()

    private val paint = Paint().apply { isAntiAlias = false; isFilterBitmap = true }
    private val bgPaint = Paint().apply { color = Color.BLACK }

    private val scaleDetector = ScaleGestureDetector(context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleFactor = (scaleFactor * detector.scaleFactor).coerceIn(0.5f, 5f)
                rebuildMatrix()
                return true
            }
        }
    )

    private val gestureDetector = GestureDetector(context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float): Boolean {
                translateX -= dx
                translateY -= dy
                rebuildMatrix()
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                sendPointer(e, 1)
                postDelayed({ sendPointer(e, 0) }, 50)
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                sendPointer(e, 4)
                postDelayed({ sendPointer(e, 0) }, 50)
            }
        }
    )

    init {
        holder.addCallback(this)
        setBackgroundColor(Color.BLACK)
    }

    fun connect(host: String = "127.0.0.1", port: Int = 5900) {
        client?.disconnect()
        client = VNCClient(host, port).also { c ->
            c.connect()
            scope.launch {
                c.state.collect { state ->
                    Timber.d("VNC state: ${state.connectionState}")
                }
            }
        }
        startRenderLoop()
    }

    fun disconnect() {
        renderJob?.cancel()
        client?.disconnect()
        client = null
    }

    private fun startRenderLoop() {
        renderJob?.cancel()
        renderJob = scope.launch {
            while (isActive) {
                render()
                delay(33)
            }
        }
    }

    private fun render() {
        val sfh = holder ?: return
        val fb  = client?.getFramebuffer() ?: return
        var canvas: Canvas? = null
        try {
            canvas = sfh.lockCanvas()
            canvas?.let { c ->
                c.drawPaint(bgPaint)
                c.drawBitmap(fb, matrix, paint)
            }
        } finally {
            canvas?.let { sfh.unlockCanvasAndPost(it) }
        }
    }

    private fun rebuildMatrix() {
        matrix.reset()
        matrix.setScale(scaleFactor, scaleFactor)
        matrix.postTranslate(translateX, translateY)
        invalidate()
    }

    private fun sendPointer(e: MotionEvent, buttonMask: Int) {
        val invertedMatrix = Matrix()
        matrix.invert(invertedMatrix)
        val pts = floatArrayOf(e.x, e.y)
        invertedMatrix.mapPoints(pts)
        client?.sendPointerEvent(pts[0].toInt(), pts[1].toInt(), buttonMask)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Timber.d("VNCView surface created")
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        scaleFactor = 1f
        translateX  = 0f
        translateY  = 0f
        rebuildMatrix()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        renderJob?.cancel()
        Timber.d("VNCView surface destroyed")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scope.cancel()
        disconnect()
    }
}
