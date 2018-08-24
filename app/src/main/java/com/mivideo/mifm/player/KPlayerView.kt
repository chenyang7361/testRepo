package com.mivideo.mifm.player

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.github.salomonbrys.kodein.android.appKodein
import com.mivideo.mifm.R
import com.mivideo.mifm.util.ScreenUtil
import org.jetbrains.anko.find

/**
 * 视频播放控件
 * Created by xingchang on 16/11/7.
 * @author LiYan
 */
class KPlayerView : FrameLayout {
    companion object {
        val REGION_CENTER = 0
        val REGION_LEFT = 1
        val REGION_RIGHT = 2
    }

    private var mVideoPlayer: FixedVideoView? = null

    private lateinit var mContainer: FrameLayout

    private var mMediaController: PortraitVideoView? = null
    private var mFullscreenController: FullscreenVideoView? = null

    private var playerView: VideoControllerView? = null
//    private var viewController: VideoController =
//            AdsDecoratorController(DefaultController.get(context))
    private var viewController: VideoController = DefaultController.get(context)

    @JvmOverloads
    constructor(context: Context,
                attrs: AttributeSet? = null,
                defStyle: Int = 0) : super(context, attrs, defStyle) {
        inflate(context, R.layout.layout_video_controller, this)
        if (isInEditMode) return
        initView(context)
        viewController.attachView(mMediaController!!)
        viewController.attachMediaPlayer(mVideoPlayer!!)
        viewController.init(appKodein)

        mMediaController?.setController(viewController)
        mFullscreenController?.setController(viewController)

        viewController.putVideoControllerView(PlayerSizeMode.PLAYER_SIZE_NORMAL, mMediaController!!)
        viewController.putVideoControllerView(PlayerSizeMode.PLAYER_SIZE_FULL_SCREEN, mFullscreenController!!)

    }

    fun setPlayerViewUIListener(listener: PlayerUIListener?) {
        mMediaController?.setPlayerUIListener(listener)
        mFullscreenController?.setPlayerUIListener(listener)
    }

    fun getControllerView(): VideoControllerView {
        return playerView!!
    }

    fun getController(): VideoController {
        return viewController
    }

    private fun initView(context: Context) {
        isClickable = true

        mContainer = find(R.id.playerViewContainer)
        mVideoPlayer = find(R.id.videoPlayer)
//        mVideoPlayer!!.setOnSeekCompleteListener(mOnSeekCompleteListener!!)

        mMediaController = find(R.id.mediaPlayerController)
        playerView = mMediaController
        mMediaController?.attachPlayerView(this, mContainer)

        mFullscreenController = find(R.id.fullscreenController)
        mFullscreenController?.attachPlayerView(this, mContainer)

        viewController.setPlayerSize(PlayerSizeMode.PLAYER_SIZE_NORMAL)

    }

    /**
     * 设置分享（微信，QQ等）按钮点击事件监听
     *
     * @param listener 分享按钮点击监听器
     */
//    fun setShareClickListener(listener: ShareListener) {
//        mFullscreenController?.setShareListener(listener)
//    }

    fun changePlayerSizeTo(state: PlayerSizeMode) {
        if (state == PlayerSizeMode.PLAYER_SIZE_FULL_SCREEN) {
            ScreenUtil.hideNavigationBar(context as Activity)
            playerView = mFullscreenController
            mMediaController?.visibility = View.GONE
            mFullscreenController?.visibility = View.VISIBLE
        } else {
            ScreenUtil.showNavigationBar(context as Activity)
            playerView = mMediaController
            mMediaController?.visibility = View.VISIBLE
            mFullscreenController?.visibility = View.GONE
        }
    }

    fun videoListUpdated() {
        viewController.updateNextBtn()
    }

    fun getMediaPlayer(): FixedVideoView? {
        return mVideoPlayer
    }

    fun getCurrentPosition(): Int {
        if (mVideoPlayer != null) {
            return mVideoPlayer!!.currentPosition
        }
        return 0
    }

    fun hideMiniSize() {
//        viewController.setPlayerSize(PlayerSizeMode.PLAYER_SIZE_INLINE)
//        mMiniPlayerView?.onExit()
    }
}