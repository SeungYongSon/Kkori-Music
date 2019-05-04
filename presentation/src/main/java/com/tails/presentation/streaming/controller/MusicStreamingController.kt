package com.tails.presentation.streaming.controller

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.PowerManager
import androidx.work.*
import com.tails.presentation.streaming.extractor.MusicExtractor
import com.tails.presentation.streaming.notification.MusicControlNotification

class MusicStreamingController(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters), PlayerAdapter, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

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

        private lateinit var musicExtractor: MusicExtractor

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

        fun prepare(videoId: String, context: Context) {
            musicExtractor = MusicExtractor(context)
            musicExtractor.extract(videoId, parseDashManifest = true, includeWebM = true)
            isPreparing = true
        }

        fun prepare(playbackInfoListener: PlaybackInfoListener, context: Context, videoId: String) {
            musicExtractor = MusicExtractor(context)
            musicExtractor.extract(videoId, parseDashManifest = true, includeWebM = true)
            this.playbackInfoListener = playbackInfoListener
            isPreparing = true
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        isPreparing = false
        playbackInfoListener.onPrepareCompleted(musicExtractor.videoMeta)
        initializeProgressCallback()
        play()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        stopToUpdateSeekBar()
        playbackInfoListener.onStateChanged(PlaybackInfoListener.State.COMPLETED)
        playbackInfoListener.onPlaybackCompleted()
    }

    override fun loadMusic(streamUrl: String) {
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
            MusicControlNotification.showNotification(applicationContext, musicExtractor.videoMeta)
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
            MusicControlNotification.showNotification(applicationContext, musicExtractor.videoMeta)
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
                if (mediaPlayer != null) updateSeekBar()
                seekHandler.postDelayed(this, 1000)
            }
        })
    }

    private fun offSeekHandler() {
        seekHandler.removeCallbacks(null)
        stopToUpdateSeekBar()
    }

    private fun updateSeekBar() {
        if (mediaPlayer != null) {
            val currentPosition = mediaPlayer!!.currentPosition
            playbackInfoListener.onPositionChanged(currentPosition)

        }
    }

    private fun stopToUpdateSeekBar() {
        playbackInfoListener.onPositionChanged(0)
    }
}