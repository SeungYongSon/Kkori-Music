package com.tails.presentation.di.module

import com.tails.presentation.di.qualifier.PerFragment
import com.tails.presentation.ui.history.HistoryFragment
import com.tails.presentation.ui.home.HomeFragment
import com.tails.presentation.ui.library.LibraryFragment
import com.tails.presentation.ui.player.PlayerFragment
import com.tails.presentation.ui.search.SearchFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBindingModule {

    @PerFragment
    @ContributesAndroidInjector
    abstract fun bindingHomeFragment(): HomeFragment

    @PerFragment
    @ContributesAndroidInjector
    abstract fun bindingHistoryFragment(): HistoryFragment

    @PerFragment
    @ContributesAndroidInjector
    abstract fun bindingLibraryFragment(): LibraryFragment

    @PerFragment
    @ContributesAndroidInjector
    abstract fun bindingSearchFragment(): SearchFragment

    @PerFragment
    @ContributesAndroidInjector
    abstract fun bindingPlayerFragment(): PlayerFragment

}