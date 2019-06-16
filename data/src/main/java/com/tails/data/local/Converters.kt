package com.tails.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tails.data.model.VideoMetaEntity

import java.util.ArrayList

object Converters {

    @TypeConverter
    @JvmStatic
    fun toVideoMetaEntityList(value: String): ArrayList<VideoMetaEntity> {
        val listType = object : TypeToken<ArrayList<VideoMetaEntity>>() {}.type
        return Gson().fromJson<ArrayList<VideoMetaEntity>>(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun toJson(list: ArrayList<VideoMetaEntity>): String = Gson().toJson(list)

}
