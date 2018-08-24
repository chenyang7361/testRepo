package com.mivideo.mifm.player

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.mivideo.mifm.R
import com.mivideo.mifm.events.HideMediaDetailPageEvent
import com.mivideo.mifm.util.app.postEvent
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.onClick

class PortraitVideoView : AbstractVideoControllerView {
    private lateinit var mTitleView: TextView
    private lateinit var mBackBtn: ImageView
    private lateinit var mPlayBtn: ImageView
    private lateinit var mPlayNextBtn: ImageView
    private lateinit var mCurrentTimeView: TextView
    private lateinit var mDurationView: TextView
    private lateinit var mSeekBar: SeekBar
    private lateinit var mBottomControlView: View
    private lateinit var mScreenMode: ImageView
    private lateinit var mTitleLayout: LinearLayout


    @JvmOverloads
    constructor(context: Context,
                attrs: AttributeSet? = null,
                defStyle: Int = 0) : super(context, attrs, defStyle) {

//        if (isInEditMode) return

    }

    override fun onCreateView(): View {
        val view = FrameLayout.inflate(context, R.layout.view_player_normal, null)
        return view
    }

    override fun onViewCreated() {
        initView()
    }

    private fun initView() {
//        Glide.with(context).load(R.drawable.loading).diskCacheStrategy(DiskCacheStrategy.RESULT).into(mLoadingView)

        mBackBtn = find<ImageView>(R.id.video_controller_back_button)
        mBackBtn.imageResource = R.drawable.icon_fullscreen_back
        mBackBtn.onClick {
            postEvent(HideMediaDetailPageEvent())
        }
        mPlayBtn = find<ImageView>(R.id.video_controller_play_button)
        mPlayBtn.onClick {
            viewController?.togglePause()
        }

        mTitleView = find<TextView>(R.id.video_controller_title_view)

        mScreenMode = find<ImageView>(R.id.video_controller_zoom_view)
        mScreenMode.onClick {
            if (mPlayerUIListener?.onClickFullScreen() != true) {
                viewController?.handleFullScreen()
            }
        }

        mBottomControlView = find(R.id.video_controller_view)

        mCurrentTimeView = find<TextView>(R.id.video_controller_current_time_view)
        mDurationView = find<TextView>(R.id.video_controller_duration_view)
        mSeekBar = find<SeekBar>(R.id.video_controller_seekbar)
        mSeekBar.max = 1000
        mSeekBar.setOnSeekBarChangeListener(mSeekListener)
        mSeekBar.onClick { }

        mPlayNextBtn = find(R.id.video_controller_next_button)
        mPlayNextBtn.onClick {
            viewController?.playNext()
        }
        mTitleLayout = find(R.id.ll_title)
    }

    override fun showBottomControlView() {
        if (visibility != View.VISIBLE) {
            visibility = View.VISIBLE
        }
//        mSeekBar.tag = DraggablePanel.LOCK_DRAGGABLEPANEL
        fadeInView(mBottomControlView)
    }

    override fun hideBottomControlView() {
        mSeekBar.tag = null
        fadeOutView(mBottomControlView)
    }


    override fun setSeekBarEnable(enable: Boolean) {
        mSeekBar.isEnabled = enable
        mSeekBar.visibility = if (enable) View.VISIBLE else View.GONE
        if (enable) {
            mCurrentTimeView.setVisibility(FrameLayout.VISIBLE)
            mDurationView.setVisibility(FrameLayout.VISIBLE)
        } else {
            mCurrentTimeView.setVisibility(FrameLayout.INVISIBLE)
            mDurationView.setVisibility(FrameLayout.INVISIBLE)
        }
    }

    override fun renderPlayBtn(isPlaying: Boolean) {
        super.renderPlayBtn(isPlaying)
        if (isPlaying) {
            mPlayBtn.setImageResource(R.drawable.ic_player_playing)
        } else {
            mPlayBtn.setImageResource(R.drawable.ic_player_stop)
        }
    }

    override fun showPlayBtn() {
        super.showPlayBtn()
        fadeInView(mPlayBtn)
    }

    override fun hidePlayBtn() {
        super.hidePlayBtn()
        fadeOutView(mPlayBtn)
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (isInEditMode) return
        if (changedView == this) {
            if (visibility == FrameLayout.VISIBLE) {
                viewController?.updatePlayingState()
            }
        }
    }

    override fun renderPlayTimeText(text: String) {
        mCurrentTimeView.text = text
    }

    override fun showVideoTitle(videoTitle: String) {
        mTitleView.visibility = View.VISIBLE
        mTitleView.text = videoTitle
        fadeInView(mTitleLayout)
    }

    override fun renderVideoTitle(videoTitle: String) {
        mTitleView.text = videoTitle
    }

    override fun hideVideoTitle() {
        fadeOutView(mTitleLayout)
    }

    override fun showNextBtn(setShow: Boolean) {
        if (setShow) {
            fadeInView(mPlayNextBtn)
        } else {
            fadeOutView(mPlayNextBtn)
        }
    }

    override fun renderSeekLayout(seekBarProgress: Int, bufferingProgress: Int,
                                  durationText: String, positionText: String) {
        mSeekBar.progress = seekBarProgress
        mSeekBar.secondaryProgress = bufferingProgress
        mDurationView.text = durationText
        mCurrentTimeView.text = positionText
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mSeekBar.setOnSeekBarChangeListener(null)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mSeekBar.setOnSeekBarChangeListener(mSeekListener)
    }

    override fun onEnter(orientation: Int) {
        if (mPlayerView == null || mControllerViewContainer == null) return
        // 隐藏ActionBar、状态栏
//        PlayerUtil.hideActionBar(mContext)

        mPlayerView!!.removeView(mControllerViewContainer)
        val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        mPlayerView!!.addView(mControllerViewContainer, params)

        visibility = View.VISIBLE
    }

    override fun onExit() {
        if (mPlayerView == null || mControllerViewContainer == null) return
        mPlayerView!!.removeView(mControllerViewContainer)
        visibility = View.GONE
    }
}