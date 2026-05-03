package com.linxdroid.app

import android.content.Context
import com.linxdroid.app.model.Distribution
import com.linxdroid.app.utils.ArchDetector
import com.linxdroid.app.utils.RootFSManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PRootManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val rootFSManager: RootFSManager
) {
    private val arch = ArchDetector.getPRootArch()
    private var process: Process? = null

    fun isSessionRunning(): Boolean = process?.isAlive == true

    fun buildCommand(distribution: Distribution, extraArgs: List<String> = emptyList()): List<String> {
        val prootBinary = extractBinary()
        val rootfsPath = rootFSManager.getRootfsPath()

        val command = mutableListOf(
            prootBinary.absolutePath,
            "--kill-on-exit",
            "-r", rootfsPath,
            "-0",
            "-w", "/root",
            "-b", "/dev",
            "-b", "/proc",
            "-b", "/sys",
            "-b", "/dev/urandom:/dev/random"
        )

        if (File("/proc/net").exists()) command.addAll(listOf("-b", "/proc/net"))
        if (File("/sys/fs").exists()) command.addAll(listOf("-b", "/sys/fs/cgroup:/sys/fs/cgroup"))

        command.addAll(extraArgs)

        command.addAll(listOf(
            "/usr/bin/env", "-i",
            "HOME=/root",
            "TERM=xterm-256color",
            "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
            "LANG=C.UTF-8",
            distribution.defaultShell, "--login"
        ))

        Timber.d("PRoot command: ${command.joinToString(" ")}")
        return command
    }

    fun startSession(distribution: Distribution, extraArgs: List<String> = emptyList()): Process {
        stopSession()
        rootFSManager.setupEtcResolv()

        val command = buildCommand(distribution, extraArgs)
        val pb = ProcessBuilder(command).apply {
            environment()["PROOT_TMP_DIR"] = context.cacheDir.absolutePath
            environment()["PROOT_LOADER"] = extractLoader().absolutePath
            redirectErrorStream(false)
        }

        process = pb.start()
        Timber.i("PRoot session started (PID available via process)")
        return process!!
    }

    fun stopSession() {
        process?.let {
            try {
                it.outputStream.close()
            } catch (_: Exception) {}
            it.destroy()
            Timber.i("PRoot session destroyed")
        }
        process = null
    }

    private fun extractBinary(): File {
        val binaryName = "proot-$arch"
        val binaryFile = File(context.filesDir, "proot")
        val versionFile = File(context.filesDir, "proot.version")

        val expectedVersion = "proot-$arch-v1"
        if (binaryFile.exists() && versionFile.exists() && versionFile.readText() == expectedVersion) {
            return binaryFile
        }

        try {
            context.assets.open(binaryName).use { input ->
                FileOutputStream(binaryFile).use { output -> input.copyTo(output) }
            }
        } catch (e: Exception) {
            Timber.w("Arch-specific binary '$binaryName' not found, trying generic 'proot'")
            context.assets.open("proot").use { input ->
                FileOutputStream(binaryFile).use { output -> input.copyTo(output) }
            }
        }

        binaryFile.setExecutable(true, false)
        versionFile.writeText(expectedVersion)
        Timber.i("Extracted proot binary: $binaryName -> ${binaryFile.absolutePath}")
        return binaryFile
    }

    private fun extractLoader(): File {
        val loaderName = "proot-loader-$arch"
        val loaderFile = File(context.filesDir, "proot-loader")

        if (loaderFile.exists()) return loaderFile

        try {
            context.assets.open(loaderName).use { input ->
                FileOutputStream(loaderFile).use { output -> input.copyTo(output) }
            }
            loaderFile.setExecutable(true, false)
        } catch (e: Exception) {
            Timber.w("PRoot loader not found in assets: $e")
        }
        return loaderFile
    }
}
