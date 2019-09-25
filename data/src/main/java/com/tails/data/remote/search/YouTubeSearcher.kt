package com.tails.data.remote.search

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.GenericJson
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.tails.data.BuildConfig
import io.reactivex.Single
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

class YouTubeSearcher @Inject constructor() {

    companion object {
        private val youtube = YouTube.Builder(
            NetHttpTransport(),
            JacksonFactory()
        ) {}.setApplicationName("Kkori Music").build()

        private var searchList: YouTube.Search.List =
            youtube.search().list(SearchConfig.YOUTUBE_SEARCH_LIST_PART).let {
                it.key = BuildConfig.YOUTUBE_API
                it.type = SearchConfig.YOUTUBE_SEARCH_LIST_TYPE
                it.maxResults = SearchConfig.YOUTUBE_MAX_RESULTS
                it.fields = SearchConfig.YOUTUBE_SEARCH_LIST_FIELDS
                it.set(SearchConfig.YOUTUBE_LANGUAGE_KEY, Locale.getDefault().language)
            }
    }

    private lateinit var keywords: String
    private lateinit var nextPageToken: String

    fun search(keywords: String): Single<GenericJson> {
        this.keywords = keywords
        this.nextPageToken = ""
        return searchVideos()
    }

    fun search(keywords: String, token: String): Single<GenericJson> {
        this.keywords = keywords
        this.nextPageToken = token
        return searchVideos()
    }

    private fun searchVideos(): Single<GenericJson> {
        searchList.q = keywords

        val pattern = Pattern.compile(SearchConfig.YOUTUBE_REGEX)
        val matcher = pattern.matcher(keywords)

        if (nextPageToken.isNotEmpty()) {
            searchList.pageToken = nextPageToken
        }

        return if (matcher.find()) {

            val singleVideo = youtube.videos().list(SearchConfig.YOUTUBE_VIDEO_PART).apply {
                key = BuildConfig.YOUTUBE_API
                fields = SearchConfig.YOUTUBE_VIDEO_FIELDS
                set(SearchConfig.YOUTUBE_LANGUAGE_KEY, Locale.getDefault().language)
                id = matcher.group(1)
            }

            Single.fromCallable { singleVideo.execute() }
        } else {
            Single.fromCallable { searchList.execute() }
        }
    }
}