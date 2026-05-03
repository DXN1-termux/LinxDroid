package com.linxdroid.app.utils

import android.content.Context
import android.util.Log
import java.io.*
import java.util.zip.GZIPInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream

class RootFSManager(private val context: Context) {
    private val rootfsDir = File(context.filesDir, "linux_rootfs")

    fun isInstalled(): Boolean = rootfsDir.exists() && rootfsDir.list()?.isNotEmpty() == true

    fun install(assetName: String, callback: (Double) -> Unit): Boolean {
        try {
            rootfsDir.mkdirs()
            val inputStream = context.assets.open(assetName)
            val gzipIn = GZIPInputStream(inputStream)
            val tarIn = TarArchiveInputStream(gzipIn)

            var entry = tarIn.nextTarEntry
            var extractedSize = 0L
            // Note: In a real implementation, you'd calculate total size for progress
            
            while (entry != null) {
                val destFile = File(rootfsDir, entry.name)
                if (entry.isDirectory) {
                    destFile.mkdirs()
                } else {
                    destFile.parentFile?.mkdirs()
                    FileOutputStream(destFile).use { output ->
                        tarIn.copyTo(output)
                    }
                    if (entry.mode != 0) {
                        destFile.setExecutable((entry.mode and 0b100) != 0)
                    }
                }
                entry = tarIn.nextTarEntry
                // callback(progress)
            }
            return true
        } catch (e: Exception) {
            Log.e("RootFSManager", "Extraction failed", e)
            return false
        }
    }

    fun getRootfsPath(): String = rootfsDir.absolutePath
}
