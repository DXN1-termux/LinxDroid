package com.linxdroid.app

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.linxdroid.app.utils.RootFSManager

class VNCService : Service() {
    private val CHANNEL_ID = "LinxDroidService"
    private lateinit var prootManager: PRootManager

    override fun onCreate() {
        super.onCreate()
        prootManager = PRootManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("LinxDroid Running")
            .setContentText("Full Linux environment is active.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        startForeground(1, notification)
        
        Thread {
            prootManager.startLinux()
        }.start()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID, "LinxDroid Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }
}
