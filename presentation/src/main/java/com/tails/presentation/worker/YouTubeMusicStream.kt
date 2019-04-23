package com.tails.presentation.worker

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import com.tails.domain.entities.VideoMeta
import com.tails.data.remote.extract.YouTubeExtractor
import com.tails.domain.entities.YtFile

class YouTubeMusicStream(context: Context) : YouTubeExtractor(context){
    override fun onExtractionComplete(ytFile: YtFile?, videoMeta: VideoMeta?) {
        Log.e("ytFile", ytFile?.url)
        Log.e("videoMeta", videoMeta.toString())

        val mediaPlayer = MediaPlayer()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        mediaPlayer.setAudioAttributes(audioAttributes)
        mediaPlayer.setDataSource(ytFile?.url)
        mediaPlayer.prepare()
        mediaPlayer.start()
    }
}