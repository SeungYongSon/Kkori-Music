package com.tails.presentation.di.module

import com.tails.data.model.SearchResultMapper
import com.tails.data.repository.SearchRepositoryImp
import com.tails.domain.repository.SearchRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RepositoryModule {

    @Provides
    @Singleton
    fun provideSearchResultMapper(): SearchResultMapper = SearchResultMapper()

    @Provides
    @Singleton
    fun provideSearchRepository(searchRepository: SearchRepositoryImp): SearchRepository = searchRepository

}