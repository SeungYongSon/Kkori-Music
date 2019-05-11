package com.tails.presentation.ui.adapter

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tails.domain.entities.VideoMeta
import com.tails.presentation.R
import com.tails.presentation.streaming.controller.MusicStreamingController
import com.tails.presentation.ui.adapter.diff.VideoMetaDiffCallback

class MusicListAdapter : RecyclerView.Adapter<MusicListAdapter.SearchViewHolder>() {

    var list = ArrayList<VideoMeta>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_music_list, parent, false)
        return SearchViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        Glide.with(holder.itemView).load(list[position].getThumbUrl()).into(holder.image)
        holder.title.text = list[position].title
        holder.uploader.text = list[position].author
        holder.itemView.setOnClickListener {
            if (!MusicStreamingController.isPreparing)
                MusicStreamingController.prepare(list[position], holder.itemView.context)
            else
                Toast.makeText(holder.itemView.context, "로딩중...", Toast.LENGTH_SHORT).show()
        }
    }

    fun add(videoMeta: VideoMeta) {
        val newList = ArrayList<VideoMeta>().apply {
            addAll(this@MusicListAdapter.list)
            add(videoMeta)
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

    inner class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = itemView.findViewById(R.id.image)
        val title: TextView = itemView.findViewById(R.id.title)
        val uploader: TextView = itemView.findViewById(R.id.uploader)
    }
}