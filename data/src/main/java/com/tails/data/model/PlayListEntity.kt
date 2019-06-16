package com.tails.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.tails.data.local.Converters
import com.tails.domain.entity.PlayList
import com.tails.domain.entity.VideoMeta
import java.io.Serializable
import javax.inject.Inject

@Entity(tableName = "playList")
class PlayListEntity(
    @PrimaryKey
    @ColumnInfo(name = "playListId")
    var playListId: String,
    @TypeConverters(Converters::class)
    @ColumnInfo(name = "videoMetaEntityList")
    var videoMetaEntityList: ArrayList<VideoMetaEntity>
) : ModelEntity(), Serializable

class PlayListMapper @Inject constructor(private val videoMetaMapper: VideoMetaMapper) :
    EntityMapper<PlayList, PlayListEntity> {

    override fun mapToDomain(entity: PlayListEntity): PlayList {
        val videoMetaList = ArrayList<VideoMeta>()
        entity.videoMetaEntityList.forEach { videoMetaList.add(videoMetaMapper.mapToDomain(it)) }

        return PlayList(entity.playListId, videoMetaList)
    }

    override fun mapToEntity(model: PlayList): PlayListEntity {
        val videoMetaEntityList = ArrayList<VideoMetaEntity>()
        model.list.forEach { videoMetaEntityList.add(videoMetaMapper.mapToEntity(it)) }

        return PlayListEntity(model.id, videoMetaEntityList)
    }
}