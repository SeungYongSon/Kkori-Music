package com.tails.data.repository

import com.google.api.services.youtube.model.SearchListResponse
import com.google.api.services.youtube.model.VideoListResponse
import com.tails.data.model.SearchResultEntity
import com.tails.data.model.SearchResultMapper
import com.tails.data.model.VideoMetaMapper
import com.tails.data.remote.parse.VideoMetaParseUtil.parseVideoMeta
import com.tails.data.remote.parse.VideoMetaParseUtil.resBodyToStream
import com.tails.data.remote.search.SearchConfig
import com.tails.data.source.search.SearchRemoteDataSource
import com.tails.domain.entity.SearchResult
import com.tails.domain.entity.VideoMeta
import com.tails.domain.repository.SearchRepository
import io.reactivex.Single
import java.util.regex.Pattern
import javax.inject.Inject

class SearchRepositoryImp @Inject constructor(
    private val searchRemoteDataSource: SearchRemoteDataSource,
    private val searchResultMapper: SearchResultMapper,
    private val videoMetaMapper: VideoMetaMapper
) : SearchRepository {

    override fun searchList(keyword: String): Single<SearchResult> =
        searchRemoteDataSource.searchList(keyword).map {
            val pattern = Pattern.compile(SearchConfig.YOUTUBE_REGEX)
            val matcher = pattern.matcher(keyword)

            if (!matcher.find()) {
                val search = (it as SearchListResponse)

                val token =
                    if (search.nextPageToken != null) search.nextPageToken
                    else ""

                val result = ArrayList<String>().apply {
                    search.items.forEach { items -> this.add(items.id.videoId) }
                }
                searchResultMapper.mapToDomain(SearchResultEntity(result, token))
            } else {
                val search = (it as VideoListResponse)

                val result = ArrayList<String>().apply {
                    search.items.forEach { items -> this.add(items.id) }
                }
                searchResultMapper.mapToDomain(SearchResultEntity(result, ""))
            }
        }

    override fun searchList(keyword: String, nextPageToken: String): Single<SearchResult> =
        searchRemoteDataSource.searchList(keyword, nextPageToken).map {
            val token =
                if (it.nextPageToken != null) it.nextPageToken
                else ""

            val result = ArrayList<String>().apply {
                it.items.forEach { items ->
                    this.add(items.id.videoId)
                }
            }
            searchResultMapper.mapToDomain(SearchResultEntity(result, token))
        }

    override fun searchResultParse(videoId: String): Single<VideoMeta> =
        searchRemoteDataSource.searchResultParse(videoId)
            .map {
                val info = resBodyToStream(it.body()!!)
                videoMetaMapper.mapToDomain(parseVideoMeta(info, videoId))
            }
}