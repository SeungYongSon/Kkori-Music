package com.tails.presentation.di.module

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Singleton

@Module
class AppModule {

    @Singleton
    @Provides
    fun bindContext(application: Application): Context = application


    @Singleton
    @Provides
    fun bindCompositeDisposable(): CompositeDisposable = CompositeDisposable()

}