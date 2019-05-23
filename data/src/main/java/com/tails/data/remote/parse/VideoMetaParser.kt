package com.tails.data.remote.parse

import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class VideoMetaParser @Inject constructor() {

    companion object {
        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36"
    }

    private val client: OkHttpClient = OkHttpClient.Builder().build()

    fun parse(videoID: String): Single<Response> {
        val request = Request.Builder()
            .url("https://www.youtube.com/get_video_info?video_id=$videoID&eurl=https://youtube.googleapis.com/v/$videoID")
            .addHeader("User-Agent", USER_AGENT)
            .build()

        return Single.fromCallable { client.newCall(request).execute() }
    }
}