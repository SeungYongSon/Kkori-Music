package com.tails.data.repository

import com.tails.domain.entities.YtVideo
import com.tails.domain.repositories.YtVideoRepository

class YtVideoRepositoryImp : YtVideoRepository {

    override fun getYtVideos(): List<YtVideo>? {
        return null
    }

    override fun getLocalYtVideos(): List<YtVideo>? {
        return null
    }

    override fun getRemoteYtVideos(): List<YtVideo>? {
        return null
    }
}