package com.linxdroid.app.terminal

import com.linxdroid.app.model.LineType
import com.linxdroid.app.model.TerminalLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter

class TerminalSession(private val process: Process) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _output = MutableSharedFlow<TerminalLine>(extraBufferCapacity = 512)
    val output: SharedFlow<TerminalLine> = _output

    private val writer = PrintWriter(OutputStreamWriter(process.outputStream), true)
    private var stdoutJob: Job? = null
    private var stderrJob: Job? = null

    fun start() {
        stdoutJob = scope.launch {
            try {
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        _output.emit(TerminalLine(line!!, LineType.OUTPUT))
                    }
                }
            } catch (e: Exception) {
                Timber.d(e, "stdout reader closed")
            }
            _output.emit(TerminalLine("[Session ended]", LineType.SYSTEM))
        }

        stderrJob = scope.launch {
            try {
                BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        _output.emit(TerminalLine(line!!, LineType.ERROR))
                    }
                }
            } catch (e: Exception) {
                Timber.d(e, "stderr reader closed")
            }
        }

        Timber.i("TerminalSession started")
    }

    fun sendCommand(command: String) {
        scope.launch {
            try {
                writer.println(command)
                _output.emit(TerminalLine("$ $command", LineType.INPUT))
            } catch (e: Exception) {
                Timber.e(e, "Failed to send command: $command")
            }
        }
    }

    fun isAlive(): Boolean = process.isAlive

    fun stop() {
        stdoutJob?.cancel()
        stderrJob?.cancel()
        try { writer.close() } catch (_: Exception) {}
        process.destroy()
        Timber.i("TerminalSession stopped")
    }
}
