package com.linxdroid.app

import android.content.Context
import android.util.Log

class PRootManager(private val context: Context) {
    fun startLinux() {
        val prootPath = context.applicationInfo.nativeLibraryDir + "/libproot.so"
        Log.d("LinxDroid", "Starting Linux with PRoot at: $prootPath")
        // Logic to execute via ProcessBuilder goes here
    }
}
