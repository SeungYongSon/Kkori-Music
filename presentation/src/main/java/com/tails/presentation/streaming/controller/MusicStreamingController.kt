package com.tails.presentation.streaming.controller

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.PowerManager
import androidx.work.*
import com.tails.domain.entity.VideoMeta
import com.tails.presentation.streaming.notification.MusicControlNotification
import javax.inject.Inject

class MusicStreamingController @Inject constructor(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters),
    PlayerAdapter, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    companion object {
        val isPlaying: Boolean
            get() = if (mediaPlayer != null) mediaPlayer!!.isPlaying else false

        lateinit var playbackInfoListener: PlaybackInfoListener

        private var mediaPlayer: MediaPlayer? = null

        lateinit var url: String
        lateinit var videoMeta: VideoMeta

        private val seekHandler = Handler()

        val workManager = WorkManager.getInstance()
        val musicControlBuilder =
            OneTimeWorkRequestBuilder<MusicStreamingController>().apply {
                setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
            }

        fun controlRequest(action: String) {
            workManager.enqueue(
                musicControlBuilder.setInputData(
                    Data.Builder().putString(
                        "control", action
                    ).build()
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

    override fun onPrepared(mp: MediaPlayer?) {
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
        val mediaPlayer = mediaPlayer

        if (mediaPlayer != null) {
            mediaPlayer.start()
            playbackInfoListener.onStateChanged(PlaybackInfoListener.State.PLAYING)
            MusicControlNotification.showNotification(applicationContext, videoMeta)
        }
    }

    override fun seekTo(position: Int) {
        val mediaPlayer = mediaPlayer
        mediaPlayer?.seekTo(position)
    }

    override fun pause() {
        val mediaPlayer = mediaPlayer

        if (mediaPlayer != null && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            playbackInfoListener.onStateChanged(PlaybackInfoListener.State.PAUSED)
            MusicControlNotification.showNotification(applicationContext, videoMeta)
        }
    }

    override fun reset() {
        val mediaPlayer = mediaPlayer

        if (mediaPlayer != null) {
            mediaPlayer.reset()
            playbackInfoListener.onStateChanged(PlaybackInfoListener.State.RESET)
            MusicControlNotification.removeNotification(applicationContext)
            loadMusic(url)
            stopToUpdateSeekBar()
        }
    }

    override fun release() {
        if (mediaPlayer != null) {
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
            try {
                val currentPosition = mediaPlayer!!.currentPosition
                playbackInfoListener.onPositionChanged(currentPosition)
            } catch (e: IllegalStateException) {
            }
        }
    }

    private fun stopToUpdateSeekBar() = playbackInfoListener.onPositionChanged(0)
}