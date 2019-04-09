package com.tails.data.remote.youtube

import android.os.AsyncTask
import android.util.Log
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.tails.data.util.Util
import com.tails.domain.entities.YtVideo
import java.io.IOException
import java.text.NumberFormat
import java.util.*
import java.util.regex.Pattern

object YouTubeSearcher : AsyncTask<String, Void, List<YtVideo>>() {

    private val youtube = YouTube.Builder(
        NetHttpTransport(),
        JacksonFactory()
    ) {}.setApplicationName("Kkori Music").build()

    private var searchList: YouTube.Search.List =
        youtube.search().list(Config.YOUTUBE_SEARCH_LIST_PART).let {
            it.key = Config.YOUTUBE_API
            it.type = Config.YOUTUBE_SEARCH_LIST_TYPE
            it.maxResults = Config.YOUTUBE_MAX_RESULTS
            it.fields = Config.YOUTUBE_SEARCH_LIST_FIELDS
            it.set(Config.YOUTUBE_LANGUAGE_KEY, Locale.getDefault().language)
        }

    private lateinit var keywords: String
    private lateinit var nextPageToken: String

    override fun doInBackground(vararg params: String?): List<YtVideo>? {
        try {
            return searchVideos()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun search(keywords: String) {
        this.keywords = keywords
        this.nextPageToken = ""
        this.execute()
    }

    fun searchNext() {
        this.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
    }

    private fun searchVideos(): List<YtVideo> {
        val ytVideos = ArrayList<YtVideo>()
        try {
            searchList.q = keywords
            if (nextPageToken.isNotEmpty()) {
                searchList.pageToken = nextPageToken
            }

            val pattern = Pattern.compile(Config.YOUTUBE_REGEX)
            val matcher = pattern.matcher(keywords)

            if (matcher.find()) {
                val singleVideo = youtube.videos().list(Config.YOUTUBE_VIDEO_PART)
                singleVideo.key = Config.YOUTUBE_API
                singleVideo.fields = Config.YOUTUBE_VIDEO_FIELDS
                singleVideo.set(Config.YOUTUBE_LANGUAGE_KEY, Locale.getDefault().language)
                singleVideo.id = matcher.group(1)

                val resp = singleVideo.execute()
                val videoResults = resp.items

                videoResults.forEach {
                    val item = YtVideo()

                    if (it != null) {
                        item.title = it.snippet.title
                        item.thumbnailURL = it.snippet.thumbnails.default.url
                        item.id = it.id

                        if (it.statistics != null) {
                            val viewsNumber = it.statistics.viewCount
                            val viewsFormatted = NumberFormat.getIntegerInstance().format(viewsNumber) + " views"
                            item.viewCount = viewsFormatted
                        }

                        if (it.contentDetails != null) {
                            val isoTime = it.contentDetails.duration
                            val time = Util.convertISO8601DurationToNormalTime(isoTime)
                            item.duration = time
                        }
                    } else {
                        item.duration = "NA"
                    }
                    ytVideos.add(item)
                }
                nextPageToken = ""
            } else {
                val videoList = youtube.videos().list(Config.YOUTUBE_VIDEO_LIST_PART)
                videoList.key = Config.YOUTUBE_API
                videoList.fields = Config.YOUTUBE_VIDEO_LIST_FIELDS
                videoList.set(Config.YOUTUBE_LANGUAGE_KEY, Locale.getDefault().language)

                val searchResp = searchList.execute()
                val searchResults = searchResp.items

                nextPageToken = searchResp.nextPageToken

                videoList.id = Util.concatenateIDs(searchResults)
                val resp = videoList.execute()
                val videoResults = resp.items

                searchResults.forEach {
                    if (it.id != null) {
                        val item = YtVideo()

                        item.title = it.snippet.title
                        item.thumbnailURL = it.snippet.thumbnails.default.url
                        item.id = it.id.videoId

                        val videoResult = videoResults[0]
                        if (videoResult != null) {
                            if (videoResult.statistics != null) {
                                val viewsNumber = videoResult.statistics.viewCount
                                val viewsFormatted = NumberFormat.getIntegerInstance().format(viewsNumber) + " views"
                                item.viewCount = viewsFormatted
                            }
                            if (videoResult.contentDetails != null) {
                                val isoTime = videoResult.contentDetails.duration
                                val time = Util.convertISO8601DurationToNormalTime(isoTime)
                                item.duration = time
                            }
                        } else {
                            item.duration = "NA"
                        }
                        ytVideos.add(item)
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("YouTubeSearcher", "Could not initialize: $e")
            e.printStackTrace()
        }
        return ytVideos
    }
}