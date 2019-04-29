package com.tails.presentation.streaming.controller

import androidx.annotation.IntDef

interface PlaybackInfoListener {

    @IntDef(
        State.INVALID,
        State.PLAYING,
        State.PAUSED,
        State.RESET,
        State.COMPLETED
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class State {
        companion object {
            const val INVALID = -1
            const val PLAYING = 0
            const val PAUSED = 1
            const val RESET = 2
            const val COMPLETED = 3
        }
    }

    fun onDurationChanged(duration: Int)

    fun onPositionChanged(position: Int)

    fun onStateChanged(@State state: Int)

    fun onPlaybackCompleted()
}