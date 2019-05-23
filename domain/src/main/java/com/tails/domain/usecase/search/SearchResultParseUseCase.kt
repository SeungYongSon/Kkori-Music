package com.tails.domain.usecase.search

import com.tails.domain.entity.VideoMeta
import com.tails.domain.repository.SearchRepository
import com.tails.domain.usecase.UseCase
import io.reactivex.Single

class SearchResultParseUseCase(private val searchRepository: SearchRepository) :
    UseCase<SearchResultParseUseCase.Params, Single<VideoMeta>>() {

    override fun createObservable(params: Params): Single<VideoMeta> =
        searchRepository.searchResultParse(params.videoId)

    override fun onCleared() {}

    data class Params(val videoId: String)
}