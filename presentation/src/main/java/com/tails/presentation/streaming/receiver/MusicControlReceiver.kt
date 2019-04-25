package com.tails.presentation.streaming.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.tails.presentation.streaming.worker.MusicStreamingController

class MusicControlReceiver : BroadcastReceiver() {

    private val musicControlBuilder =
        OneTimeWorkRequestBuilder<MusicStreamingController>().apply {
            setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
        }

    private val workManager = WorkManager.getInstance()

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent!!.action) {
            "kkori.pause" -> {
                if (MusicStreamingController.isPlaying) {
                    workManager.enqueue(
                        musicControlBuilder.setInputData(
                            Data.Builder().putString("control", "pause")
                                .build()
                        ).build()
                    )
                } else {
                    workManager.enqueue(
                        musicControlBuilder.setInputData(
                            Data.Builder().putString("control", "play")
                                .build()
                        ).build()
                    )
                }
            }
            "kkori.previous" -> {
            }
            "kkori.next" -> {
            }
        }
    }
}