package com.linxdroid.app

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class PRootManager(private val context: Context) {

    fun startLinux() {
        val prootBinary = extractBinary()
        val rootfsDir = File(context.filesDir, "linux_rootfs")
        
        if (!rootfsDir.exists()) {
            rootfsDir.mkdirs()
            // In a real app, you'd extract the tarball here.
            Log.d("LinxDroid", "RootFS needs extraction at: ${rootfsDir.absolutePath}")
        }

        val command = mutableListOf(
            prootBinary.absolutePath,
            "-r", rootfsDir.absolutePath,
            "-0",
            "-w", "/",
            "-b", "/dev",
            "-b", "/proc",
            "-b", "/sys",
            "/bin/sh", "-c", "echo 'Hello from full Linux!'"
        )

        val pb = ProcessBuilder(command)
        pb.environment().put("PROOT_TMP_DIR", context.cacheDir.absolutePath)
        
        try {
            val process = pb.start()
            process.inputStream.bufferedReader().use { reader ->
                reader.forEachLine { Log.d("LinxDroid-Linux", it) }
            }
        } catch (e: Exception) {
            Log.e("LinxDroid", "Failed to start Linux: ${e.message}")
        }
    }

    private fun extractBinary(): File {
        val binaryFile = File(context.filesDir, "proot")
        if (!binaryFile.exists()) {
            // This assumes you add the binary to assets/
            context.assets.open("proot").use { input ->
                FileOutputStream(binaryFile).use { output ->
                    input.copyTo(output)
                }
            }
            binaryFile.setExecutable(true)
        }
        return binaryFile
    }
}
