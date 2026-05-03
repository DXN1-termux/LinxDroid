package com.linxdroid.app.ui.main

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.linxdroid.app.model.AppState
import com.linxdroid.app.ui.screens.*

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state          by viewModel.state.collectAsStateWithLifecycle()
    val terminalLines  by viewModel.terminalLines.collectAsStateWithLifecycle()
    val installedSize  by viewModel.installedSizeMb.collectAsStateWithLifecycle()

    var showSettings by remember { mutableStateOf(false) }

    val currentDistro = when (val s = state) {
        is AppState.Ready          -> s.distro
        is AppState.SessionRunning -> s.distro
        else -> null
    }

    if (showSettings) {
        SettingsScreen(
            distribution    = currentDistro,
            installedSizeMb = installedSize,
            vncPort         = 5900,
            vncDisplay      = 0,
            customArgs      = "",
            onBack          = { showSettings = false },
            onUninstall     = {
                showSettings = false
                viewModel.uninstallDistribution()
            },
            onSaveVnc        = { port, display -> viewModel.saveVncSettings(port, display) },
            onSaveCustomArgs = { args -> viewModel.saveCustomArgs(args) }
        )
        return
    }

    AnimatedContent(
        targetState  = state,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
        label        = "screenTransition"
    ) { currentState ->
        when (currentState) {
            is AppState.Welcome -> {
                WelcomeScreen(onGetStarted = { viewModel.onGetStarted() })
            }
            is AppState.DistroSelection -> {
                DistroSelectionScreen(onInstall = { viewModel.installDistribution(it) })
            }
            is AppState.Downloading,
            is AppState.Extracting -> {
                ProgressScreen(state = currentState)
            }
            is AppState.Ready -> {
                ReadyScreen(
                    distribution    = currentState.distro,
                    installedSizeMb = installedSize,
                    onStartSession  = { viewModel.startSession(currentState.distro) },
                    onSettings      = { showSettings = true }
                )
            }
            is AppState.SessionRunning -> {
                TerminalScreen(
                    distribution  = currentState.distro,
                    lines         = terminalLines,
                    onCommand     = { viewModel.sendCommand(it) },
                    onStopSession = { viewModel.stopSession() },
                    onClear       = { viewModel.clearTerminal() }
                )
            }
            is AppState.Error -> {
                ErrorScreen(
                    message   = currentState.message,
                    retryable = currentState.retryable,
                    onRetry   = { viewModel.retryFromError() }
                )
            }
        }
    }
}
