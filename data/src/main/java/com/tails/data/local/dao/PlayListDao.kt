package com.tails.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.tails.data.model.PlayListEntity

@Dao
interface PlayListDao {

    @Insert
    fun insert(playListEntity: PlayListEntity)

    @Query(value = "SELECT * FROM playList")
    fun getAll(): List<PlayListEntity>

    @Delete
    fun delete(playListEntity: PlayListEntity)

}