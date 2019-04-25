package com.tails.presentation.streaming

import android.content.Context
import android.util.Log
import androidx.work.*
import com.tails.data.remote.extract.YouTubeExtractor
import com.tails.domain.entities.VideoMeta
import com.tails.domain.entities.YtFile
import com.tails.presentation.streaming.worker.MusicStreamingController
import com.tails.presentation.streaming.worker.PlaybackInfoListener

class MusicStreamPrepare(playbackInfoListener: PlaybackInfoListener, context: Context) : YouTubeExtractor(context) {

    init {
        MusicStreamingController.playbackInfoListener = playbackInfoListener
    }

    override fun onExtractionComplete(ytFile: YtFile?, videoMeta: VideoMeta?) {
        if (ytFile != null && videoMeta != null) {
            Log.e("result url", ytFile.url)

            val data = Data.Builder()
                .putString("control", "load")
                .putString("streamUrl", ytFile.url)
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val streamMusic = OneTimeWorkRequestBuilder<MusicStreamingController>()
                .setConstraints(constraints)
                .setInputData(data)
                .build()

            WorkManager.getInstance().enqueue(streamMusic)
        }
    }
}