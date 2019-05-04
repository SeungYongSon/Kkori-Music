package com.tails.presentation.streaming.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import com.tails.presentation.streaming.controller.MusicStreamingController
import com.tails.presentation.streaming.notification.MusicControlNotification


class RealTimeOperationService : Service() {

    private val seekHandler = Handler()

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

        seekHandler.post(object : Runnable {
            override fun run() {
                if (MusicStreamingController.isPlaying) {
                    MusicStreamingController.controlRequest("seekUpdate")
                }
                seekHandler.postDelayed(this, 1000)
            }
        })

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        seekHandler.removeCallbacks(null)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        seekHandler.removeCallbacks(null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) stopForeground(true)
        else with(NotificationManagerCompat.from(applicationContext)) { cancel(20011203) }

        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}