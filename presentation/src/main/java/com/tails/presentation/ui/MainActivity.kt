package com.tails.presentation.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.tails.data.remote.youtube.VideoMeta
import com.tails.data.remote.youtube.YouTubeExtractor
import com.tails.data.remote.youtube.YouTubeSearcher
import com.tails.data.remote.youtube.YtFile
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

        object : YouTubeExtractor(applicationContext){
            override fun onExtractionComplete(ytFile: YtFile?, videoMeta: VideoMeta?) {
                Log.e("ytFile", ytFile.toString())
                Log.e("videoMeta", videoMeta.toString())
            }
        }.extract("RRKJiM9Njr8", parseDashManifest = true, includeWebM = true)
    }
}
