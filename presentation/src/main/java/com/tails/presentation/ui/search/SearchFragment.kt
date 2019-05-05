package com.tails.presentation.ui.search

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.tails.data.remote.search.SearchComplete
import com.tails.data.remote.VideoMetaParser
import com.tails.data.remote.search.YouTubeSearcher
import com.tails.presentation.R
import com.tails.presentation.ui.MainActivity
import com.tails.presentation.ui.adapter.list.diff.LinearLayoutManagerWrapper
import com.tails.presentation.ui.adapter.list.MusicListAdapter
import com.tails.presentation.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_search.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class SearchFragment : BaseFragment(), SearchComplete {

    override val layoutId: Int
        get() = R.layout.fragment_search

    private lateinit var nextPageToken: String
    private var lastSearch: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                if(s.isNotEmpty()) {
                    if (lastSearch != s) (search_list.adapter as MusicListAdapter).remove()
                    YouTubeSearcher(this@SearchFragment).search(s)
                }
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                return false
            }
        })

        search_list.adapter = MusicListAdapter()
        search_list.layoutManager = LinearLayoutManagerWrapper(
            context!!,
            LinearLayoutManager.VERTICAL,
            false
        )

    }

    override fun onSearchComplete(result: List<String>, nextPageToken: String) {
        this.nextPageToken = nextPageToken
        result.forEach {
            val reqVideoInfo = Request.Builder()
                .url("https://www.youtube.com/get_video_info?video_id=$it&eurl=https://youtube.googleapis.com/v/$it")
                .addHeader("User-Agent", VideoMetaParser.USER_AGENT)
                .build()

            VideoMetaParser.client.newCall(reqVideoInfo).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                override fun onResponse(call: Call, response: Response) {
                    val streamMap = VideoMetaParser.resBodyToStream(response.body()!!)
                    activity?.runOnUiThread {
                        (search_list.adapter as MusicListAdapter).add(VideoMetaParser.parseVideoMeta(streamMap, it))
                    }
                }
            })
        }
    }
}