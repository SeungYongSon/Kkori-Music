package com.tails.presentation.ui.search.adapter

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tails.domain.entity.VideoMeta
import com.tails.presentation.KkoriApplication
import com.tails.presentation.R
import com.tails.presentation.ui.search.adapter.diff.VideoMetaDiffCallback
import kotlinx.android.synthetic.main.item_music_list.view.*

class MusicListAdapter(private val kkoriApplication: KkoriApplication) :
    RecyclerView.Adapter<MusicListAdapter.SearchViewHolder>() {

    private val list = ArrayList<VideoMeta>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view =
            if (viewType == 1) LayoutInflater.from(parent.context).inflate(R.layout.item_music_list, parent, false)
            else LayoutInflater.from(parent.context).inflate(R.layout.item_music_loading_list, parent, false)

        return SearchViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int =
        if (list[position].channelId.isNotEmpty()) 1 else 0

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        if (getItemViewType(position) == 1) {
            Glide.with(holder.itemView).load(list[position].getThumbUrl()).into(holder.itemView.image)
            holder.itemView.title.text = list[position].title
            holder.itemView.uploader.text = list[position].author
            holder.itemView.setOnClickListener {
                kkoriApplication.prepare(list[position])
            }
        }
    }

    fun add(videoMeta: VideoMeta) {
        val newList = ArrayList<VideoMeta>().apply {
            addAll(this@MusicListAdapter.list)
            if (size - 1 > -1) add(size - 1, videoMeta)
            else add(videoMeta)
        }
        updateList(newList)
    }

    fun loading() {
        val newList = ArrayList<VideoMeta>().apply {
            addAll(this@MusicListAdapter.list)
            add(VideoMeta())
        }
        updateList(newList)
    }

    fun removeLoading() {
        val newList = ArrayList<VideoMeta>().apply {
            addAll(this@MusicListAdapter.list)
            removeAt(size - 1)
        }
        updateList(newList)
    }

    fun remove() = updateList(ArrayList())

    private fun updateList(newList: List<VideoMeta>) {
        val callback = VideoMetaDiffCallback(this.list, newList)
        val diffResult = DiffUtil.calculateDiff(callback)
        this.list.clear()
        this.list.addAll(newList)
        Handler(Looper.getMainLooper()).post { diffResult.dispatchUpdatesTo(this@MusicListAdapter) }
    }

    inner class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view)
}