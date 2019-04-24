package com.tails.presentation.streaming

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.tails.data.remote.extract.YouTubeExtractor
import com.tails.domain.entities.VideoMeta
import com.tails.domain.entities.YtFile
import com.tails.presentation.ui.base.SingleLiveEvent

class YouTubeMusicStreamViewModel(application: Application) : AndroidViewModel(application), PlaybackInfoListener {

    companion object {
        lateinit var playerAdapter: PlayerAdapter
    }

    init {
        playerAdapter = MusicPlayerHolder(this)
    }

    val durationLiveData = MutableLiveData<Int>()
    val positionLiveData = MutableLiveData<Int>()
    val stateSingleLiveEvent = SingleLiveEvent<Int>()
    val playBackSingleLiveEvent = SingleLiveEvent<Unit>()

    private val youTubeExtractor = @SuppressLint("StaticFieldLeak")
    object : YouTubeExtractor(getApplication()) {
        override fun onExtractionComplete(ytFile: YtFile?, videoMeta: VideoMeta?) {
            if(ytFile != null && videoMeta != null) {
                Log.e("Result Url", ytFile.url)

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

    fun youTubeExtract(videoId: String, parseDashManifest: Boolean, includeWebM: Boolean){
        youTubeExtractor.extract(videoId, parseDashManifest, includeWebM)
    }

    override fun onDurationChanged(duration: Int) {
        durationLiveData.value = duration
    }

    override fun onPositionChanged(position: Int) {
        positionLiveData.value = position
    }

    override fun onStateChanged(state: Int) {
        stateSingleLiveEvent.value = state
    }

    override fun onPlaybackCompleted() = playBackSingleLiveEvent.call()
}