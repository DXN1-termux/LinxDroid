package com.linxdroid.app.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.InputStream
import java.util.concurrent.TimeUnit

data class DownloadResult(
    val inputStream: InputStream,
    val totalBytes: Long,
    val isXz: Boolean
)

class DownloadManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun download(
        url: String,
        onProgress: (Float, Long, Long) -> Unit
    ): DownloadResult = withContext(Dispatchers.IO) {
        Timber.d("Downloading: $url")

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: ${response.message}")
        }

        val body = response.body ?: throw Exception("Empty response body")
        val totalBytes = body.contentLength()
        val isXz = url.endsWith(".xz", ignoreCase = true)

        val rawStream = body.byteStream()
        val progressStream = ProgressInputStream(rawStream, totalBytes) { received, total ->
            val progress = if (total > 0) received.toFloat() / total else 0f
            onProgress(progress, received, total)
        }

        DownloadResult(progressStream, totalBytes, isXz)
    }
}

private class ProgressInputStream(
    private val wrapped: InputStream,
    private val total: Long,
    private val onProgress: (Long, Long) -> Unit
) : InputStream() {

    private var bytesRead = 0L

    override fun read(): Int {
        val b = wrapped.read()
        if (b != -1) {
            bytesRead++
            onProgress(bytesRead, total)
        }
        return b
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val n = wrapped.read(b, off, len)
        if (n > 0) {
            bytesRead += n
            onProgress(bytesRead, total)
        }
        return n
    }

    override fun close() = wrapped.close()
}
