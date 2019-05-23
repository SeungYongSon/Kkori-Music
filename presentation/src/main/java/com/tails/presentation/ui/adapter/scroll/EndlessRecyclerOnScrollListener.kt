package com.tails.presentation.ui.adapter.scroll

import android.util.Log
import androidx.recyclerview.widget.RecyclerView


abstract class EndlessRecyclerOnScrollListener : RecyclerView.OnScrollListener() {

    var isFirstLoading = true

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (!recyclerView.canScrollVertically(-1)) {
            Log.e("scroll state", "Top of list")
        } else if (!recyclerView.canScrollVertically(1)) {
            Log.e("scroll state", "End of list")
            if(!isFirstLoading) onLoadMore()
            else isFirstLoading = false
        } else {
            Log.e("scroll state", "idle")
        }
    }

    abstract fun onLoadMore()
}