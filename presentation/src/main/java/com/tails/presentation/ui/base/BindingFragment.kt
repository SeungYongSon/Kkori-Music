package com.tails.presentation.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.CompositeDisposable

abstract class BindingFragment<T : ViewDataBinding> : DaggerFragment() {

    lateinit var rootView: View
    lateinit var binding: T
    abstract val layoutId: Int

    companion object {
        var resultList: RecyclerView? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        rootView = binding.root

        return rootView
    }
}