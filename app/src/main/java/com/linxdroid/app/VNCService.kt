package com.linxdroid.app

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.linxdroid.app.model.Distributions
import timber.log.Timber
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VNCService : Service() {

    companion object {
        const val ACTION_START = "com.linxdroid.app.ACTION_START"
        const val ACTION_STOP  = "com.linxdroid.app.ACTION_STOP"
        const val EXTRA_DISTRO_ID = "distro_id"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "linxdroid_service"
    }

    @Inject lateinit var prootManager: PRootManager

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
        Timber.i("VNCService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                val distroId = intent?.getStringExtra(EXTRA_DISTRO_ID) ?: "alpine"
                startForeground(NOTIFICATION_ID, buildNotification(distroId))
                Timber.i("VNCService started for distro: $distroId")
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        prootManager.stopSession()
        releaseWakeLock()
        Timber.i("VNCService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(distroId: String): Notification {
        val distroName = Distributions.findById(distroId)?.name ?: distroId

        val stopPendingIntent = PendingIntent.getService(
            this, 0,
            Intent(this, VNCService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("LinxDroid — $distroName Running")
            .setContentText("Linux environment is active. Tap to open.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_delete, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "LinxDroid Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows when a Linux session is running"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "LinxDroid:SessionWakeLock"
        ).apply { acquire(24 * 60 * 60 * 1000L) }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }
}
