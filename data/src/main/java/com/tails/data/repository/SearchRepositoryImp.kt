package com.tails.data.repository

import com.tails.data.source.search.SearchRemoteDataSource
import com.tails.domain.entity.SearchResult
import com.tails.domain.entity.VideoMeta
import com.tails.domain.repository.SearchRepository
import io.reactivex.Single
import javax.inject.Inject

class SearchRepositoryImp @Inject constructor(
    private val searchRemoteDataSource: SearchRemoteDataSource
) : SearchRepository {

    override fun searchList(keyword: String): Single<SearchResult> =
        searchRemoteDataSource.searchList(keyword)

    override fun searchList(keyword: String, nextPageToken: String): Single<SearchResult> =
        searchRemoteDataSource.searchList(keyword, nextPageToken)

    override fun searchResultParse(videoId: String): Single<VideoMeta> =
        searchRemoteDataSource.searchResultParse(videoId)

}