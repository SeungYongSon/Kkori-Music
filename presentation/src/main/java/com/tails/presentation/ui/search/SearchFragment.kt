package com.tails.presentation.ui.search

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.tails.presentation.KkoriApplication
import com.tails.presentation.R
import com.tails.presentation.databinding.FragmentSearchBinding
import com.tails.presentation.ui.MainActivity
import com.tails.presentation.ui.base.BindingFragment
import com.tails.presentation.ui.search.adapter.MusicListAdapter
import com.tails.presentation.ui.search.adapter.wrapper.LinearLayoutManagerWrapper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_search.*
import javax.inject.Inject

class SearchFragment : BindingFragment<FragmentSearchBinding>() {

    override val layoutId: Int
        get() = R.layout.fragment_search

    @Inject
    lateinit var searchViewModelFactory: SearchViewModelFactory

    private lateinit var musicListAdapter: MusicListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        musicListAdapter = MusicListAdapter((activity as MainActivity).application as KkoriApplication)

        binding.vm = ViewModelProviders.of(this, searchViewModelFactory)[SearchViewModel::class.java]

        initSingleLiveEvent(binding.vm!!)
        initUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        exit()
    }

    private fun initUI() {
        (activity as MainActivity).apply {
            playerBehavior.peekHeight =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56f, resources.displayMetrics).toInt()
            bottomNavigationView.visibility = View.GONE

            searchView.setOnQueryTextListener(binding.vm!!.queryTextListener)
        }

        resultList = search_list

        search_list.adapter = musicListAdapter
        search_list.layoutManager = LinearLayoutManagerWrapper(
            context!!,
            LinearLayoutManager.VERTICAL,
            false
        )
        search_list.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    }

    private fun initSingleLiveEvent(vm: SearchViewModel) {

        vm.startSearchSingleLiveEvent.observe(this, Observer {
            search_progress.visibility = View.VISIBLE
            search_list.visibility = View.INVISIBLE
            musicListAdapter.remove()
            search_list.addOnScrollListener(binding.vm!!.endlessRecyclerOnScrollListener)
        })

        vm.parseSuccessSingleLiveEvent.observe(this, Observer {
            musicListAdapter.add(it)

            if (search_progress.visibility == View.VISIBLE)
                search_progress.visibility = View.INVISIBLE
            if (search_list.visibility == View.INVISIBLE)
                search_list.visibility = View.VISIBLE
        })

        vm.searchNextPageLoadSingleLiveEvent.observe(this, Observer { musicListAdapter.loading() })

        vm.searchEndSingleLiveEvent.observe(this, Observer {
            if (musicListAdapter.itemCount > 1)
                musicListAdapter.removeLoading()
        })

        vm.searchFailSingleLiveEvent.observe(this, Observer {
            if (search_progress.visibility == View.VISIBLE)
                search_progress.visibility = View.INVISIBLE

            Toast.makeText(context, "검색 결과가 없거나\nAPI 호출 횟수가 초과 되었습니다.", Toast.LENGTH_SHORT).show()
        })
    }

    private fun exit() {
        resultList = null
        (activity as MainActivity).apply {
            appBarLayout.setExpanded(true, true)
            playerBehavior.peekHeight =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 112f, resources.displayMetrics).toInt()
            bottomNavigationView.visibility = View.VISIBLE
        }
    }
}