package com.linxdroid.app.utils

import android.os.Build
import timber.log.Timber

object ArchDetector {

    fun getPRootArch(): String {
        val abis = Build.SUPPORTED_ABIS
        Timber.d("Supported ABIs: ${abis.joinToString()}")
        return when {
            abis.contains("arm64-v8a")  -> "aarch64"
            abis.contains("x86_64")     -> "x86_64"
            abis.contains("armeabi-v7a") -> "armhf"
            abis.contains("x86")        -> "x86"
            else -> {
                Timber.w("Unknown ABI list, defaulting to aarch64")
                "aarch64"
            }
        }
    }

    fun getPrimaryAbi(): String = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
}
