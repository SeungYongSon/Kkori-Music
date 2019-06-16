package com.tails.presentation.di.module

import com.tails.data.repository.ExtractRepositoryImp
import com.tails.domain.repository.ExtractRepository
import com.tails.domain.usecase.extract.ExtractStreamingUrlUseCase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ExtractModule {

    @Provides
    @Singleton
    fun provideExtractRepository(extractRepository: ExtractRepositoryImp): ExtractRepository = extractRepository

    @Provides
    @Singleton
    fun provideExtractStreamingUseCase(extractRepository: ExtractRepositoryImp): ExtractStreamingUrlUseCase =
        ExtractStreamingUrlUseCase(extractRepository)

}