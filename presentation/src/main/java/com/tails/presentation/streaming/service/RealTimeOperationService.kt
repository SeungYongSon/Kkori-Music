package com.tails.presentation.streaming.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import com.tails.presentation.streaming.notification.MusicControlNotification

class RealTimeOperationService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (MusicControlNotification.notification != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForeground(20011203, MusicControlNotification.notification)
            } else {
                with(NotificationManagerCompat.from(applicationContext)) {
                    notify(20011203, MusicControlNotification.notification!!)
                }
            }
        }

        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) stopForeground(true)
        else with(NotificationManagerCompat.from(applicationContext)) { cancel(20011203) }

        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}