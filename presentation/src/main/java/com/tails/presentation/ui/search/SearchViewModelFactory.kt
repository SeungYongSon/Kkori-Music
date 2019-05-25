package com.tails.presentation.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tails.domain.usecase.search.SearchResultParseUseCase
import com.tails.domain.usecase.search.SearchUseCase
import io.reactivex.disposables.CompositeDisposable

open class SearchViewModelFactory(
    private val searchUseCase: SearchUseCase,
    private val searchResultParseUseCase: SearchResultParseUseCase,
    private val compositeDisposable: CompositeDisposable
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        modelClass.getConstructor(
            SearchUseCase::class.java,
            SearchResultParseUseCase::class.java,
            CompositeDisposable::class.java
        ).newInstance(searchUseCase, searchResultParseUseCase, compositeDisposable)
}