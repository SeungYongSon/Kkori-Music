package com.tails.data.remote.search

import android.os.AsyncTask
import android.util.Log
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import java.io.IOException
import java.util.*

class YouTubeSearcher(private val searchComplete: SearchComplete) : AsyncTask<String, Void, List<String>>() {

    companion object {
        private val youtube = YouTube.Builder(
            NetHttpTransport(),
            JacksonFactory()
        ) {}.setApplicationName("Kkori Music").build()
    }

    private var searchList: YouTube.Search.List =
        youtube.search().list(SearchConfig.YOUTUBE_SEARCH_LIST_PART).let {
            it.key = SearchConfig.YOUTUBE_API
            it.type = SearchConfig.YOUTUBE_SEARCH_LIST_TYPE
            it.maxResults = SearchConfig.YOUTUBE_MAX_RESULTS
            it.fields = SearchConfig.YOUTUBE_SEARCH_LIST_FIELDS
            it.set(SearchConfig.YOUTUBE_LANGUAGE_KEY, Locale.getDefault().language)
        }

    private lateinit var keywords: String
    private lateinit var nextPageToken: String

    fun search(keywords: String) {
        this.keywords = keywords
        this.nextPageToken = ""
        this.execute()
    }

    fun search(keywords: String, token: String) {
        this.keywords = keywords
        this.nextPageToken = token
        this.execute()
    }

    override fun doInBackground(vararg params: String?): List<String>? {
        try {
            return searchVideos()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun onPostExecute(result: List<String>?) {
        super.onPostExecute(result)
        if (result != null) searchComplete.onSearchComplete(result, nextPageToken)
    }

    private fun searchVideos(): List<String> {
        val ytVideos = ArrayList<String>()
        try {
            searchList.q = keywords

            if (nextPageToken.isNotEmpty()) {
                searchList.pageToken = nextPageToken
            }

            val searchResp = searchList.execute()
            val searchResults = searchResp.items

            if (searchResp.nextPageToken != null)
                this.nextPageToken = searchResp.nextPageToken

            searchResults.forEach {
                if (it.id != null) {
                    ytVideos.add(it.id.videoId)
                }
            }
        } catch (e: IOException) {
            Log.e("YouTubeSearcher", "Could not initialize: $e")
            e.printStackTrace()
        }
        return ytVideos
    }
}