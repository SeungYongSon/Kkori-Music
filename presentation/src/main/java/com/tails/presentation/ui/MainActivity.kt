package com.tails.presentation.ui

import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.Process
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tails.presentation.R
import com.tails.presentation.streaming.controller.MusicStreamingController
import com.tails.presentation.streaming.receiver.MusicControlReceiver
import com.tails.presentation.ui.player.PlayerFragment
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : DaggerAppCompatActivity() {

    lateinit var playerBehavior: BottomSheetBehavior<View?>
    lateinit var searchView: SearchView
    lateinit var searchItem: MenuItem

    private val broadcastReceiver = MusicControlReceiver()
    private var isSearchBack = false

    private lateinit var navController: NavController
    private val bottomNavigationViewBehavior: BottomSheetBehavior<BottomNavigationView> by lazy {
        BottomSheetBehavior.from(bottomNavigationView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initNotificationActionBroadcastReceiver()

        setSupportActionBar(toolbar)

        playerBehavior = BottomSheetBehavior.from(player_fragment.view)

        supportActionBar!!.setLogo(R.drawable.ic_tail)
        supportActionBar!!.title = "Music"

        navController = findNavController(R.id.page_fragment)
        bottomNavigationView.setupWithNavController(navController)

        supportFragmentManager.beginTransaction().apply {
            add(R.id.player_fragment, PlayerFragment())
            commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search, menu)

        searchItem = menu.findItem(R.id.searchFragment)
        searchView = searchItem.actionView as SearchView

        searchView.queryHint = "검색"
        searchView.maxWidth = Integer.MAX_VALUE

        val v = searchView.findViewById(androidx.appcompat.R.id.search_plate) as View
        val txtSearch = searchView.findViewById(androidx.appcompat.R.id.search_src_text) as EditText

        v.setBackgroundColor(Color.TRANSPARENT)
        txtSearch.setTextColor(Color.argb(255, 255, 87, 34))

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                navController.navigate(R.id.searchFragment)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                isSearchBack = true
                onBackPressed()
                return true
            }
        })
        return true
    }

    override fun onBackPressed() {
        if (playerBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            playerBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            if (!isSearchBack) {
                if (MusicStreamingController.isPlaying)
                    moveTaskToBack(true)
                else finishAndRemoveTask()
            } else {
                isSearchBack = false
                super.onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(broadcastReceiver)
        Process.killProcess(Process.myPid())
    }

    private fun initNotificationActionBroadcastReceiver() {
        val pauseIntentFilter = IntentFilter("kkori.pause")
        val previousIntentFilter = IntentFilter("kkori.previous")
        val nextIntentFilter = IntentFilter("kkori.next")

        this.registerReceiver(broadcastReceiver, previousIntentFilter)
        this.registerReceiver(broadcastReceiver, pauseIntentFilter)
        this.registerReceiver(broadcastReceiver, nextIntentFilter)
    }

    fun collapsePlayer() {
        if (!searchItem.isActionViewExpanded) {
            bottomNavigationViewBehavior.isHideable = false
            bottomNavigationViewBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        appBarLayout.setExpanded(true, true)
    }

    fun expandPlayer() {
        if (!searchItem.isActionViewExpanded) {
            bottomNavigationViewBehavior.isHideable = true
            bottomNavigationViewBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        appBarLayout.setExpanded(false, true)
    }
}