package com.tails.presentation.streaming.controller

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.PowerManager
import android.util.Log
import androidx.work.*
import com.tails.data.remote.extract.ExtractComplete
import com.tails.data.remote.extract.YouTubeExtractor
import com.tails.domain.entities.VideoMeta
import com.tails.domain.entities.YtFile
import com.tails.presentation.streaming.notification.MusicControlNotification
import java.lang.IllegalStateException

class MusicStreamingController(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters),
    PlayerAdapter, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    companion object {

        var isPreparing: Boolean = false
        val isPlaying: Boolean
            get() = if (mediaPlayer != null) mediaPlayer!!.isPlaying else false

        lateinit var playbackInfoListener: PlaybackInfoListener

        private var mediaPlayer: MediaPlayer? = null
        private var wifiLock: WifiManager.WifiLock? = null

        private lateinit var url: String

        private val workManager = WorkManager.getInstance()
        private val musicControlBuilder =
            OneTimeWorkRequestBuilder<MusicStreamingController>().apply {
                setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
            }

        private lateinit var musicExtractor: YouTubeExtractor
        private val extractComplete = object : ExtractComplete {
            override fun onExtractComplete(ytFile: YtFile?, videoMeta: VideoMeta?) {
                if (ytFile != null && videoMeta != null) {
                    Log.e("result url", ytFile.url)
                    this@Companion.videoMeta = videoMeta
                    controlRequest("load", "streamUrl", ytFile.url!!)
                }
            }
        }
        private lateinit var videoMeta: VideoMeta

        private val seekHandler = Handler()

        fun controlRequest(action: String) {
            workManager.enqueue(
                musicControlBuilder.setInputData(
                    Data.Builder().putString(
                        "control", action
                    ).build()
                ).build()
            )
        }

        fun controlRequest(action: String, key: String, value: String) {
            workManager.enqueue(
                musicControlBuilder.setInputData(
                    Data.Builder()
                        .putString("control", action)
                        .putString(key, value)
                        .build()
                ).build()
            )
        }

        fun controlRequest(action: String, key: String, value: Int) {
            workManager.enqueue(
                musicControlBuilder.setInputData(
                    Data.Builder()
                        .putString("control", action)
                        .putInt(key, value)
                        .build()
                ).build()
            )
        }

        fun prepare(videoMeta: VideoMeta, context: Context) {
            musicExtractor = YouTubeExtractor(extractComplete, context)
            musicExtractor.extract(videoMeta, parseDashManifest = true, includeWebM = true)
            isPreparing = true
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        isPreparing = false
        playbackInfoListener.onPrepareCompleted(videoMeta)
        initializeProgressCallback()
        play()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        stopToUpdateSeekBar()
        playbackInfoListener.onStateChanged(PlaybackInfoListener.State.COMPLETED)
        playbackInfoListener.onPlaybackCompleted()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        release()
        isPreparing = false
        return false
    }

    override fun loadMusic(streamUrl: String) {
        if (mediaPlayer != null)
            release()

        initializeMediaPlayer()

        if (mediaPlayer != null) {
            url = streamUrl
            mediaPlayer!!.setDataSource(streamUrl)
            mediaPlayer!!.prepareAsync()
        }
    }

    override fun play() {
        if (mediaPlayer != null) {
            mediaPlayer!!.start()
            playbackInfoListener.onStateChanged(PlaybackInfoListener.State.PLAYING)
            MusicControlNotification.showNotification(applicationContext, videoMeta)
        }
    }

    override fun seekTo(position: Int) {
        if (mediaPlayer != null) {
            mediaPlayer!!.seekTo(position)
        }
    }

    override fun pause() {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            playbackInfoListener.onStateChanged(PlaybackInfoListener.State.PAUSED)
            MusicControlNotification.showNotification(applicationContext, videoMeta)
        }
    }

    override fun reset() {
        if (mediaPlayer != null) {
            mediaPlayer!!.reset()
            playbackInfoListener.onStateChanged(PlaybackInfoListener.State.RESET)
            MusicControlNotification.removeNotification(applicationContext)
            loadMusic(url)
            stopToUpdateSeekBar()
        }
    }

    override fun release() {
        if (mediaPlayer != null) {
            wifiLock!!.release()
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = null
            offSeekHandler()
            MusicControlNotification.removeNotification(applicationContext)
        }
    }

    override fun initializeProgressCallback() {
        if (mediaPlayer != null) {
            val duration = mediaPlayer!!.duration
            playbackInfoListener.onDurationChanged(duration)
            playbackInfoListener.onPositionChanged(0)
            onSeekHandler()
        }
    }

    private fun initializeMediaPlayer() {
        if (mediaPlayer != null) mediaPlayer!!.release()

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            setOnPreparedListener(this@MusicStreamingController)
            setOnCompletionListener(this@MusicStreamingController)
            setOnErrorListener(this@MusicStreamingController)
            isLooping = true
        }

        wifiLock = (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .createWifiLock(WifiManager.WIFI_MODE_FULL, "wifiLock")
        wifiLock!!.acquire()
    }

    override fun doWork(): Result = try {
        when (inputData.getString("control")) {
            "load" -> loadMusic(inputData.getString("streamUrl")!!)
            "play" -> play()
            "pause" -> pause()
            "seek" -> seekTo(inputData.getInt("seekPosition", 0))
            "reset" -> reset()
            "release" -> release()
            "seekUpdate" -> updateSeekBar()
            else -> Result.failure()
        }
        Result.success()
    } catch (e: Exception) {
        Result.failure()
    }

    private fun onSeekHandler() {
        seekHandler.post(object : Runnable {
            override fun run() {
                if (mediaPlayer != null) {
                    updateSeekBar()
                    seekHandler.postDelayed(this, 1000)
                }
            }
        })
    }

    private fun offSeekHandler() {
        seekHandler.removeCallbacks(null)
        stopToUpdateSeekBar()
    }

    private fun updateSeekBar() {
        if (mediaPlayer != null) {
            try{
                val currentPosition = mediaPlayer!!.currentPosition
                playbackInfoListener.onPositionChanged(currentPosition)
            } catch (e : IllegalStateException){ }
        }
    }

    private fun stopToUpdateSeekBar() {
        playbackInfoListener.onPositionChanged(0)
    }
}