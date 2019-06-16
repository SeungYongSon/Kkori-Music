package com.tails.domain.usecase.extract

import com.tails.domain.repository.ExtractRepository
import com.tails.domain.usecase.UseCase
import io.reactivex.Observable

class ExtractStreamingUrlUseCase(private val extractRepository: ExtractRepository) :
    UseCase<ExtractStreamingUrlUseCase.Params, Observable<String>>() {

    override fun createObservable(params: Params): Observable<String> =
        extractRepository.extractStreamingUrl(params.videoId)

    override fun onCleared() {}

    data class Params(val videoId: String)
}