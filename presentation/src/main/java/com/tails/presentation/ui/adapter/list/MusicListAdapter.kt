package com.tails.presentation.ui.adapter.list

import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tails.domain.entities.YtVideo
import com.tails.presentation.R
import com.tails.presentation.streaming.controller.MusicStreamingController

class MusicListAdapter : RecyclerView.Adapter<MusicListAdapter.SearchViewHolder>() {

    var list = ArrayList<YtVideo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_music_list, parent, false)
        return SearchViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        Glide.with(holder.itemView).load(list[position].thumbnailURL).into(holder.image)
        holder.title.text = list[position].title
        holder.uploader.text = list[position].viewCount
        holder.itemView.setOnClickListener {
            if(!MusicStreamingController.isPreparing)
                MusicStreamingController.prepare(list[position].id!!, holder.itemView.context)
            else
                Toast.makeText(holder.itemView.context, "로딩중...", Toast.LENGTH_SHORT).show()
        }
    }

    fun add(list: List<YtVideo>) {
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = itemView.findViewById(R.id.image)
        val title: TextView = itemView.findViewById(R.id.title)
        val uploader: TextView = itemView.findViewById(R.id.uploader)
    }
}