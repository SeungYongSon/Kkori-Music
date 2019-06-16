package com.tails.domain.entity

data class PlayList(
    val id: String,
    val list: ArrayList<VideoMeta>
) : Model()

