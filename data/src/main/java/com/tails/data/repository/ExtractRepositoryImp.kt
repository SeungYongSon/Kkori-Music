package com.tails.data.repository

import com.tails.data.source.extract.ExtractRemoteDataSource
import com.tails.domain.repository.ExtractRepository
import io.reactivex.Observable
import javax.inject.Inject

class ExtractRepositoryImp @Inject constructor(
    private val extractRemoteDataSource: ExtractRemoteDataSource
) : ExtractRepository {

    override fun extractStreamingUrl(videoId: String): Observable<String> =
        extractRemoteDataSource.extract(videoId)

}