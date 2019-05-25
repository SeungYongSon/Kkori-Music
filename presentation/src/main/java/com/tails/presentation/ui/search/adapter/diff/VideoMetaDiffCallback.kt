package com.tails.presentation.ui.search.adapter.diff

import androidx.recyclerview.widget.DiffUtil
import com.tails.domain.entity.VideoMeta


class VideoMetaDiffCallback(private val oldList: List<VideoMeta>, private val newList: List<VideoMeta>) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size


    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].videoId === newList[newItemPosition].videoId

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].videoId === newList[newItemPosition].videoId
}