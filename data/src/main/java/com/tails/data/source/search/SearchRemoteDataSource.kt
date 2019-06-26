package com.tails.data.source.search

import com.google.api.services.youtube.model.SearchListResponse
import com.google.api.services.youtube.model.VideoListResponse
import com.tails.data.model.SearchResultEntity
import com.tails.data.model.SearchResultMapper
import com.tails.data.model.VideoMetaMapper
import com.tails.data.remote.parse.VideoMetaParseUtil
import com.tails.data.remote.parse.VideoMetaParser
import com.tails.data.remote.search.SearchConfig
import com.tails.data.remote.search.YouTubeSearcher
import com.tails.domain.entity.SearchResult
import com.tails.domain.entity.VideoMeta
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.regex.Pattern
import javax.inject.Inject

class SearchRemoteDataSource @Inject constructor(
    private val youTubeSearcher: YouTubeSearcher,
    private val videoMetaParser: VideoMetaParser,
    private val searchResultMapper: SearchResultMapper,
    private val videoMetaMapper: VideoMetaMapper
) {

    fun searchList(keyword: String): Single<SearchResult> =
        youTubeSearcher.search(keyword)
            .map {
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
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun searchList(keyword: String, token: String): Single<SearchResult> =
        youTubeSearcher.search(keyword, token)
            .map {
                val searchListResponse = it as SearchListResponse

                val nextToken =
                    if (searchListResponse.nextPageToken != null) searchListResponse.nextPageToken
                    else ""

                val result = ArrayList<String>().apply {
                    searchListResponse.items.forEach { items ->
                        this.add(items.id.videoId)
                    }
                }

                searchResultMapper.mapToDomain(SearchResultEntity(result, nextToken))
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun searchResultParse(videoId: String): Single<VideoMeta> =
        videoMetaParser.parse(videoId)
            .map {
                val info = VideoMetaParseUtil.resBodyToStream(it.body()!!)
                videoMetaMapper.mapToDomain(VideoMetaParseUtil.parseVideoMeta(info, videoId))
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

}