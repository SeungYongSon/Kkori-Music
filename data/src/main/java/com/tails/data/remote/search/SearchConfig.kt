package com.tails.data.remote.search

object SearchConfig {
   internal const val YOUTUBE_API = "INPUT_YOUR_YouTube_Data_API_KEY"

    internal const val YOUTUBE_SEARCH_LIST_TYPE = "video"
    internal const val YOUTUBE_SEARCH_LIST_PART = "id,snippet"
    internal const val YOUTUBE_SEARCH_LIST_FIELDS = "nextPageToken,items(id/videoId)"

    internal const val YOUTUBE_LANGUAGE_KEY = "hl"
    internal const val YOUTUBE_MAX_RESULTS = 50L
}