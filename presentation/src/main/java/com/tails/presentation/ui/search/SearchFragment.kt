package com.tails.presentation.ui.search

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.tails.domain.entity.SearchResult
import com.tails.domain.usecase.search.SearchResultParseUseCase
import com.tails.domain.usecase.search.SearchUseCase
import com.tails.presentation.R
import com.tails.presentation.ui.MainActivity
import com.tails.presentation.ui.adapter.MusicListAdapter
import com.tails.presentation.ui.adapter.scroll.EndlessRecyclerOnScrollListener
import com.tails.presentation.ui.adapter.wrapper.LinearLayoutManagerWrapper
import com.tails.presentation.ui.base.BaseFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_search.*
import javax.inject.Inject

class SearchFragment : BaseFragment() {

    override val layoutId: Int
        get() = R.layout.fragment_search

    private var searchCount = 0
    private var nextPageToken = ""
    private var lastSearch = ""
    private var isLoading = false

    @Inject
    lateinit var searchUseCase: SearchUseCase
    @Inject
    lateinit var searchResultParseUseCase: SearchResultParseUseCase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).apply {
            playerBehavior.peekHeight =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56f, resources.displayMetrics).toInt()
            bottomNavigationView.visibility = View.GONE

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(s: String): Boolean {
                    if (s.isNotEmpty()) {
                        if (lastSearch != s) {
                            lastSearch = s
                            searchCount = 0
                            nextPageToken = ""
                            isLoading = true
                            compositeDisposable.clear()
                            search_progress.visibility = View.VISIBLE
                            search_list.visibility = View.INVISIBLE
                            (search_list.adapter as MusicListAdapter).remove()
                            search_list.addOnScrollListener(endlessRecyclerOnScrollListener)
                            search(s)
                        }
                    }
                    return false
                }

                override fun onQueryTextChange(s: String): Boolean = false
            })
        }

        resultList = search_list

        search_list.adapter = MusicListAdapter()
        search_list.layoutManager = LinearLayoutManagerWrapper(
            context!!,
            LinearLayoutManager.VERTICAL,
            false
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        resultList = null
        (activity as MainActivity).apply {
            appBarLayout.setExpanded(true, true)
            playerBehavior.peekHeight =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 112f, resources.displayMetrics).toInt()
            bottomNavigationView.visibility = View.VISIBLE
        }
    }

    private val endlessRecyclerOnScrollListener = object : EndlessRecyclerOnScrollListener() {
        override fun onLoadMore() {
            if (nextPageToken != "" && searchCount == 0 && !isLoading) {
                search(lastSearch, nextPageToken)
                nextPageToken = ""
                isLoading = true
                (search_list.adapter as MusicListAdapter).loading()
            }
        }
    }

    fun search(keyword: String) {
        compositeDisposable.add(
            searchUseCase.createObservable(SearchUseCase.Params(keyword))
                .subscribe({ searchSuccess(it) }, {
                    Log.e("asdf", it.message)
                    searchFail()
                })
        )
    }

    fun search(keyword: String, nextPageToken: String) {
        compositeDisposable.add(
            searchUseCase.createObservable(SearchUseCase.Params(keyword, nextPageToken))
                .subscribe({ searchSuccess(it) }, { searchFail() })
        )
    }

    private fun searchSuccess(result: SearchResult) {
        if (result.nextPageToken.isNotEmpty()) this.nextPageToken = result.nextPageToken

        if(result.resultList.isNotEmpty()) {
            searchCount = result.resultList.size

            result.resultList.forEach { id ->
                compositeDisposable.add(
                    searchResultParseUseCase.createObservable(
                        SearchResultParseUseCase.Params(id)
                    ).subscribe({ video ->
                        searchCount--

                        activity?.runOnUiThread {
                            (search_list.adapter as MusicListAdapter).add(video)

                            if (searchCount == 0) {
                                (search_list.adapter as MusicListAdapter).removeLoading()
                                isLoading = false
                            }

                            if (search_progress.visibility == View.VISIBLE)
                                search_progress.visibility = View.INVISIBLE
                            if (search_list.visibility == View.INVISIBLE)
                                search_list.visibility = View.VISIBLE
                        }
                    }, {
                        searchCount--
                        if (searchCount == 0) {
                            (search_list.adapter as MusicListAdapter).removeLoading()
                            isLoading = false
                        }
                    })
                )
            }
        } else {
            if (search_progress.visibility == View.VISIBLE)
                search_progress.visibility = View.INVISIBLE
            if (search_list.visibility == View.INVISIBLE)
                search_list.visibility = View.VISIBLE

            Toast.makeText(context, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchFail() {
        activity?.runOnUiThread {
            if (search_progress.visibility == View.VISIBLE)
                search_progress.visibility = View.INVISIBLE

            isLoading = false

            Toast.makeText(context, "검색 결과가 없거나\nAPI 호출 횟수가 초과 되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}