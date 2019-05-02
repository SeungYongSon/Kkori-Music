package com.tails.presentation.streaming.extractor

import android.content.Context
import android.util.Log
import com.tails.data.remote.extract.YouTubeExtractor
import com.tails.domain.entities.VideoMeta
import com.tails.domain.entities.YtFile
import com.tails.presentation.streaming.controller.MusicStreamingController

class MusicExtractor(context: Context) : YouTubeExtractor(context) {

    lateinit var videoMeta: VideoMeta
    private lateinit var ytFile: YtFile

    override fun onExtractionComplete(ytFile: YtFile?, videoMeta: VideoMeta?) {
        if (ytFile != null && videoMeta != null) {
            Log.e("result url", ytFile.url)

            this.ytFile = ytFile
            this.videoMeta = videoMeta

            MusicStreamingController.controlRequest("load", "streamUrl", ytFile.url!!)
        }
    }
}