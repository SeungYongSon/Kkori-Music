package com.tails.presentation.streaming

import android.media.AudioAttributes
import android.media.MediaPlayer
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MusicPlayerHolder(private val playbackInfoListener: PlaybackInfoListener) : PlayerAdapter,
    MediaPlayer.OnPreparedListener {

    companion object {
        private const val PLAYBACK_POSITION_REFRESH_INTERVAL_MS = 1000
    }

    private var mediaPlayer: MediaPlayer? = null
    private var executor: ScheduledExecutorService? = null

    private lateinit var streamUrl: String

    override val isPlaying: Boolean get() = mediaPlayer!!.isPlaying

    override fun onPrepared(mp: MediaPlayer?) {
        initializeProgressCallback()
        play()
    }

    override fun loadMusic(streamUrl: String) {
        initializeMediaPlayer()
        if (mediaPlayer != null) {
            this.streamUrl = streamUrl

            mediaPlayer!!.setDataSource(streamUrl)
            mediaPlayer!!.prepare()
        }
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
            loadMusic(streamUrl)
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
            setOnPreparedListener(this@MusicPlayerHolder)
            isLooping = true
        }
    }

    private fun startUpdatingCallbackWithPosition() {
        val seekbarPositionUpdateTask = Runnable { updateProgressCallbackTask() }

        executor = Executors.newSingleThreadScheduledExecutor().apply {
            scheduleAtFixedRate(
                seekbarPositionUpdateTask,
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