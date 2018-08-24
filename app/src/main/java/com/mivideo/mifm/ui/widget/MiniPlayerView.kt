package com.mivideo.mifm.ui.widget

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.Toast
import com.mivideo.mifm.R
import com.mivideo.mifm.events.ShowPlayerEvent
import com.mivideo.mifm.player.manager.DataContainer
import com.mivideo.mifm.util.app.postEvent

/**
 * Created by Jiwei Yuan on 18-7-25.
 */
class MiniPlayerView : ImageView {

    companion object {
        const val STATUS_PAUSE = 0
        const val STATUS_PLAYING = 1
    }

    private var switching: Boolean = false
    private var status = STATUS_PAUSE
    private var canControl = false

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    fun setCanControl() {
        canControl = true
    }

    private fun initView() {
        setOnClickListener(null)
        setImageResource(R.drawable.mini_play_pause)
        (drawable as AnimationDrawable).start()
        setOnClickListener(object : OnClickListener {
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            override fun onClick(v: View?) {

                if(DataContainer.hasData()) {
                    postEvent(ShowPlayerEvent())
                }else{
                    Toast.makeText(context,R.string.no_content_to_play,Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun setOnClickListener(l: OnClickListener?) {
        val listener = OnClickListener {
            if (canControl) {
                switchStatue()
            }
            l?.onClick(it)
        }
        super.setOnClickListener(listener)
    }

    private fun switchStatue() {
        if (switching) {
            return
        }
        if (status == STATUS_PAUSE) {
            switchToPlay()
        } else {
            switchToPause()
        }
    }

    fun switchToPause() {
        if (status == STATUS_PAUSE) {
            return
        }
        switching = true

        (drawable as AnimationDrawable).stop()
        setImageResource(R.drawable.mini_play_play2pause)
        val drawable = this.drawable as AnimationDrawable
        drawable.start()
        postDelayed({
            switching = false
            pause()
        }, 20 * 18)
    }

    fun switchToPlay() {
        if (status == STATUS_PLAYING) {
            return
        }
        switching = true

        (drawable as AnimationDrawable).stop()
        setImageResource(R.drawable.mini_play_pause2play)
        val drawable = this.drawable as AnimationDrawable
        drawable.start()
        postDelayed({
            switching = false
            playing()
        }, 20 * 13)
    }

    private fun pause() {
        if (status == STATUS_PAUSE) {
            return
        }

        if (status == STATUS_PLAYING) {
            (drawable as AnimationDrawable).stop()
        }

        status = STATUS_PAUSE
        setImageResource(R.drawable.mini_play_pause)
        (drawable as AnimationDrawable).start()
    }

    private fun playing() {
        if (status == STATUS_PLAYING) {
            return
        }

        status = STATUS_PLAYING
        setImageResource(R.drawable.mini_playing)
        (drawable as AnimationDrawable).start()
    }
}