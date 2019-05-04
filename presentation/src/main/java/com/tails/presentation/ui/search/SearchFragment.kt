package com.tails.presentation.ui.search

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import com.tails.presentation.R
import com.tails.presentation.ui.MainActivity
import com.tails.presentation.ui.base.BaseFragment

class SearchFragment : BaseFragment() {
    override val layoutId: Int
        get() = R.layout.fragment_search

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {

                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                return false
            }
        })
    }
}