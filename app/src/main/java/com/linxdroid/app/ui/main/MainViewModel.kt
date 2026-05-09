package com.linxdroid.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linxdroid.app.PRootManager
import com.linxdroid.app.model.AppState
import com.linxdroid.app.model.Distribution
import com.linxdroid.app.model.Distributions
import com.linxdroid.app.model.LineType
import com.linxdroid.app.model.TerminalLine
import com.linxdroid.app.terminal.TerminalSession
import com.linxdroid.app.utils.ArchDetector
import com.linxdroid.app.utils.DownloadManager
import com.linxdroid.app.utils.PreferencesManager
import com.linxdroid.app.utils.RootFSManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prootManager: PRootManager,
    private val rootFSManager: RootFSManager,
    private val downloadManager: DownloadManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow<AppState>(AppState.Welcome)
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val _terminalLines = MutableStateFlow<List<TerminalLine>>(emptyList())
    val terminalLines: StateFlow<List<TerminalLine>> = _terminalLines.asStateFlow()

    private val _installedSizeMb = MutableStateFlow(0L)
    val installedSizeMb: StateFlow<Long> = _installedSizeMb.asStateFlow()

    private var terminalSession: TerminalSession? = null

    init {
        viewModelScope.launch {
            val firstLaunch = preferencesManager.isFirstLaunch.first()
            val installedId = preferencesManager.installedDistro.first()
            _state.value = when {
                firstLaunch -> AppState.Welcome
                installedId != null -> {
                    val distro = Distributions.findById(installedId)
                    if (distro != null && rootFSManager.isInstalled()) {
                        refreshInstalledSize()
                        AppState.Ready(distro)
                    } else {
                        AppState.DistroSelection
                    }
                }
                else -> AppState.DistroSelection
            }
        }
    }

    fun onGetStarted() {
        viewModelScope.launch {
            preferencesManager.markFirstLaunchDone()
            _state.value = AppState.DistroSelection
        }
    }

    fun installDistribution(distribution: Distribution) {
        val arch = ArchDetector.getPRootArch()
        val url = distribution.urlForArch(arch) ?: run {
            _state.value = AppState.Error("No download URL for architecture: $arch")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.value = AppState.Downloading(distribution, 0f, 0L, 0L)
                Timber.i("Downloading ${distribution.name} from $url")

                val result = downloadManager.download(url) { progress, received, total ->
                    _state.value = AppState.Downloading(distribution, progress, received, total)
                }

                _state.value = AppState.Extracting(distribution, 0f)
                Timber.i("Extracting rootfs…")

                val success = rootFSManager.install(
                    inputStream = result.inputStream,
                    totalBytes  = result.totalBytes,
                    isXz        = result.isXz,
                    onProgress  = { progress ->
                        _state.value = AppState.Extracting(distribution, progress)
                    }
                )

                if (success) {
                    preferencesManager.setInstalledDistro(distribution.id)
                    refreshInstalledSize()
                    _state.value = AppState.Ready(distribution)
                    Timber.i("${distribution.name} installed successfully")
                } else {
                    _state.value = AppState.Error("Extraction failed for ${distribution.name}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to install ${distribution.name}")
                _state.value = AppState.Error("Download failed: ${e.message}", retryable = true)
            }
        }
    }

    fun startSession(distribution: Distribution) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                addSystemLine("[LinxDroid] Starting ${distribution.name} session…")
                val process = prootManager.startSession(distribution)
                terminalSession = TerminalSession(process).also { session ->
                    session.start()
                    viewModelScope.launch {
                        session.output.collect { line -> appendLine(line) }
                    }
                }
                _state.value = AppState.SessionRunning(distribution)
                addSystemLine("[LinxDroid] Session active. Type commands below.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to start session")
                _state.value = AppState.Error("Failed to start session: ${e.message}")
            }
        }
    }

    fun sendCommand(command: String) {
        terminalSession?.sendCommand(command) ?: Timber.w("No active session")
    }

    fun stopSession() {
        terminalSession?.stop()
        terminalSession = null
        prootManager.stopSession()
        val distro = (state.value as? AppState.SessionRunning)?.distro
            ?: Distributions.all.first()
        _state.value = AppState.Ready(distro)
        addSystemLine("[LinxDroid] Session stopped.")
    }

    fun clearTerminal() {
        _terminalLines.value = emptyList()
    }

    fun uninstallDistribution() {
        viewModelScope.launch {
            terminalSession?.stop()
            terminalSession = null
            prootManager.stopSession()
            rootFSManager.uninstall()
            preferencesManager.setInstalledDistro(null)
            _terminalLines.value = emptyList()
            _installedSizeMb.value = 0L
            _state.value = AppState.DistroSelection
        }
    }

    fun retryFromError() {
        _state.value = AppState.DistroSelection
    }

    fun saveVncSettings(port: Int, display: Int) {
        viewModelScope.launch {
            preferencesManager.setVncPort(port)
            preferencesManager.setVncDisplay(display)
        }
    }

    fun saveCustomArgs(args: String) {
        viewModelScope.launch { preferencesManager.setCustomArgs(args) }
    }

    private fun refreshInstalledSize() {
        _installedSizeMb.value = rootFSManager.getInstalledSizeMb()
    }

    private fun appendLine(line: TerminalLine) {
        _terminalLines.update { current ->
            val updated = current + line
            if (updated.size > 2000) updated.takeLast(2000) else updated
        }
    }

    private fun addSystemLine(text: String) {
        appendLine(TerminalLine(text, LineType.SYSTEM))
    }

    override fun onCleared() {
        super.onCleared()
        terminalSession?.stop()
        prootManager.stopSession()
    }
}
