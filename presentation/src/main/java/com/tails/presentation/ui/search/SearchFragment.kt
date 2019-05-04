package com.tails.presentation.ui.search

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.tails.data.remote.search.SearchComplete
import com.tails.data.remote.search.YouTubeSearcher
import com.tails.domain.entities.YtVideo
import com.tails.presentation.R
import com.tails.presentation.ui.MainActivity
import com.tails.presentation.ui.adapter.list.MusicListAdapter
import com.tails.presentation.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : BaseFragment(), SearchComplete {


    override val layoutId: Int
        get() = R.layout.fragment_search

    lateinit var nextPageToken: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                YouTubeSearcher(this@SearchFragment).search(s)
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                return false
            }
        })

        search_list.adapter = MusicListAdapter()
        search_list.layoutManager = LinearLayoutManager(context)
    }

    override fun onSearchComplete(result: List<YtVideo>, nextPageToken: String) {
        this.nextPageToken = nextPageToken
        (search_list.adapter as MusicListAdapter).add(result)
    }
}