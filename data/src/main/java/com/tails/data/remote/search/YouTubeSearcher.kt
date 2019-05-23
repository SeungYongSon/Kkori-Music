package com.tails.data.remote.search

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.SearchListResponse
import io.reactivex.Single
import java.util.*
import javax.inject.Inject

class YouTubeSearcher @Inject constructor() {

    companion object {
        private val youtube = YouTube.Builder(
            NetHttpTransport(),
            JacksonFactory()
        ) {}.setApplicationName("Kkori Music").build()

        private var searchList: YouTube.Search.List =
            youtube.search().list(SearchConfig.YOUTUBE_SEARCH_LIST_PART).let {
                it.key = YOUTUBE_API
                it.type = SearchConfig.YOUTUBE_SEARCH_LIST_TYPE
                it.maxResults = SearchConfig.YOUTUBE_MAX_RESULTS
                it.fields = SearchConfig.YOUTUBE_SEARCH_LIST_FIELDS
                it.order = SearchConfig.YOUTUBE_SEARCH_LIST_ORDERS
                it.set(SearchConfig.YOUTUBE_LANGUAGE_KEY, Locale.getDefault().language)
            }
    }

    private lateinit var keywords: String
    private lateinit var nextPageToken: String

    fun search(keywords: String): Single<SearchListResponse> {
        this.keywords = keywords
        this.nextPageToken = ""
        return searchVideos()
    }

    fun search(keywords: String, token: String): Single<SearchListResponse> {
        this.keywords = keywords
        this.nextPageToken = token
        return searchVideos()
    }

    private fun searchVideos(): Single<SearchListResponse> {
        searchList.q = keywords

        if (nextPageToken.isNotEmpty()) {
            searchList.pageToken = nextPageToken
        }

        return Single.fromCallable { searchList.execute() }
    }
}