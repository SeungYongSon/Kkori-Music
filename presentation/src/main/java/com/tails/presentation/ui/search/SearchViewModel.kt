package com.tails.presentation.ui.search

import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModel
import com.tails.domain.entity.SearchResult
import com.tails.domain.entity.VideoMeta
import com.tails.domain.usecase.search.SearchResultParseUseCase
import com.tails.domain.usecase.search.SearchUseCase
import com.tails.presentation.ui.base.SingleLiveEvent
import com.tails.presentation.ui.search.adapter.scroll.EndlessRecyclerOnScrollListener
import io.reactivex.disposables.CompositeDisposable

class SearchViewModel(
    private val searchUseCase: SearchUseCase,
    private val searchResultParseUseCase: SearchResultParseUseCase,
    private val compositeDisposable: CompositeDisposable
) : ViewModel() {

    private var searchCount = 0
    private var nextPageToken = ""
    private var lastSearch = ""
    private var isLoading = false

    val startSearchSingleLiveEvent = SingleLiveEvent<Unit>()
    val searchFailSingleLiveEvent = SingleLiveEvent<Unit>()
    val searchNextPageLoadSingleLiveEvent = SingleLiveEvent<Unit>()
    val searchEndSingleLiveEvent = SingleLiveEvent<Unit>()
    val parseSuccessSingleLiveEvent = SingleLiveEvent<VideoMeta>()

    fun search(keyword: String) {
        compositeDisposable.add(
            searchUseCase.createObservable(SearchUseCase.Params(keyword))
                .subscribe({ searchSuccess(it) }, { searchFail() })
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

        if (result.resultList.isNotEmpty()) {
            searchCount = result.resultList.size

            result.resultList.forEach { id ->
                compositeDisposable.add(
                    searchResultParseUseCase.createObservable(
                        SearchResultParseUseCase.Params(id)
                    ).subscribe { result, _ ->
                        if (result != null) parseSuccessSingleLiveEvent.value = result

                        searchCount--

                        if (searchCount == 0) {
                            isLoading = false
                            searchEndSingleLiveEvent.call()
                        }
                    })
            }
        } else {
            searchFail()
        }
    }

    private fun searchFail() {
        isLoading = false
        searchFailSingleLiveEvent.call()
    }

    val queryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(s: String): Boolean {
            if (s.isNotEmpty()) {
                if (lastSearch != s) {
                    lastSearch = s
                    searchCount = 0
                    nextPageToken = ""
                    isLoading = true
                    compositeDisposable.clear()
                    search(s)
                    startSearchSingleLiveEvent.call()
                }
            }
            return false
        }

        override fun onQueryTextChange(s: String): Boolean = false
    }

    val endlessRecyclerOnScrollListener = object : EndlessRecyclerOnScrollListener() {
        override fun onLoadMore() {
            if (nextPageToken != "" && searchCount == 0 && !isLoading) {
                search(lastSearch, nextPageToken)
                nextPageToken = ""
                isLoading = true
                searchNextPageLoadSingleLiveEvent.call()
            }
        }
    }
}