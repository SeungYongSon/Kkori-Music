package com.tails.data.remote.search

object SearchConfig {
    internal const val YOUTUBE_REGEX = "(?:[?&]vi?=|\\/embed\\/|\\/\\d\\d?\\/|\\/vi?\\/|https?:\\/\\/(?:www\\.)?youtu\\.be\\/)([A-Za-z0-9_\\-]{11})"

    internal const val YOUTUBE_SEARCH_LIST_TYPE = "video"
    internal const val YOUTUBE_SEARCH_LIST_PART = "id,snippet"
    internal const val YOUTUBE_SEARCH_LIST_FIELDS = "nextPageToken,items(id/videoId)"
    internal const val YOUTUBE_SEARCH_LIST_ORDERS = "viewCount"

    internal const val YOUTUBE_LANGUAGE_KEY = "hl"
    internal const val YOUTUBE_MAX_RESULTS = 50L

    internal const val YOUTUBE_VIDEO_PART = "id,snippet"
    internal const val YOUTUBE_VIDEO_FIELDS = "items(id)"
}