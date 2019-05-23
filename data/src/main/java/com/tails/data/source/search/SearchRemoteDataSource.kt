package com.tails.data.source.search

import com.google.api.services.youtube.model.SearchListResponse
import com.tails.data.remote.parse.VideoMetaParser
import com.tails.data.remote.search.YouTubeSearcher
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Response
import javax.inject.Inject

class SearchRemoteDataSource @Inject constructor(
    private val youTubeSearcher: YouTubeSearcher,
    private val videoMetaParser: VideoMetaParser
) {

    fun searchList(keyword: String): Single<SearchListResponse> =
        youTubeSearcher.search(keyword)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun searchList(keyword: String, token: String): Single<SearchListResponse> =
        youTubeSearcher.search(keyword, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun searchResultParse(videoId: String): Single<Response> =
        videoMetaParser.parse(videoId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}