package com.tails.presentation

import android.app.Activity
import android.util.Log
import com.tails.domain.entity.VideoMeta
import com.tails.domain.usecase.extract.ExtractStreamingUrlUseCase
import com.tails.presentation.di.DaggerAppComponent
import com.tails.presentation.streaming.controller.MusicStreamingController
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import java.io.IOException
import java.net.SocketException
import javax.inject.Inject

class KkoriApplication : DaggerApplication(), HasActivityInjector {

    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var extractStreamingUrlUseCase: ExtractStreamingUrlUseCase

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
        DaggerAppComponent.builder().application(this).build()

    override fun activityInjector(): DispatchingAndroidInjector<Activity> = activityInjector

    override fun onCreate() {
        super.onCreate()

        RxJavaPlugins.setErrorHandler { e ->
            var error = e
            if (error is UndeliverableException) {
                error = e.cause
            }
            if (error is IOException || error is SocketException) {
                // fine, irrelevant network problem or API that throws on cancellation
                return@setErrorHandler
            }
            if (error is InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return@setErrorHandler
            }
            if (error is NullPointerException || error is IllegalArgumentException) {
                // that's likely a bug in the application
                Thread.currentThread().uncaughtExceptionHandler
                    .uncaughtException(Thread.currentThread(), error)
                return@setErrorHandler
            }
            if (error is IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Thread.currentThread().uncaughtExceptionHandler
                    .uncaughtException(Thread.currentThread(), error)
                return@setErrorHandler
            }
            Log.w("not sure what to do", error)
        }
    }

    fun prepare(videoMeta: VideoMeta) {
        MusicStreamingController.playbackInfoListener.onPrepare(videoMeta)
        MusicStreamingController.controlReleaseRequest()
        compositeDisposable.add(
            extractStreamingUrlUseCase.createObservable(ExtractStreamingUrlUseCase.Params(videoMeta.videoId))
                .subscribe({
                    MusicStreamingController.controlPrepareRequest(it, videoMeta)
                }, { Log.e("asdf", it.message) })
        )
    }
}