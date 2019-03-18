package com.tails.data.youtube

import android.os.AsyncTask
import android.util.Log
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.tails.data.util.Util
import com.tails.domain.YoutubeVideo
import java.io.IOException
import java.text.NumberFormat
import java.util.*
import java.util.regex.Pattern

object YoutubeSearch : AsyncTask<String, Void, List<YoutubeVideo>>() {

    private val youtube = YouTube.Builder(
        NetHttpTransport(),
        JacksonFactory()
    ) {}.setApplicationName("Kkori Music").build()

    private lateinit var searchList : YouTube.Search.List

    private var keywords : String? = null
    private var currentPageToken : String? = null
    private var nextPageToken : String? = null

    override fun doInBackground(vararg params: String?): List<YoutubeVideo>? {
        if (keywords == null) return null
        try {
            return searchVideos()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun search(keywords: String) {
        YoutubeSearch.keywords = keywords
        this.execute()
    }

    fun search(keywords: String, currentPageToken: String) {
        YoutubeSearch.keywords = keywords
        YoutubeSearch.currentPageToken = currentPageToken
        nextPageToken = null
        this.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
    }

    private fun searchVideos() : List<YoutubeVideo>{
        val ytVideos = ArrayList<YoutubeVideo>()
        try {
            searchList = youtube.search().list(
                Config.YOUTUBE_SEARCH_LIST_PART
            )

            searchList.q =
                keywords
            searchList.key = Config.YOUTUBE_API
            searchList.type =
                Config.YOUTUBE_SEARCH_LIST_TYPE
            searchList.maxResults =
                Config.YOUTUBE_MAX_RESULTS
            searchList.fields =
                Config.YOUTUBE_SEARCH_LIST_FIELDS
            searchList.set(Config.YOUTUBE_LANGUAGE_KEY, Locale.getDefault().language)

            if (currentPageToken != null) {
                searchList.pageToken =
                    currentPageToken
            }

            val pattern = Pattern.compile(Config.YOUTUBE_REGEX)
            val matcher = pattern.matcher(keywords)

            if(matcher.find()){
                val singleVideo = youtube.videos().list(Config.YOUTUBE_VIDEO_PART)
                singleVideo.key = Config.YOUTUBE_API
                singleVideo.fields = Config.YOUTUBE_VIDEO_FIELDS
                singleVideo.set(Config.YOUTUBE_LANGUAGE_KEY, Locale.getDefault().language)
                singleVideo.id = matcher.group(1)

                val resp = singleVideo.execute()
                val videoResults = resp.items

                videoResults.forEach{
                    val item = YoutubeVideo()

                    if(it != null){
                        item.title = it.snippet.title
                        item.thumbnailURL = it.snippet.thumbnails.default.url
                        item.id = it.id

                        if(it.statistics != null){
                            val viewsNumber = it.statistics.viewCount
                            val viewsFormatted = NumberFormat.getIntegerInstance().format(viewsNumber) + " views"
                            item.viewCount = viewsFormatted
                        }

                        if(it.contentDetails != null){
                            val isoTime = it.contentDetails.duration
                            val time = Util.convertISO8601DurationToNormalTime(isoTime)
                            item.duration = time
                        }
                    } else{
                        item.duration = "NA"
                    }
                    ytVideos.add(item)
                }
            } else{
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

                var index = 0
                searchResults.forEach {
                    if(it.id != null){
                        val item = YoutubeVideo()

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
                        index++
                    }
                }
            }
        }catch (e : IOException){
            Log.e("YoutubeSearch", "Could not initialize: $e")
            e.printStackTrace()
        }
        Log.e("YoutubeSearch: return ", ytVideos.size.toString())
        return ytVideos
    }
}