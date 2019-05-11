package com.tails.data.remote.search

interface SearchComplete {
    fun onSearchComplete(result: List<String>?, nextPageToken: String?)
}