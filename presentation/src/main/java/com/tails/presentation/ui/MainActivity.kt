package com.tails.presentation.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tails.data.remote.search.YouTubeSearcher
import com.tails.presentation.worker.YouTubeMusicStream
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.tails.presentation.R.layout.activity_main)

        YouTubeSearcher.search("넘쳐흘러")
        YouTubeSearcher.get().apply {
            test_text.text = toString()
            YouTubeSearcher.cancel(false)
        }

        val musicStream = YouTubeMusicStream(applicationContext)
        musicStream.extract("O6tXw1BmJaM", parseDashManifest = true, includeWebM = true)
    }
}