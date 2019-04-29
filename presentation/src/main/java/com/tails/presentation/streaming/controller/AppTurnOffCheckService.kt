package com.tails.presentation.streaming.controller

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.tails.presentation.streaming.notification.MusicControlNotification
import com.tails.presentation.streaming.receiver.MusicControlReceiver

class AppTurnOffCheckService: Service(){

    private val broadcastReceiver = MusicControlReceiver()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        initNotificationActionBroadcastReceiver()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(broadcastReceiver)
        MusicControlNotification.removeNotification(applicationContext)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        MusicControlNotification.removeNotification(applicationContext)
        this.unregisterReceiver(broadcastReceiver)
    }

    private fun initNotificationActionBroadcastReceiver() {
        val pauseIntentFilter = IntentFilter("kkori.pause")
        val previousIntentFilter = IntentFilter("kkori.previous")
        val nextIntentFilter = IntentFilter("kkori.next")

        this.registerReceiver(broadcastReceiver, previousIntentFilter)
        this.registerReceiver(broadcastReceiver, pauseIntentFilter)
        this.registerReceiver(broadcastReceiver, nextIntentFilter)
    }
}