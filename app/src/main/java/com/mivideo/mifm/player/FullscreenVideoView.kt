package com.mivideo.mifm.player

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.mivideo.mifm.R
import com.mivideo.mifm.manager.OrientationManager
import com.mivideo.mifm.util.PlayerUtil
import com.mivideo.mifm.util.ScreenUtil
import org.jetbrains.anko.enabled
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.onClick

class FullscreenVideoView : AbstractVideoControllerView {
    private lateinit var mBackBtn: ImageView
    private lateinit var mPlayBtn: ImageView
    private lateinit var mPlayNextBtn: ImageView
    private lateinit var mCurrentTimeView: TextView
    private lateinit var mDurationView: TextView
    private lateinit var mTitleView: TextView
    private lateinit var mSeekBar: SeekBar
    private lateinit var mBottomControlView: View
    private lateinit var mScreenMode: ImageView
    private lateinit var mSharedGroup: View
    private lateinit var mSharedWeixin: ImageView
    private lateinit var mSharedQQ: ImageView
    private lateinit var mSharedWeibo: ImageView
    private lateinit var mSharedWeixinFriend: ImageView
    private lateinit var mLockView: ImageView
    private lateinit var mFasGroup: View
    private lateinit var mFasIcon: ImageView
    private lateinit var mFasCurrentTimeView: TextView
    private lateinit var mFasDurationView: TextView
    private lateinit var mFasTextView: TextView
    private lateinit var mFasTimeView: View
    private lateinit var mTitleLayout: LinearLayout
    private lateinit var mResolutionText: TextView

    @JvmOverloads
    constructor(context: Context,
                attrs: AttributeSet? = null,
                defStyle: Int = 0) : super(context, attrs, defStyle) {
//        if (isInEditMode) {
//            inflate(context, R.layout.view_player_fullscreen, this)
//        }
        mContext = context!!
    }

    override fun onCreateView(): View {
        val view = inflate(context, R.layout.view_player_fullscreen, null)
        return view
    }

    override fun onViewCreated() {
        initView()
    }

    private fun initView() {
        isClickable = true
//        Glide.with(context).load(R.drawable.loading).diskCacheStrategy(DiskCacheStrategy.RESULT).into(mLoadingView)

        mTitleLayout = find(R.id.ll_title)
        mBackBtn = find(R.id.video_controller_back_button)
        mBackBtn.onClick {
            if (mPlayerUIListener?.onClickBackBtn() != true) {
                viewController?.clickBackBtn()
            }
        }
        mPlayBtn = find(R.id.video_controller_play_button)
        mPlayBtn.onClick {
            viewController?.togglePause()
        }

        mScreenMode = find(R.id.video_controller_zoom_view)
        mScreenMode.onClick {
            if (mPlayerUIListener?.onClickFullScreen() != true) {
                viewController?.handleFullScreen()
            }
        }

        mBottomControlView = find(R.id.video_controller_view)

        mCurrentTimeView = find(R.id.video_controller_current_time_view)
        mDurationView = find(R.id.video_controller_duration_view)
        mSeekBar = find(R.id.video_controller_seekbar)
        mSeekBar.max = 1000
        mSeekBar.setOnSeekBarChangeListener(mSeekListener)
        mSeekBar.onClick { }

        mPlayNextBtn = find(R.id.video_controller_next_button)
        mPlayNextBtn.onClick {
            viewController?.playNext()
        }

        mLockView = find(R.id.video_controller_lock_button)
//        if (OrientationManager.instance!!.lockedScreen()) {
//            mLockView.imageResource = R.drawable.player_icon_locked
//        } else {
        mLockView.imageResource = R.drawable.ic_player_unlock
//        }
        mLockView.setOnClickListener {
            viewController?.clickLockView()

        }

        mSharedGroup = find(R.id.video_controller_shared_group)
        mSharedWeixin = find(R.id.video_controller_shared_weixin_view)
        mSharedWeixin.onClick {
//            mShareListener?.onShareWx()
        }

        mSharedWeixinFriend = find(R.id.video_controller_shared_weixinfriend_view)
        mSharedWeixinFriend.onClick {
//            mShareListener?.onShareWxMoments()
        }

        mSharedQQ = find(R.id.video_controller_shared_qq_view)
        mSharedQQ.onClick {
//            mShareListener?.onShareQQ()
        }

        mSharedWeibo = find(R.id.video_controller_shared_weibo_view)
        mSharedWeibo.onClick {
//            mShareListener?.onShareWeibo()
        }

        mTitleView = find(R.id.video_controller_title_view)

        mFasGroup = find(R.id.video_controller_fas_group)
        mFasGroup.visibility = View.INVISIBLE
        mFasIcon = find(R.id.video_controller_fas_icon)

        mFasCurrentTimeView = find(R.id.video_controller_fas_current_time_view)
        mFasDurationView = find(R.id.video_controller_fas_duration_view)
        mFasTextView = find(R.id.video_controller_fas_text_view)
        mFasTimeView = find(R.id.video_controller_fas_time_view)
        mResolutionText = find(R.id.video_controller_resolution)
        mResolutionText.onClick {
//            viewController?.clickResolutionPick()
        }
    }

//    fun setShareListener(listener: ShareListener) {
//        this.mShareListener = listener
//    }


    override fun showBottomControlView() {
        ScreenUtil.showNavigationBar(context as Activity)
        fadeInView(mBottomControlView)
        fadeInView(mSharedGroup)
    }

    override fun hideBottomControlView() {
        ScreenUtil.hideNavigationBar(context as Activity)
        fadeOutView(mBottomControlView)
        fadeOutView(mSharedGroup)
    }

    override fun showLockView(setShow: Boolean) {
        if (setShow) {
//            mSeekBar.tag = DraggablePanel.LOCK_DRAGGABLEPANEL
            mLockView.imageResource = R.drawable.ic_player_lock
            fadeInView(mLockView)
        } else {
            fadeOutView(mLockView)
        }
    }

    override fun showUnLockView(setShow: Boolean) {
        if (setShow) {
            mSeekBar.tag = null
            mLockView.imageResource = R.drawable.ic_player_unlock
            fadeInView(mLockView)
        } else {
            fadeOutView(mLockView)
        }
    }


    override fun setSeekBarEnable(enable: Boolean) {
        mSeekBar.isEnabled = enable
        mSeekBar.visibility = if (enable) View.VISIBLE else View.GONE
        if (enable) {
            mCurrentTimeView.setVisibility(VISIBLE)
            mDurationView.setVisibility(VISIBLE)
        } else {
            mCurrentTimeView.setVisibility(INVISIBLE)
            mDurationView.setVisibility(INVISIBLE)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        if (viewController?.isFullScreen() == true && isClickable) {
            return viewController?.onTouchEvent(event) ?: false
        } else {
            return false
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

    override fun renderSeekLayout(seekBarProgress: Int, bufferingProgress: Int,
                                  durationText: String, positionText: String) {
        mSeekBar.progress = seekBarProgress
        mSeekBar.secondaryProgress = bufferingProgress
        mDurationView.text = durationText
        mCurrentTimeView.text = positionText
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (isInEditMode) return
        if (changedView == this) {
            if (visibility == VISIBLE) {
                viewController?.updatePlayingState()
            }
        }
    }

    override fun renderPlayTimeText(text: String) {
        mCurrentTimeView.text = text
    }

    override fun showBrightAdjustLayout(brightValue: String) {
        mFasIcon.setImageResource(R.drawable.home_icon_brightness)

        if (mFasGroup.visibility != View.VISIBLE) {
            mFasGroup.visibility = View.VISIBLE
        }

        if (mFasTextView.visibility != View.VISIBLE) {
            mFasTextView.visibility = View.VISIBLE
        }

        mFasTextView.text = brightValue

        if (mFasTimeView.visibility == View.VISIBLE) {
            mFasTimeView.visibility = View.INVISIBLE
        }
    }

    override fun showAdjustVolumeLayout(newValue: Int, text: String) {
        if (newValue == 0) {
            mFasIcon.setImageResource(R.drawable.home_icon_mute)
        } else {
            mFasIcon.setImageResource(R.drawable.home_icon_sound)
        }

        if (mFasGroup.visibility != View.VISIBLE) {
            mFasGroup.visibility = View.VISIBLE
        }

        if (mFasTextView.visibility != View.VISIBLE) {
            mFasTextView.visibility = View.VISIBLE
        }

        mFasTextView.text = text

        if (mFasTimeView.visibility == View.VISIBLE) {
            mFasTimeView.visibility = View.INVISIBLE
        }
    }


    override fun showNextBtn(setShow: Boolean) {
        if (setShow) {
            fadeInView(mPlayNextBtn)
        } else {
            fadeOutView(mPlayNextBtn)
        }
    }

    override fun showFastGroupView(setShow: Boolean) {
        if (setShow) {
            mFasGroup.visibility = View.VISIBLE
        } else {
            mFasGroup.visibility = View.INVISIBLE
        }
    }

    override fun showVideoTitle(videoTitle: String) {
        mTitleView.text = videoTitle
        fadeInView(mTitleLayout)
    }

    override fun renderVideoTitle(videoTitle: String) {
        mTitleView.text = videoTitle
    }

    override fun hideVideoTitle() {
        fadeOutView(mTitleLayout)
    }

    override fun renderFastMoveLayout(fastFaward: Boolean,
                                      durationText: String,
                                      seekText: String) {
        if (fastFaward) {
            mFasIcon.setImageResource(R.drawable.home_next_icon)
        } else {
            mFasIcon.setImageResource(R.drawable.home_back_icon)
        }
        mFasGroup.visibility = View.VISIBLE
        mFasTextView.visibility = View.INVISIBLE
        mFasTimeView.visibility = View.VISIBLE

        mFasDurationView.text = durationText
        mFasCurrentTimeView.text = seekText
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mSeekBar.setOnSeekBarChangeListener(null)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mSeekBar.setOnSeekBarChangeListener(mSeekListener)
    }

    override fun renderResolutionText(text: String, enable: Boolean) {
        super.renderResolutionText(text, enable)
        if (TextUtils.isEmpty(text)) {
            mResolutionText.visibility = View.GONE
        } else {
            mResolutionText.visibility = View.VISIBLE
            mResolutionText.enabled = enable
            mResolutionText.text = text
        }
    }

    override fun onEnter(orientation: Int) {
        if (mPlayerView == null || mControllerViewContainer == null) return
        // 隐藏ActionBar、状态栏，并横屏
        PlayerUtil.hideActionBar(mContext)
        if (orientation == OrientationManager.OrientationChangedListener.ORIENTATION_LANDSCAPE) {
            PlayerUtil.scanForActivity(mContext)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            PlayerUtil.scanForActivity(mContext)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        }
        val contentView = PlayerUtil.scanForActivity(mContext)
                ?.findViewById<ViewGroup>(android.R.id.content)

        contentView?.removeView(mControllerViewContainer)
        mPlayerView!!.removeView(mControllerViewContainer)
        val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        contentView?.addView(mControllerViewContainer, params)

        visibility = View.VISIBLE
    }

    override fun onExit() {
        if (mPlayerView == null || mControllerViewContainer == null) return
        PlayerUtil.scanForActivity(mContext)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        PlayerUtil.showActionBar(mContext)
        ScreenUtil.showNavigationBar(context as Activity)
        val contentView = PlayerUtil.scanForActivity(mContext)
                ?.findViewById<ViewGroup>(android.R.id.content)
        contentView?.removeView(mControllerViewContainer)

        visibility = View.GONE
    }
}