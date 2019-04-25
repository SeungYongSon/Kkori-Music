package com.tails.presentation.streaming.worker

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MusicStreamingController(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters), PlayerAdapter, MediaPlayer.OnPreparedListener {

    companion object {
        lateinit var playbackInfoListener: PlaybackInfoListener

        private const val PLAYBACK_POSITION_REFRESH_INTERVAL_MS = 1000

        private var mediaPlayer: MediaPlayer? = null
        private var executor: ScheduledExecutorService? = null

        private lateinit var url: String

        val isPlaying: Boolean
            get() = if (mediaPlayer != null) mediaPlayer!!.isPlaying else false
    }

    override fun doWork(): Result = try {
        when (inputData.getString("control")) {
            "load" -> {
                loadMusic(inputData.getString("streamUrl")!!)
                Result.success()
            }
            "play" -> {
                play()
                Result.success()
            }
            "pause" -> {
                pause()
                Result.success()
            }
            "seek" -> {
                val position = inputData.getInt("seekPosition", 0)
                seekTo(position)
                Result.success()
            }
            "reset" -> {
                reset()
                Result.success()
            }
            "release" -> {
                release()
                Result.success()
            }
            else -> Result.failure()
        }
    } catch (e: Exception) {
        Result.failure()
    }

    override fun loadMusic(streamUrl: String) {
        initializeMediaPlayer()
        if (mediaPlayer != null) {
            url = streamUrl

            mediaPlayer!!.setDataSource(streamUrl)
            mediaPlayer!!.prepare()
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        initializeProgressCallback()
        play()
    }

    override fun release() {
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
        }
    }

    override fun play() {
        if (mediaPlayer != null) {
            mediaPlayer!!.start()
            playbackInfoListener.onStateChanged(PlaybackInfoListener.State.PLAYING)
            startUpdatingCallbackWithPosition()
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
        }
    }

    override fun reset() {
        if (mediaPlayer != null) {
            mediaPlayer!!.reset()
            playbackInfoListener.onStateChanged(PlaybackInfoListener.State.RESET)
            loadMusic(url)
            stopUpdatingCallbackWithPosition(true)
        }
    }

    override fun initializeProgressCallback() {
        if (mediaPlayer != null) {
            val duration = mediaPlayer!!.duration
            playbackInfoListener.onDurationChanged(duration)
            playbackInfoListener.onPositionChanged(0)
        }
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setOnCompletionListener {
                stopUpdatingCallbackWithPosition(true)
                playbackInfoListener.onStateChanged(PlaybackInfoListener.State.COMPLETED)
                playbackInfoListener.onPlaybackCompleted()
            }
            setOnPreparedListener(this@MusicStreamingController)
            isLooping = true
        }
    }

    private fun startUpdatingCallbackWithPosition() {
        val seekBarPositionUpdateTask = Runnable { updateProgressCallbackTask() }

        executor = Executors.newSingleThreadScheduledExecutor().apply {
            scheduleAtFixedRate(
                seekBarPositionUpdateTask,
                0,
                PLAYBACK_POSITION_REFRESH_INTERVAL_MS.toLong(),
                TimeUnit.MILLISECONDS
            )
        }
    }

    private fun stopUpdatingCallbackWithPosition(resetUIPlaybackPosition: Boolean) {
        if (executor != null) {
            executor!!.shutdownNow()
            if (resetUIPlaybackPosition) {
                playbackInfoListener.onPositionChanged(0)
            }
        }
    }

    private fun updateProgressCallbackTask() {
        if (mediaPlayer != null) {
            val currentPosition = mediaPlayer!!.currentPosition
            playbackInfoListener.onPositionChanged(currentPosition)
        }
    }
}