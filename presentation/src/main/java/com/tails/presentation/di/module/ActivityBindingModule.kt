package com.tails.presentation.di.module

import com.tails.presentation.di.qualifier.PerActivity
import com.tails.presentation.ui.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindingModule {

    @PerActivity
    @ContributesAndroidInjector
    abstract fun bindingMainActivity(): MainActivity

}