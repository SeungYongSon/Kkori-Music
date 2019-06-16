package com.tails.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tails.data.local.dao.PlayListDao
import com.tails.data.model.PlayListEntity

@Database(entities = [PlayListEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playListDao() : PlayListDao
}