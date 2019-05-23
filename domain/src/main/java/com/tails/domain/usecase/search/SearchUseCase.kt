package com.tails.domain.usecase.search

import com.tails.domain.entity.SearchResult
import com.tails.domain.repository.SearchRepository
import com.tails.domain.usecase.UseCase
import io.reactivex.Single

class SearchUseCase(private val searchRepository: SearchRepository) :
    UseCase<SearchUseCase.Params, Single<SearchResult>>() {

    override fun createObservable(params: Params): Single<SearchResult> =
        if (params.nextPageToken.isEmpty()) searchRepository.searchList(params.keyword)
        else searchRepository.searchList(params.keyword, params.nextPageToken)

    override fun onCleared() {}

    data class Params(val keyword: String, val nextPageToken: String) {
        constructor(keyword: String) : this(keyword, "")
    }
}