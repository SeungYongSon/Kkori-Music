package com.tails.presentation.streaming

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.tails.presentation.streaming.YouTubeMusicStream.Companion.playerAdapter
import java.lang.Exception

class PlayMusicWorker(context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {

    override fun doWork(): Result = try {
        val streamUrl = inputData.getString("streamUrl")
        playerAdapter.loadMusic(streamUrl!!)
        Result.success()
    } catch (e: Exception) {
        Result.failure()
    }
}