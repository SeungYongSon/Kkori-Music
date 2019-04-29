package com.tails.presentation.ui

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.tails.presentation.R
import com.tails.presentation.streaming.controller.MusicStreamingController
import com.tails.presentation.streaming.controller.PlaybackInfoListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), PlaybackInfoListener, View.OnClickListener {

    private var userIsSeeking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.tails.presentation.R.layout.activity_main)

//        YouTubeSearcher.search("넘쳐흘러")
//        YouTubeSearcher.get().apply {
//            test_text.text = toString()
//            YouTubeSearcher.cancel(false)
//        }

        initializeUI()

        MusicStreamingController.prepare(this, applicationContext, "VYOjWnS4cMY")
    }

    override fun onBackPressed() {
        if (MusicStreamingController.isPlaying) moveTaskToBack(true)
        else {
            MusicStreamingController.controlRequest("release")
            super.onBackPressed()
        }
    }

    private fun initializeUI() {
        button_play.setOnClickListener(this)
        button_pause.setOnClickListener(this)
        button_reset.setOnClickListener(this)

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
                    MusicStreamingController.controlRequest("seek", "seekPosition", userSelectedPosition)
                }
            })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_play -> {
                MusicStreamingController.controlRequest("play")
            }
            R.id.button_pause -> {
                MusicStreamingController.controlRequest("pause")
            }
            R.id.button_reset -> {
                MusicStreamingController.controlRequest("reset")
            }
        }
    }

    override fun onDurationChanged(duration: Int) {
        seekbar_audio.max = duration
    }

    override fun onPositionChanged(position: Int) {
        if (!userIsSeeking) {
            seekbar_audio.progress = position
        }
    }

    override fun onStateChanged(state: Int) {}

    override fun onPlaybackCompleted() {}
}