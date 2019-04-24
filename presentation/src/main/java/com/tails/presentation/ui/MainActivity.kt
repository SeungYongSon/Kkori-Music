package com.tails.presentation.ui

import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.tails.presentation.streaming.PlaybackInfoListener
import com.tails.presentation.streaming.YouTubeMusicStream
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), PlaybackInfoListener {

    var userIsSeeking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.tails.presentation.R.layout.activity_main)

//        YouTubeSearcher.search("넘쳐흘러")
//        YouTubeSearcher.get().apply {
//            test_text.text = toString()
//            YouTubeSearcher.cancel(false)
//        }
        initializeUI()

        val musicStream = YouTubeMusicStream(this, applicationContext)
        musicStream.extract("RRKJiM9Njr8", parseDashManifest = true, includeWebM = true)
    }

    private fun initializeUI() {
        button_play.setOnClickListener {
            YouTubeMusicStream.playerAdapter.play()
        }
        button_pause.setOnClickListener {
            YouTubeMusicStream.playerAdapter.pause()
        }
        button_reset.setOnClickListener {
            YouTubeMusicStream.playerAdapter.reset()
        }

        seekbar_audio.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                var userSelectedPosition = 0

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    userIsSeeking = true
                }

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        userSelectedPosition = progress
                    }
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    userIsSeeking = false
                    YouTubeMusicStream.playerAdapter.seekTo(userSelectedPosition)
                }
            })
    }

    override fun onDurationChanged(duration: Int) {
        seekbar_audio.max = duration
    }

    override fun onPositionChanged(position: Int) {
        if (!userIsSeeking) {
            seekbar_audio.progress = position
        }
    }

    override fun onStateChanged(state: Int) {

    }

    override fun onPlaybackCompleted() {

    }
}