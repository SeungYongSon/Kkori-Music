package com.tails.domain.repositories

import com.tails.domain.entities.YtVideo

interface YtVideoRepository {

    fun getYtVideos(): List<YtVideo>
    fun getLocalYtVideos(): List<YtVideo>
    fun getRemoteYtVideos(): List<YtVideo>

}