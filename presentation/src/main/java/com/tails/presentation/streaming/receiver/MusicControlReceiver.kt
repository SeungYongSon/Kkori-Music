package com.tails.presentation.streaming.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.tails.presentation.streaming.controller.MusicStreamingController

class MusicControlReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent!!.action) {
            "kkori.pause" -> {
                if (!MusicStreamingController.isPrepare) {
                    if (MusicStreamingController.isPlaying) {
                        MusicStreamingController.controlRequest("pause")
                    } else {
                        MusicStreamingController.controlRequest("play")
                    }
                } else {
                    Toast.makeText(context, "노래를 로딩중 입니다.", Toast.LENGTH_SHORT).show()
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