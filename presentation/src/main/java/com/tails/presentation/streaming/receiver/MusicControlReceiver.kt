package com.tails.presentation.streaming.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tails.presentation.streaming.controller.MusicStreamingController

class MusicControlReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent!!.action) {
            "kkori.pause" -> {
                if (MusicStreamingController.isPlaying) {
                    MusicStreamingController.controlRequest("pause")
                } else {
                    MusicStreamingController.controlRequest("play")
                }
            }
            "kkori.previous" -> {
            }
            "kkori.next" -> {
            }
            "kkori.cancel" -> MusicStreamingController.controlRequest("release")
        }
    }
}