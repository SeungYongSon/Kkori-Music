package com.tails.data.remote.parse

import com.tails.data.remote.parse.VideoMetaParseUtil.USER_AGENT
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class VideoMetaParser @Inject constructor() {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(5, TimeUnit.MINUTES)
        .build()

    fun parse(videoID: String): Single<Response> {
        val request = Request.Builder()
            .url("https://www.youtube.com/get_video_info?video_id=$videoID&eurl=https://youtube.googleapis.com/v/$videoID")
            .addHeader("User-Agent", USER_AGENT)
            .build()

        return Single.fromCallable { client.newCall(request).execute() }
    }
}