package com.tails.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tails.domain.entity.VideoMeta
import javax.inject.Inject

@Entity
data class VideoMetaEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val author: String,
    val channelId: String,
    val videoLength: Long,
    val viewCount: Long,
    val isLiveStream: Boolean,
    val infoStream: String
) : ModelEntity() {

    companion object {
        private const val IMAGE_BASE_URL = "https://i.ytimg.com/vi/"
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

        val videoMeta = other as VideoMetaEntity?

        if (videoLength != videoMeta!!.videoLength) return false
        if (viewCount != videoMeta.viewCount) return false
        if (isLiveStream != videoMeta.isLiveStream) return false
        if (videoId != videoMeta.videoId)
            return false
        if (title != videoMeta.title) return false
        if (author != videoMeta.author)
            return false
        return channelId == videoMeta.channelId
    }

    override fun hashCode(): Int {
        var result = videoId.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + channelId.hashCode()
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

class VideoMetaMapper @Inject constructor() : EntityMapper<VideoMeta, VideoMetaEntity> {

    override fun mapToDomain(entity: VideoMetaEntity): VideoMeta =
        VideoMeta(
            entity.videoId,
            entity.title,
            entity.author,
            entity.channelId,
            entity.videoLength,
            entity.viewCount,
            entity.isLiveStream,
            entity.infoStream
        )

    override fun mapToEntity(model: VideoMeta): VideoMetaEntity =
        VideoMetaEntity(
            model.videoId,
            model.title,
            model.author,
            model.channelId,
            model.videoLength,
            model.viewCount,
            model.isLiveStream,
            model.infoStream
        )

}