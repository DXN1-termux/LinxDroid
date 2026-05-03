package com.linxdroid.app.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class RootFSManager(private val context: Context) {

    private val rootfsDir: File get() = File(context.filesDir, "linux_rootfs")

    fun isInstalled(): Boolean =
        rootfsDir.exists() && rootfsDir.list()?.isNotEmpty() == true

    fun getRootfsPath(): String = rootfsDir.absolutePath

    suspend fun install(
        inputStream: InputStream,
        totalBytes: Long,
        isXz: Boolean = false,
        onProgress: (Float) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            rootfsDir.deleteRecursively()
            rootfsDir.mkdirs()

            val bufferedInput = inputStream.buffered()
            val decompressed = if (isXz) {
                XZCompressorInputStream(bufferedInput)
            } else {
                GzipCompressorInputStream(bufferedInput)
            }

            val tarInput = TarArchiveInputStream(decompressed)
            var bytesRead = 0L
            var entry = tarInput.nextTarEntry

            while (entry != null) {
                val destFile = File(rootfsDir, entry.name)

                if (!destFile.canonicalPath.startsWith(rootfsDir.canonicalPath)) {
                    Timber.w("Skipping potentially unsafe entry: ${entry.name}")
                    entry = tarInput.nextTarEntry
                    continue
                }

                if (entry.isDirectory) {
                    destFile.mkdirs()
                } else {
                    destFile.parentFile?.mkdirs()
                    FileOutputStream(destFile).use { out ->
                        val buffer = ByteArray(8192)
                        var n: Int
                        while (tarInput.read(buffer).also { n = it } != -1) {
                            out.write(buffer, 0, n)
                            bytesRead += n
                            if (totalBytes > 0) {
                                onProgress((bytesRead.toFloat() / totalBytes).coerceIn(0f, 1f))
                            }
                        }
                    }

                    val mode = entry.mode
                    if (mode and 0b001_000_000 != 0 || mode and 0b000_001_000 != 0 || mode and 0b000_000_001 != 0) {
                        destFile.setExecutable(true, false)
                    }
                }
                entry = tarInput.nextTarEntry
            }

            tarInput.close()
            onProgress(1f)
            Timber.i("RootFS installation complete at ${rootfsDir.absolutePath}")
            true
        } catch (e: Exception) {
            Timber.e(e, "RootFS extraction failed")
            rootfsDir.deleteRecursively()
            false
        }
    }

    fun uninstall() {
        rootfsDir.deleteRecursively()
        Timber.i("RootFS uninstalled")
    }

    fun getInstalledSizeMb(): Long {
        if (!rootfsDir.exists()) return 0L
        return rootfsDir.walkTopDown().filter { it.isFile }.sumOf { it.length() } / (1024 * 1024)
    }

    fun setupEtcResolv() {
        val resolvConf = File(rootfsDir, "etc/resolv.conf")
        if (!resolvConf.exists()) {
            resolvConf.parentFile?.mkdirs()
            resolvConf.writeText("nameserver 8.8.8.8\nnameserver 1.1.1.1\n")
        }
    }
}
