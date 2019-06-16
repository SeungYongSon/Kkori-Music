package com.tails.domain.repository

import io.reactivex.Observable

interface ExtractRepository {

    fun extractStreamingUrl(videoId: String): Observable<String>

}