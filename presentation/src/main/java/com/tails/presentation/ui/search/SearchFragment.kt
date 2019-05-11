package com.tails.presentation.ui.search

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tails.data.remote.VideoMetaParser
import com.tails.data.remote.search.SearchComplete
import com.tails.data.remote.search.YouTubeSearcher
import com.tails.presentation.R
import com.tails.presentation.ui.MainActivity
import com.tails.presentation.ui.adapter.MusicListAdapter
import com.tails.presentation.ui.adapter.scroll.EndlessRecyclerOnScrollListener
import com.tails.presentation.ui.adapter.wrapper.LinearLayoutManagerWrapper
import com.tails.presentation.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_search.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/*
TODO Search 예외처리(저번 검색어 리스트 추가 되는 거 막기)
TODO 플레이 일 때, 리스트 위로 올리기
 */
class SearchFragment : BaseFragment(), SearchComplete {

    override val layoutId: Int
        get() = R.layout.fragment_search

    lateinit var searchList : RecyclerView

    private var searchCount = 0
    private var nextPageToken = ""
    private var lastSearch = ""


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchList = search_list

        search_list.adapter = MusicListAdapter()
        search_list.layoutManager = LinearLayoutManagerWrapper(
            context!!,
            LinearLayoutManager.VERTICAL,
            false
        )

        (activity as MainActivity).searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                if (s.isNotEmpty()) {
                    if (lastSearch != s) {
                        (search_list.adapter as MusicListAdapter).remove()
                        YouTubeSearcher(this@SearchFragment).search(s)
                        search_progress.visibility = View.VISIBLE
                        search_list.addOnScrollListener(endlessRecyclerOnScrollListener)
                        lastSearch = s
                        searchCount = 0
                    }
                }
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                return false
            }
        })
    }

    override fun onSearchComplete(result: List<String>?, nextPageToken: String?) {
        if (nextPageToken != null)
            this.nextPageToken = nextPageToken

        result?.forEach {
            val reqVideoInfo = Request.Builder()
                .url("https://www.youtube.com/get_video_info?video_id=$it&eurl=https://youtube.googleapis.com/v/$it")
                .addHeader("User-Agent", VideoMetaParser.USER_AGENT)
                .build()

            VideoMetaParser.client.newCall(reqVideoInfo).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                override fun onResponse(call: Call, response: Response) {
                    val streamMap = VideoMetaParser.resBodyToStream(response.body()!!)

                    searchCount++

                    activity?.runOnUiThread {
                        (search_list.adapter as MusicListAdapter).add(VideoMetaParser.parseVideoMeta(streamMap, it))
                        if (search_progress.visibility == View.VISIBLE)
                            search_progress.visibility = View.INVISIBLE
                    }
                }
            })
        }
    }

    private val endlessRecyclerOnScrollListener = object : EndlessRecyclerOnScrollListener() {
        override fun onLoadMore() {
            Log.e("asdf", searchCount.toString())
            if (nextPageToken != "" && searchCount % 50 == 0) {
                YouTubeSearcher(this@SearchFragment).search(lastSearch, nextPageToken)
                nextPageToken = ""
            }
        }
    }
}