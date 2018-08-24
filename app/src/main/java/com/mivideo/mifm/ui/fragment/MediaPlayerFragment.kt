package com.mivideo.mifm.ui.fragment

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.github.salomonbrys.kodein.instance
import com.hwangjr.rxbus.annotation.Subscribe
import com.mivideo.mifm.NetworkManager
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.CommonVideoHistory
import com.mivideo.mifm.data.models.jsondata.common.CommonVideo
import com.mivideo.mifm.data.models.jsondata.common.VideoInfoParams
import com.mivideo.mifm.data.repositories.VideoRepository
import com.mivideo.mifm.events.*
import com.mivideo.mifm.player.*
import com.mivideo.mifm.player.manager.MediaManager
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.ui.widget.StatusLayout
import com.mivideo.mifm.ui.widget.StatusLayoutListener
import com.mivideo.mifm.util.PlayerUtil
import com.mivideo.mifm.util.app.postEvent
import org.jetbrains.anko.support.v4.find
import timber.log.Timber

class MediaPlayerFragment : BaseFragment() {

//    companion object {
//        const val ARG_EVENT = "event"
//    }
//
//    private lateinit var videoContainer: FrameLayout
//    private lateinit var draggableHandle: View
//
//    private var videoController: KPlayerView? = null
//    private var autoPlayEvent: NotifyMediaPlayEvent? = null
////    private val videoRepo: VideoRepository by instance()
//    private val videoProxySource: VideoProxySource by instance()
//
//    private var kPlayerManager: KPlayerManager? = null
//    private lateinit var event: NotifyMediaPlayEvent
//    private lateinit var videoInfo: VideoInfoParams
//
//    private var lateInitAction: Runnable? = null
//    private var createdView: View? = null
//    private lateinit var statusLayout: StatusLayout
//    private var currentVideoInHomeListPosition: Int = -1
//    private var isCommentInfoVisibility: Boolean = false
//
//    private val mPlayerListener = object : PlayListener() {
//
//        override fun onComplete() {
//            videoController?.getController()?.autoPlayNext()
//        }
//    }
//
//    private var mLastVideoPlayEvent: NotifyMediaPlayEvent? = null
//
//    private val mPlayerErrorHandleListener = object : ErrorHandleListener {
//        override fun onClickRetry() {
//            if (mLastVideoPlayEvent != null && isVisible) {
//                onNotifyMediaPlayEvent(mLastVideoPlayEvent!!)
//            }
//        }
//
//        override fun onClickUseMobileContinue() {
//        }
//
//    }
//
//    private val lifecycleInterceptor = object : PlayerLifecycleInterceptor() {
//        override fun onInterceptResume(): Boolean {
//            return !isSupportVisible
//        }
//    }
//
//    override fun onCreateView(inflater: LayoutInflater,
//                              container: ViewGroup?,
//                              savedInstanceState: Bundle?): View? {
//        return inflater.inflate(R.layout.fragment_media_player, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        kPlayerManager = KPlayerManager.get(context.applicationContext)
//        if (arguments != null && arguments!!.getParcelable<NotifyMediaPlayEvent>(ARG_EVENT) != null) {
//            this.event = arguments!!.getParcelable(ARG_EVENT)
//        }
//        videoInfo = event.video
//
//        videoContainer = find(R.id.videoContainer)
//        draggableHandle = find(R.id.draggableHandle)
//        statusLayout = find(R.id.statusLayout)
//
//        statusLayout.setStatusLayoutListener(object : StatusLayoutListener {
//            override fun onRetry() {
//                if (mLastVideoPlayEvent != null && isVisible) {
//                    onNotifyMediaPlayEvent(mLastVideoPlayEvent!!)
//                }
//            }
//        })
//
//        view?.postDelayed({
//            PlayerUtil.hideActionBar(context)
//        }, 300)
//
//        initVideoController()
//
//        lateInitAction = Runnable { loadViewAndData() }
//        createdView = view
//        createdView?.post(lateInitAction)
//
//        if (NetworkManager.isNetworkUnConnected()) {
//            showNetUnconnected()
//        }
//    }
//
//    /**
//     * 初始化VideoController
//     */
//    private fun initVideoController() {
//        if (kPlayerManager?.getCurrentPlayer() == null) {
//            val playerView = KPlayerView(context)
//            kPlayerManager?.setCurrentPlayer(playerView)
//            kPlayerManager?.attachLifecycleToPlayer(lifecycle)
//        }
//        videoController = kPlayerManager?.getCurrentPlayer()!!
//        videoController?.getController()?.setPlayerSize(PlayerSizeMode.PLAYER_SIZE_NORMAL)
//        videoController?.getController()?.addErrorListener(mPlayerErrorHandleListener)
//        videoController?.getController()?.addPlayListener(mPlayerListener)
//        videoController?.getController()?.addLifecycleInterceptor(lifecycleInterceptor)
//
//        view?.post {
//            if (videoController?.parent != null) {
//                (videoController?.parent as ViewGroup).removeView(videoController)
//                if (null != videoContainer && null != videoController) {
//                    videoContainer.addView(videoController)
//                }
//            } else {
//                if (videoContainer != null && null != videoController) {
//                    videoContainer.addView(videoController)
//                }
//            }
//        }
//
//    }
//
//    /**
//     * 自动播放模式 vs 点击播放模式
//     */
//    @Subscribe
//    fun onNotifyMediaPlayEvent(event: NotifyMediaPlayEvent) {
//        Timber.i("onNotifyVideoPlayEvent.......")
//        mLastVideoPlayEvent = event
//        currentVideoInHomeListPosition = event.itemPositionInList
//        if (!event.autoPlay) {
//            autoPlayEvent = null
//        }
//        if (event.autoPlay) {
//            autoPlayEvent = event
//            return
//        }
//
//        if (event.video.isVideoIdOpen) {
//            statusLayout.showLoadingView()
//            Log.d("PPP", "event.video.commonVideo.video_id|" + event.video.commonVideo.video_id)
//            var testId = "j-kkL62uCa823M16ZSY-WAC_JtMlXPA="
//            videoRepo.loadVideoDetailNetwork(testId)
//                    .compose(asyncSchedulers())
//                    .subscribe({ cVideo: CommonVideo? ->
//                        Log.d("PPP", "loadVideoDetailNetwork|success")
//                        if (cVideo != null) {
//                            event.video.commonVideo = cVideo
//                            preparePlay(event.video, true)
//                        }
//                    }, {
//                        Log.d("PPP", "loadVideoDetailNetwork|failure")
//                        statusLayout.showErrorView()
//                    })
//        } else {
//            if (!TextUtils.isEmpty(event.video.commonVideo.play_url)) {
//                // 自动播放模式
//                if (event.autoPlay) {
//                    statusLayout.showContentView()
//                    preparePlay(event.video, false)
//                }
//                // 点击播放模式
//                else {
//                    statusLayout.showLoadingView()
//                    preparePlay(event.video, true)
//                }
//            } else {
//                videoController?.getController()?.playNext()
//            }
//        }
//    }
//
//    /**
//     * 初始化播放组件并播放视频
//     */
//    private fun preparePlay(videoInfo: VideoInfoParams, isUpdateList: Boolean) {
//        this.videoInfo = videoInfo
////        updateToolbar()
//        updateListContent(isUpdateList)
//    }
//
//    private fun loadViewAndData() {
//        if (!isAdded) {
//            return
//        }
//
//        postFragmentCreateEvent()
//        onNotifyMediaPlayEvent(event)
//    }
//
//    /**
//     * 执行视频的播放操作
//     */
//    private fun playVideo(videoInfo: VideoInfoParams) {
//        if (videoController?.getController()?.getPlayVideoInfo()?.commonVideo?.video_id ==
//                videoInfo.commonVideo.video_id) {
//            videoController?.getController()?.resume()
//            return
//        } else {
//            if (videoController != null) {
//                savePlayPosition()
//                val positionObservable = videoRepo.getLastPosition(videoInfo.commonVideo.video_id)
//                videoController?.getController()?.startPlayVideo(videoInfo, positionObservable)
//            } else {
//                videoController?.getController()?.startPlayVideo(videoInfo)
//            }
//
//            videoRepo.saveHistory(CommonVideoHistory(videoInfo.commonVideo))
//        }
//    }
//
//    /**
//     * 更新列表内容
//     */
//    private fun updateListContent(isUpdateList: Boolean) {
//        if (NetworkManager.isNetworkUnConnected()) {
//            showNetUnconnected()
//        }
//        if (isUpdateList) {
//            hideTipView()
//            playVideo(videoInfo)
//            statusLayout.showContentView()
//        } else {
//            hideTipView()
//            playVideo(videoInfo)
//            statusLayout.showContentView()
//        }
//    }
//
//    private fun savePlayPosition() {
//        if (videoController == null) return
//        val playVideoInfo = videoController!!.getController().getPlayVideoInfo()
//        val position = videoController!!.getCurrentPosition()
//        if (playVideoInfo != null) {
//            playVideoInfo.commonVideo.saveViewTime()
//            playVideoInfo!!.commonVideo.viewTime = 0
//            videoRepo.saveHistory(CommonVideoHistory(playVideoInfo.commonVideo, position))
//        }
//    }
//
//    private fun notifyAutoPlay() {
//        if (autoPlayEvent != null) {
//            postEvent(autoPlayEvent!!)
//            autoPlayEvent = null
//        }
//    }
//
//    /**
//     * 暂停视频播放功能
//     */
//    @Subscribe
//    fun onPauseMediaEvent(event: PauseMediaEvent) {
//        Timber.i(event.sender + ":pause media")
//        videoController?.getController()?.pause()
//    }
//
//    /**
//     * 恢复视频播放功能
//     */
//    @Subscribe
//    fun onResumeMediaEvent(event: ResumeMediaEvent) {
//        Timber.i(event.sender + ":resume video")
//        videoController?.getController()?.resume()
//    }
//
//    /**
//     * 播放器控件返回按键处理逻辑
//     */
//    @Subscribe
//    fun onHideMediaPlayerPageEvent(event: HideMediaPlayerPageEvent) {
//        if (isAdded && activity is MediaPlayerActivity) {
//            postEvent(SingleFragmentBackEvent(""))
//        } else {
//            pop()
//        }
//    }
//
//    /**
//     * 点击更多，显示全部相关视频
//     */
//    @Subscribe
//    fun onMediaMoreClickEvent(event: MediaMoreClickEvent) {
////        val videoInfo = videoDetailAdapter?.videoProxySource?.getNextPlayVideo(false)
////        videoDetailAdapter?.addMoreVideo(event.videoMoreList)
////        if (videoInfo == null) {
////            videoDetailAdapter?.videoProxySource?.moveToPrePosition()
////            videoProxySource.playNextVideo(true)
////        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//        videoController?.getController()?.removeLifecycleInterceptor(lifecycleInterceptor)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        videoController?.getController()?.removePlayListener(mPlayerListener)
//        videoController?.getController()?.removeErrorListener(mPlayerErrorHandleListener)
//        videoController?.getController()?.removeLifecycleInterceptor(lifecycleInterceptor)
//        videoController?.getController()?.reset()
//        videoController = null
//    }
//
//    override fun onDestroyView() {
//        savePlayPosition()
//        PlayerUtil.showActionBar(context)
//        videoContainer.removeView(videoController)
//        super.onDestroyView()
//    }
//
//    override fun onDetach() {
//        super.onDetach()
//        createdView?.removeCallbacks(lateInitAction)
//    }
//
//    override fun onBackPressedSupport(): Boolean {
//        if (kPlayerManager?.onBackPressed() == true) return true
//        if (activity != null && activity is MediaPlayerActivity) {
//            return super.onBackPressedSupport()
//        } else {
//            pop()
//            return true
//        }
//    }
//
//    private var mVideoPausedByInvisible = false
//
//    override fun onSupportInvisible() {
//        super.onSupportInvisible()
//        Timber.i("onSupportInvisible")
//        val controller = videoController?.getController()
//        mVideoPausedByInvisible = true
//        controller?.pause()
//    }
//
//    override fun onSupportVisible() {
//        super.onSupportVisible()
//        Timber.i("onSupportVisible")
//        val controller = videoController?.getController()
//        if (mVideoPausedByInvisible && controller?.isPaused() == true) {
//            mVideoPausedByInvisible = false
//            controller.start()
//        }
//    }
//
//    override fun onNewBundle(args: Bundle?) {
//        super.onNewBundle(args)
//        if (args?.getParcelable<NotifyMediaPlayEvent>(ARG_EVENT) != null) {
//            this.event = args.getParcelable(ARG_EVENT)
//            this.videoInfo = event.video
//            onNotifyMediaPlayEvent(event)
//        }
//    }
}

fun createMediaPlayerFragment(event: NotifyMediaPlayEvent): MediaPlayerFragment {
    val mediaPlayerFragment = MediaPlayerFragment()
    val args = Bundle()
//    args.putParcelable(MediaPlayerFragment.ARG_EVENT, event)
    mediaPlayerFragment.arguments = args
    mediaPlayerFragment.putNewBundle(args)
    return mediaPlayerFragment
}