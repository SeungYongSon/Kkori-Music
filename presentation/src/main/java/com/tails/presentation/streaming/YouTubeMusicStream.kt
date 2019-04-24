package com.tails.presentation.streaming

import android.content.Context
import android.util.Log
import androidx.work.*
import com.tails.data.remote.extract.YouTubeExtractor
import com.tails.domain.entities.VideoMeta
import com.tails.domain.entities.YtFile

class YouTubeMusicStream(playbackInfoListener: PlaybackInfoListener, context: Context) : YouTubeExtractor(context) {

    companion object {
        lateinit var playerAdapter: PlayerAdapter
    }

    init {
        playerAdapter = MusicPlayerHolder(playbackInfoListener)
    }

    override fun onExtractionComplete(ytFile: YtFile?, videoMeta: VideoMeta?) {
        if(ytFile != null && videoMeta != null) {
            Log.e("result url", ytFile.url)

            val data = Data.Builder()
                .putString("streamUrl", ytFile.url)
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<PlayMusicWorker>()
                .setConstraints(constraints)
                .setInputData(data)
                .build()

            WorkManager.getInstance().enqueue(workRequest)
        }
    }
}