package com.tails.data.remote.search

object SearchConfig {
    const val YOUTUBE_API = "INPUT_YOUR_YouTube_Data_API_KEY"

    const val YOUTUBE_SEARCH_LIST_TYPE = "video"
    const val YOUTUBE_SEARCH_LIST_PART = "id,snippet"
    const val YOUTUBE_SEARCH_LIST_FIELDS = "pageInfo,nextPageToken,items(id/videoId,snippet/title,snippet/thumbnails/default/url)"

    const val YOUTUBE_LANGUAGE_KEY = "hl"
    const val YOUTUBE_MAX_RESULTS = 50L
}