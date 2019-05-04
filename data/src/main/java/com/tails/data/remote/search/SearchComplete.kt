package com.tails.data.remote.search

import com.tails.domain.entities.YtVideo

interface SearchComplete {
    fun onSearchComplete(result: List<YtVideo>, nextPageToken: String)
}