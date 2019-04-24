package com.tails.presentation.streaming

interface PlayerAdapter {

    val isPlaying: Boolean

    fun loadMusic(streamUrl: String)

    fun release()

    fun play()

    fun reset()

    fun pause()

    fun initializeProgressCallback()

    fun seekTo(position: Int)
}