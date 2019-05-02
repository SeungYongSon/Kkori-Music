package com.tails.presentation.ui.player

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.AsyncTask
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.SeekBar
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tails.domain.entities.VideoMeta
import com.tails.presentation.R
import com.tails.presentation.streaming.controller.MusicStreamingController
import com.tails.presentation.streaming.controller.PlaybackInfoListener
import com.tails.presentation.ui.MainActivity
import com.tails.presentation.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_player.*
import mkaflowski.mediastylepalette.MediaNotificationProcessor

class PlayerFragment : BaseFragment(), View.OnClickListener, PlaybackInfoListener {

    private var userIsSeeking = false
    private var statusColor: Int = 0

    override val layoutId: Int
        get() = R.layout.fragment_player

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).apply {
            playerBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            playerBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, state: Int) {
                    when (state) {
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            expandPlayer()
                            if (toolbar_expand.visibility == View.GONE) expandToolbarToggle()
                            if (toolbar_collapse.visibility == View.VISIBLE) collapseToolbarToggle()

                            window.apply {
                                statusBarColor = statusColor
                                navigationBarColor = statusColor
                            }
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            collapsePlayer()
                            if (toolbar_expand.visibility == View.VISIBLE) expandToolbarToggle()
                            if (toolbar_collapse.visibility == View.GONE) collapseToolbarToggle()

                            window.apply {
                                statusBarColor = Color.argb(255, 250, 250, 250)
                                navigationBarColor = Color.argb(255, 250, 250, 250)
                            }
                        }
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            collapsePlayer()
                            if (toolbar_expand.visibility == View.VISIBLE) expandToolbarToggle()
                            if (toolbar_collapse.visibility == View.GONE) collapseToolbarToggle()

                            window.apply {
                                statusBarColor = Color.argb(255, 250, 250, 250)
                                navigationBarColor = Color.argb(255, 250, 250, 250)
                            }
                            MusicStreamingController.controlRequest("release")
                        }
                        BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        }
                        BottomSheetBehavior.STATE_DRAGGING -> {
                        }
                        BottomSheetBehavior.STATE_SETTLING -> {
                        }
                    }
                }

                override fun onSlide(p0: View, p1: Float) {}
            })
        }

        initializeUI()

        MusicStreamingController.prepare(this, context!!, "O6tXw1BmJaM")
    }

    private fun initializeUI() {
        toolbar_music_title.isSelected = true
        toolbar_music_uploader.isSelected = true
        music_title.isSelected = true
        music_uploader.isSelected = true
        music_list_next_title.isSelected = true

        music_pause.setOnClickListener(this)
        toolbar_pause.setOnClickListener(this)
        toolbar_cancel.setOnClickListener(this)
        music_down.setOnClickListener(this)
        toolbar_collapse.setOnClickListener(this)

        music_seek.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                var userSelectedPosition = 0

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    userIsSeeking = true
                }

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        userSelectedPosition = progress
                    }
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    userIsSeeking = false
                    MusicStreamingController.controlRequest("seek", "seekPosition", userSelectedPosition)
                }
            })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.music_pause -> {
                if (MusicStreamingController.isPlaying)
                    MusicStreamingController.controlRequest("pause")
                else
                    MusicStreamingController.controlRequest("play")
            }
            R.id.toolbar_pause -> {
                if (MusicStreamingController.isPlaying)
                    MusicStreamingController.controlRequest("pause")
                else
                    MusicStreamingController.controlRequest("play")
            }
            R.id.toolbar_cancel -> {
                (activity as MainActivity).playerBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            R.id.music_down -> {
                (activity as MainActivity).playerBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            R.id.toolbar_collapse -> {
                (activity as MainActivity).playerBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun onPrepareCompleted(videoMeta: VideoMeta) {
        AsyncTask.execute {
            val bitmap = Glide.with(context!!).asBitmap().load(videoMeta.getMqImageUrl()).submit().get()

            activity?.runOnUiThread {
                music_title.text = videoMeta.title
                music_uploader.text = videoMeta.author
                toolbar_music_title.text = videoMeta.title
                toolbar_music_uploader.text = videoMeta.author

                music_image.setImageBitmap(bitmap)
                toolbar_image.setImageBitmap(bitmap)

                val processor = MediaNotificationProcessor(context!!)
                processor.getPaletteAsync({
                    val backgroundColor = it.backgroundColor
                    val foregroundColor = it.primaryTextColor
                    val secondaryColor = it.secondaryTextColor

                    root_layout.setBackgroundColor(backgroundColor)

                    music_title.setTextColor(foregroundColor)
                    music_uploader.setTextColor(secondaryColor)

                    toolbar_music_title.setTextColor(foregroundColor)
                    toolbar_music_uploader.setTextColor(secondaryColor)

                    music_seek.progressDrawable.setColorFilter(foregroundColor, PorterDuff.Mode.SRC_ATOP)
                    music_seek.thumb.setColorFilter(foregroundColor, PorterDuff.Mode.SRC_ATOP)
                    music_seek.indeterminateDrawable.setColorFilter(secondaryColor, PorterDuff.Mode.SRC_ATOP)

                    music_pause.setColorFilter(foregroundColor)

                    music_list.setBackgroundColor(it.secondaryTextColor)
                    music_list_next_title_tag.setTextColor(it.backgroundColor)
                    music_list_next_title.setTextColor(it.backgroundColor)

                    music_previous.setColorFilter(it.secondaryTextColor)
                    music_minus.setColorFilter(it.secondaryTextColor)
                    music_next.setColorFilter(it.secondaryTextColor)
                    music_plus.setColorFilter(it.secondaryTextColor)
                    toolbar_pause.setColorFilter(it.secondaryTextColor)
                    toolbar_cancel.setColorFilter(it.secondaryTextColor)
                    music_down.setColorFilter(it.secondaryTextColor)
                    music_menu.setColorFilter(it.secondaryTextColor)
                    music_time.setTextColor(it.primaryTextColor)
                    music_time_total.setTextColor(it.primaryTextColor)
                    music_list_up.setColorFilter(it.backgroundColor)
                    music_list_menu.setColorFilter(it.backgroundColor)

                    activity?.window?.apply {
                        statusBarColor = it.primaryTextColor
                        navigationBarColor = it.primaryTextColor
                    }
                    this.statusColor = it.primaryTextColor

/*                    if (processor.isLight) {
                        music_previous.setColorFilter(Color.DKGRAY)
                        music_minus.setColorFilter(Color.DKGRAY)
                        music_next.setColorFilter(Color.DKGRAY)
                        music_plus.setColorFilter(Color.DKGRAY)
                        toolbar_pause.setColorFilter(Color.DKGRAY)
                        toolbar_cancel.setColorFilter(Color.DKGRAY)
                        music_down.setColorFilter(Color.DKGRAY)
                        music_menu.setColorFilter(Color.DKGRAY)
                        music_time.setTextColor(Color.DKGRAY)
                        music_time_total.setTextColor(Color.DKGRAY)
                        music_list_up.setColorFilter(Color.DKGRAY)
                        music_list_menu.setColorFilter(Color.DKGRAY)
                    } else {
                        music_previous.setColorFilter(Color.WHITE)
                        music_minus.setColorFilter(Color.WHITE)
                        music_next.setColorFilter(Color.WHITE)
                        music_plus.setColorFilter(Color.WHITE)
                        toolbar_pause.setColorFilter(Color.WHITE)
                        toolbar_cancel.setColorFilter(Color.WHITE)
                        music_down.setColorFilter(Color.WHITE)
                        music_menu.setColorFilter(Color.WHITE)
                        music_time.setTextColor(Color.WHITE)
                        music_time_total.setTextColor(Color.WHITE)
                        music_list_up.setColorFilter(Color.WHITE)
                        music_list_menu.setColorFilter(Color.WHITE)
                    }*/
                    (activity as MainActivity).playerBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }, bitmap)

            }
        }
    }

    override fun onDurationChanged(duration: Int) {
        music_seek.max = duration
        music_time_total.text = getTimeString(duration)
    }

    override fun onPositionChanged(position: Int) {
        if (!userIsSeeking) {
            music_seek.progress = position
            music_time.text = getTimeString(position)
        }
    }

    override fun onStateChanged(state: Int) {
        when (state) {
            PlaybackInfoListener.State.PAUSED -> {
                music_pause.setImageResource(R.drawable.ic_play_circle_outline)
                toolbar_pause.setImageResource(R.drawable.ic_play_arrow_24dp)
            }
            PlaybackInfoListener.State.PLAYING -> {
                music_pause.setImageResource(R.drawable.ic_pause_circle_outline)
                toolbar_pause.setImageResource(R.drawable.ic_pause_24dp)
            }
        }
    }

    override fun onPlaybackCompleted() {}

    private fun getTimeString(mills: Int): String {
        val m = mills % (1000 * 60 * 60) / (1000 * 60)
        val s = mills % (1000 * 60 * 60) % (1000 * 60) / 1000

        return StringBuilder()
            .append(String.format("%02d", m))
            .append(":")
            .append(String.format("%02d", s)).toString()
    }

    private fun expandToolbarToggle() {
        val transition =
            if (toolbar_expand.visibility == View.GONE) Slide(Gravity.TOP)
            else Fade()

        transition.duration = 250
        transition.addTarget(toolbar_expand)

        TransitionManager.beginDelayedTransition(toolbar_expand, transition)
        toolbar_expand.visibility = if (toolbar_expand.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    private fun collapseToolbarToggle() {
        val transition =
            if (toolbar_collapse.visibility == View.GONE) Fade()
            else Slide(Gravity.TOP)

        transition.duration = 200
        transition.addTarget(toolbar_collapse)

        TransitionManager.beginDelayedTransition(toolbar_collapse, transition)
        toolbar_collapse.visibility = if (toolbar_collapse.visibility == View.GONE) View.VISIBLE else View.GONE
    }
}