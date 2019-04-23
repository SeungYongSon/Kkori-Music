package com.tails.domain.entities

data class VideoMeta(
    val videoId: String?,
    val title: String?,
    val author: String?,
    val channelId: String?,
    val videoLength: Long,
    val viewCount: Long,
    val isLiveStream: Boolean
) {
    companion object {
        private const val IMAGE_BASE_URL = "http://i.ytimg.com/vi/"
    }

    // 120 x 90
    fun getThumbUrl(): String = "$IMAGE_BASE_URL$videoId/default.jpg"


    // 320 x 180
    fun getMqImageUrl(): String = "$IMAGE_BASE_URL$videoId/mqdefault.jpg"

    // 480 x 360
    fun getHqImageUrl(): String = "$IMAGE_BASE_URL$videoId/hqdefault.jpg"

    // 640 x 480
    fun getSdImageUrl(): String = "$IMAGE_BASE_URL$videoId/sddefault.jpg"

    // Max Res
    fun getMaxResImageUrl(): String = "$IMAGE_BASE_URL$videoId/maxresdefault.jpg"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val videoMeta = other as VideoMeta?

        if (videoLength != videoMeta!!.videoLength) return false
        if (viewCount != videoMeta.viewCount) return false
        if (isLiveStream != videoMeta.isLiveStream) return false
        if (if (videoId != null) videoId != videoMeta.videoId else videoMeta.videoId != null)
            return false
        if (if (title != null) title != videoMeta.title else videoMeta.title != null) return false
        if (if (author != null) author != videoMeta.author else videoMeta.author != null)
            return false
        return if (channelId != null) channelId == videoMeta.channelId else videoMeta.channelId == null
    }

    override fun hashCode(): Int {
        var result = videoId?.hashCode() ?: 0
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (author?.hashCode() ?: 0)
        result = 31 * result + (channelId?.hashCode() ?: 0)
        result = 31 * result + (videoLength xor videoLength.ushr(32)).toInt()
        result = 31 * result + (viewCount xor viewCount.ushr(32)).toInt()
        result = 31 * result + if (isLiveStream) 1 else 0
        return result
    }

    override fun toString(): String = "VideoMeta{" +
            "videoId='" + videoId + '\''.toString() +
            ", title='" + title + '\''.toString() +
            ", author='" + author + '\''.toString() +
            ", channelId='" + channelId + '\''.toString() +
            ", videoLength=" + videoLength +
            ", viewCount=" + viewCount +
            ", isLiveStream=" + isLiveStream +
            '}'.toString()
}