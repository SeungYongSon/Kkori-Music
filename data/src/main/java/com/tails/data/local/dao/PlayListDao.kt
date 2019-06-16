package com.tails.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.tails.data.model.PlayListEntity
import com.tails.data.model.VideoMetaEntity

@Dao
interface PlayListDao {

    @Insert
    fun insert(playListEntity: PlayListEntity)

    @Query(value = "SELECT * FROM playList")
    fun getAll(): List<PlayListEntity>

//    @Query("SELECT * FROM playList where videoMetaEntityList = :playListId")
//    fun getIdForList(playListId: String): List<VideoMetaEntity>

    @Delete
    fun delete(playListEntity: PlayListEntity)

}