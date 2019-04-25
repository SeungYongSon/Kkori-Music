package com.tails.presentation.ui

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.tails.presentation.R
import com.tails.presentation.streaming.worker.PlaybackInfoListener
import com.tails.presentation.streaming.MusicStreamPrepare
import com.tails.presentation.streaming.worker.MusicStreamingController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), PlaybackInfoListener, View.OnClickListener {

    private val workManager = WorkManager.getInstance()
    private val musicControlBuilder =
        OneTimeWorkRequestBuilder<MusicStreamingController>().apply {
            setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
        }
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

        val musicStreamPrepare = MusicStreamPrepare(this, applicationContext)
        musicStreamPrepare.extract("CP3R8rpbBgQ", parseDashManifest = true, includeWebM = true)
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
                    workManager.enqueue(
                        musicControlBuilder.setInputData(
                            Data.Builder()
                                .putString("control", "seek")
                                .putInt("seekPosition", userSelectedPosition)
                                .build()
                        ).build()
                    )
                }
            })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_play -> {
                workManager.enqueue(
                    musicControlBuilder.setInputData(
                        Data.Builder().putString("control", "play").build()
                    ).build()
                )
            }
            R.id.button_pause -> {
                workManager.enqueue(
                    musicControlBuilder.setInputData(
                        Data.Builder().putString("control", "pause").build()
                    ).build()
                )
            }
            R.id.button_reset -> {
                workManager.enqueue(
                    musicControlBuilder.setInputData(
                        Data.Builder().putString("control", "reset").build()
                    ).build()
                )
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