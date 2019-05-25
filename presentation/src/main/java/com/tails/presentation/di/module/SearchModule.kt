package com.tails.presentation.di.module

import com.tails.data.repository.SearchRepositoryImp
import com.tails.domain.usecase.search.SearchResultParseUseCase
import com.tails.domain.usecase.search.SearchUseCase
import com.tails.presentation.ui.search.SearchViewModelFactory
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Singleton

@Module
class SearchModule {

    @Provides
    @Singleton
    fun provideSearchUseCase(searchRepository: SearchRepositoryImp): SearchUseCase =
        SearchUseCase(searchRepository)

    @Provides
    @Singleton
    fun provideSearchResultParseUseCase(searchRepository: SearchRepositoryImp): SearchResultParseUseCase =
        SearchResultParseUseCase(searchRepository)

    @Provides
    @Singleton
    fun provideSearchViewModelFactory(
        searchUseCase: SearchUseCase,
        searchResultParseUseCase: SearchResultParseUseCase,
        compositeDisposable: CompositeDisposable
    ): SearchViewModelFactory =
        SearchViewModelFactory(searchUseCase, searchResultParseUseCase, compositeDisposable)

}