package com.tails.domain.entity

data class SearchResult(val resultList: List<String>,
                        val nextPageToken: String) : Model()