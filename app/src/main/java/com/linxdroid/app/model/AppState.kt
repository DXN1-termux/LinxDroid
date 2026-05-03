package com.linxdroid.app.model

sealed class AppState {
    object Welcome : AppState()
    object DistroSelection : AppState()
    data class Downloading(val distro: Distribution, val progress: Float, val bytesReceived: Long, val totalBytes: Long) : AppState()
    data class Extracting(val distro: Distribution, val progress: Float) : AppState()
    data class Ready(val distro: Distribution) : AppState()
    data class SessionRunning(val distro: Distribution) : AppState()
    data class Error(val message: String, val retryable: Boolean = true) : AppState()
}
