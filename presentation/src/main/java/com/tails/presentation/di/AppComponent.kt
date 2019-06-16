package com.tails.presentation.di

import android.app.Application
import com.tails.presentation.KkoriApplication
import com.tails.presentation.di.module.*
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        SearchModule::class,
        ExtractModule::class,
        ActivityBindingModule::class,
        FragmentBindingModule::class,
        AndroidSupportInjectionModule::class
    ]
)
interface AppComponent : AndroidInjector<KkoriApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }
}