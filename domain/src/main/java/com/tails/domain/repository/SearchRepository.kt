package com.tails.domain.repository

import com.tails.domain.entity.SearchResult
import com.tails.domain.entity.VideoMeta
import io.reactivex.Single

interface SearchRepository {

    fun searchList(keyword: String): Single<SearchResult>
    fun searchList(keyword: String, nextPageToken: String): Single<SearchResult>
    fun searchResultParse(videoId: String): Single<VideoMeta>

}