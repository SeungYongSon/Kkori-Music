package com.tails.dukkorimusic

import android.os.Bundle
import android.util.Log
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.tails.dukkorimusic.youtube.YoutubeSearch
import com.tails.dukkorimusic.utils.Config
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : YouTubeBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listenerY = object : YouTubePlayer.OnInitializedListener{
            override fun onInitializationFailure(ypp: YouTubePlayer.Provider?, yp: YouTubeInitializationResult?) {}
            override fun onInitializationSuccess(ypp: YouTubePlayer.Provider?, yp: YouTubePlayer, p2: Boolean) {
                yp.loadVideo("GBaKmkppD5A")
            }
        }

        player.initialize(Config.YOUTUBE_API, listenerY)

        YoutubeSearch.apply {
            this.search("띵")
            Log.e("Activity", get().toString())
        }
    }
}
