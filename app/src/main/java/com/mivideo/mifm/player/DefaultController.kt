package com.mivideo.mifm.player

import android.app.Activity
import android.arch.lifecycle.Lifecycle
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.MainApp
import com.mivideo.mifm.NetworkManager
import com.mivideo.mifm.R
import com.mivideo.mifm.SpManager
import com.mivideo.mifm.data.models.jsondata.common.VideoInfoParams
import com.mivideo.mifm.manager.OrientationManager
import com.mivideo.mifm.manager.OrientationManager.OrientationChangedListener.Companion.ORIENTATION_LANDSCAPE
import com.mivideo.mifm.manager.OrientationManager.OrientationChangedListener.Companion.ORIENTATION_LANDSCAPE_REVERSE
import com.mivideo.mifm.plugin.PluginException
import com.mivideo.mifm.util.app.showToast
import com.mivideo.mifm.util.bus.changeMillisecondTime2Str
import rx.Observable
import rx.Subscriber
import rx.Subscription
import timber.log.Timber

/**
 * 播放器Controller默认实现
 * @author LiYan
 */
class DefaultController : AbstractVideoController, KodeinInjected {

    companion object {
        private const val AUTO_DISMISS_TIMER = 8000

        /**
         * 播放器初始状态
         */
        private val MEDIA_STATUS_INITIAL = 0
        /**
         * 开始播放loading状态
         */
        private val MEDIA_STATUS_LOADING = 1
        /**
         * 播放缓冲状态
         */
        private val MEDIA_STATUS_BUFFERING = 2
        /**
         * 正在播放状态
         */
        private val MEDIA_STATUS_PLAYING = 3

        fun get(context: Context): DefaultController {
            return DefaultController(context)
        }
    }

    private var mContext: Context
    private lateinit var orientationManager: OrientationManager
    private lateinit var videoProxySource: VideoProxySource

    //播控布局是否在显示
    private var mIsControlViewShowing = false
    //是否已锁定屏幕
    private var mLocked = false
    //当前用户是否正在seek操作
    private var mIsSeeking = false
    //播控是否用户点击触发显示的
    private var mUserClick = false
    //是否会提示自动播放的下一个视频的标题
    private var mNeedTippedNextVideo = true
    //视频播放之前网络检查等preload操作是否成功
    private var mPreloadSuccess = false
    private var mVideoReseted: Boolean = true
    private var ignoreCheckWifi = false


    private var mPosition = 0
    private var mCachedSeekPosition = -1
    private var mLastPosition = 0
    private var mCurrentStatus = MEDIA_STATUS_INITIAL
    private var mFullscreenOrientation = ORIENTATION_LANDSCAPE
    private var mLastFullscreenOrientation = ORIENTATION_LANDSCAPE

    //当前视频view尺寸变换的前一个尺寸
    private var lastSize: PlayerSizeMode? = null
    private var mCurrentVideo: VideoInfoParams? = null
    private var mErrorVideo: VideoInfoParams? = null
    private var mNextVideo: VideoInfoParams? = null
//    private var mAdController: AdsViewController? = null
    private var spManager: SpManager? = null
    private var mVideoUrlManager: VideoUrlManager? = null
    private var playerGestureDetector: PlayerGestureDetector? = null
    private var systemHelper: SystemHelper? = null
    private var mNextVideoHint: String = ""

    private val mHandler = Handler(Looper.getMainLooper())
    private var mUpdateProgressRunner: Runnable? = null
    private var mPlayNextRunner: Runnable? = null
    private var mAutoDismiss: Runnable? = null

    private val mSeekRunner = Runnable {
        if (mVideoPlayer == null) {
            return@Runnable
        }
        if (mCachedSeekPosition >= 0) {
            mVideoPlayer?.seekTo(mCachedSeekPosition)
        }
        mCachedSeekPosition = -1
    }

    //系统未锁定方向转动，转动屏幕监听实现全屏和退出全屏
    private var mo = object : OrientationManager.OrientationChangedListener {
        override fun onOrientationChanged(orientation: Int) {
            Timber.i("onOrientationChanged:$orientation")

            if (mVideoReseted || !enableOrientationListener) return
            if (mLifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == false) return

            val fullScreen = orientation == ORIENTATION_LANDSCAPE ||
                    orientation == ORIENTATION_LANDSCAPE_REVERSE
            if (fullScreen) {
                mLastFullscreenOrientation = mFullscreenOrientation
                mFullscreenOrientation = orientation
                var delay = 0L
                //api低于19的设备在监听方向转动全屏是会产生onAttachGlContext的异常
                //这里发现showController操作可以规避此问题，暂时先做一个兼容
                if (Build.VERSION.SDK_INT <= 19) {
                    showController()
                    delay = 500L
                }
                mHandler.postDelayed({ setPlayerSize(PlayerSizeMode.PLAYER_SIZE_FULL_SCREEN) }, delay)
            } else {
                if (lastSize == PlayerSizeMode.PLAYER_SIZE_NORMAL) {
                    setPlayerSize(PlayerSizeMode.PLAYER_SIZE_NORMAL)
                }
            }
        }
    }

    override val injector = KodeinInjector()

    private constructor(context: Context) {
        mContext = context
        inject(mContext.appKodein())
        systemHelper = SystemHelper(context as Activity)
        playerGestureDetector = PlayerGestureDetector(context)
        playerGestureDetector?.setOnGestureListener(object : PlayerGestureDetector.OnPlayerGestureListener {
            override fun onTouchMove(region: Int, movementX: Float, movementY: Float) {
                handleTouchMove(region, movementX, movementY)
            }

            override fun onTab(region: Int) {
                //暂时没有监听这个事件的需求
            }

            override fun onTouchUp(region: Int) {
                handleTouchUp(region)
            }

        })

        mPlayNextRunner = Runnable {
            mErrorVideo = null
            mHandler.removeCallbacks(mPlayNextRunner)
            if (mLifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true) {
                playNext()
            }
        }
    }


    private fun handleTouchMove(region: Int, movementX: Float, movementY: Float) {
        if (mLocked) return
        if (mIsControlViewShowing) hideController()
        if (region == KPlayerView.REGION_LEFT) {
            val newValue = systemHelper!!.getNewBrightnessValue(movementY)
            systemHelper!!.setBrightness(movementY)
            val brightValue = "" + (newValue * 100) / 255 + "%"
            playerView?.showBrightAdjustLayout(brightValue)
        } else if (region == KPlayerView.REGION_RIGHT) {
            val newVoiceValue = systemHelper!!.getNewVolumeValue(movementY)
            systemHelper!!.setNewVolumeValue(movementY)
            val text = "" + (newVoiceValue * 100) / systemHelper!!.mMaxVolume + "%"
            playerView?.showAdjustVolumeLayout(newVoiceValue, text)
        } else if (region == KPlayerView.REGION_CENTER) {
            mIsSeeking = true
            adjustSeekStart(movementX)
        }
    }

    private fun handleTouchUp(region: Int) {
        if (mLocked) return
        if (mIsControlViewShowing) hideController()
        if (region == KPlayerView.REGION_CENTER) {
            playerView?.showFastGroupView(false)
            mIsSeeking = false
            mSeekRunner.run()
        }
        if (region == KPlayerView.REGION_RIGHT) {
            playerView?.showFastGroupView(false)
        }
        if (region == KPlayerView.REGION_LEFT) {
            playerView?.showFastGroupView(false)
        }
    }


    override fun onWifiConnected() {
        Timber.i("network connected---> wifi")
        if (mLifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == false
                || isPlaying()) return

        playerView?.hideNoNetworkLayout()
        playerView?.hideUseMobileNetLayout()
        if (isPaused() && mPreloadSuccess) {
            start()
        } else if (mCurrentVideo != null) {
            startPlayVideo(mCurrentVideo!!, mPosition)
        }
        mErrorHandleListeners.forEach {
            it.onClickRetry()
        }
    }

    override fun onMobileNetConnected() {
        Timber.i("network connected---> mobile")
        if (mCurrentVideo?.isVideoCached == true) return
        mErrorHandleListeners.forEach {
            it.onClickRetry()
        }
        playerView?.showUseMobileNetLayout()
        if (isPlaying()) {
            pause()
        }
    }

    override fun onNetworkDisConnected() {
        Timber.i("network disconnected -----")
        if (mCurrentVideo?.isVideoCached == true) return
        playerView?.showNoNetworkLayout()
        if (isPlaying()) {
            pause()
        }
    }

    override fun init(appKodein: () -> Kodein) {
        val app = mContext.applicationContext as MainApp
        orientationManager = app.kodein.instance()
        videoProxySource = app.kodein.instance()
        orientationManager.lockOrientation()
        orientationManager.addOrientationChangedListener(mo)
        spManager = app.kodein.instance()
        hideController()
        updateStatus(mCurrentStatus)
    }

    override fun attachView(view: VideoControllerView) {
        this.playerView = view

    }

    override fun attachMediaPlayer(player: FixedVideoView) {
        this.mVideoPlayer = player
        this.mVideoPlayer!!.setMediaPlayerListener(object : MediaPlayerListener {
            override fun onPrepared() {
                onPlayerPrepared()
            }

            override fun onCompletion() {
                Timber.d("onCompletion")
                stopUpdateProgress()
                hideController()
            }

            override fun onBufferingUpdate(percent: Int) {
                onBufferingUpdate(percent)
            }

            override fun onSeekComplete() {
                onPlayerSeekComplete()
            }

            override fun onVideoSizeChanged(width: Int, height: Int) {
            }

            override fun onError(what: Int, extra: Int): Boolean = onPlayerError(what, extra)

            override fun onInfo(what: Int, extra: Int): Boolean = onPlayerInfo(what, extra)
        })
    }

    override fun onPlayerPrepared() {
        Timber.d("onPlayerPrepared")
        if (mVideoPause) return
        hideController()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            updateStatus(MEDIA_STATUS_PLAYING)
            val durationText = changeMillisecondTime2Str(mVideoPlayer?.duration ?: 0)
            val positionText = changeMillisecondTime2Str(0)
            playerView?.renderSeekLayout(0, 0, durationText, positionText)
            playerView?.hideLoadingView()
            if (!mUserClick) {
                hideController()
            }
        }
        KPlayerManager.get(mContext).handleVideoSourcePlaySuccess(mCurrentVideo!!)
        if (mPosition > 5000 && mPosition < mVideoPlayer!!.duration - 5000) {
            mVideoPlayer!!.seekTo(mPosition)
        }

        mPlayListeners.forEach {
            it.onStart()
        }

        if (mLifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true) {
            mVideoPlayer?.start()
            mCurrentVideo!!.commonVideo?.saveStartTime()
        }
    }

    override fun onPlayerCompletion() {
        Timber.i("onPlayerCompletion")
        stopUpdateProgress()
        hideController()

        mPlayListeners.forEach {
            it.onComplete()
        }
    }

    override fun onPlayerSeekComplete() {
        Timber.i("onPlayerSeekComplete")
    }

    override fun onPlayerError(what: Int, extra: Int): Boolean {
        Timber.i("onPlayerError: what： $what , extra $extra")

        stopUpdateProgress()
        if (mCurrentVideo != null) {
            Timber.d("onPlayerError: current video: " + mCurrentVideo!!.commonVideo.video_title)
            KPlayerManager.get(mContext).handleVideoSourcePlayError(mCurrentVideo!!)
            if (mVideoUrlManager?.hasNext() == true) {
                val videoUrl = mVideoUrlManager!!.next()
                Timber.i("onPlayerError: try next video url: $videoUrl")
                val dataSource = DataSource.builder()
                        .url(videoUrl)
                        .build(mContext)
                mVideoPlayer!!.setDataSource(dataSource)
                mErrorVideo = null
                return false
            } else {
                Timber.i("onPlayerError: no next video url can retry")
                mErrorVideo = mCurrentVideo
            }
        } else {
            Timber.d("error: current video not find")
        }
        return false
    }

    override fun onPlayerInfo(what: Int, extra: Int): Boolean {
        Timber.i("onPlayerInfo: what： $what , extra $extra")
        if (MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START == what) {
            updateStatus(MEDIA_STATUS_PLAYING)
            if (!mUserClick) {
                hideController()
            }
        } else if (MediaPlayer.MEDIA_INFO_BUFFERING_START == what) {
            updateStatus(MEDIA_STATUS_BUFFERING)
            updatePlayingState()
        } else if (MediaPlayer.MEDIA_INFO_BUFFERING_END == what) {
            updateStatus(MEDIA_STATUS_PLAYING)
            if (!mUserClick) {
                hideController()
            }
        }
        return false
    }

    private fun setVideoDataSource() {
        Timber.i("enter setVideoDataSource()")
        if (mCurrentVideo == null) return
        mVideoUrlManager?.resetCursor()
        Timber.i("setVideoDataSource --> url size:" + mVideoUrlManager?.size())
        if (mVideoUrlManager?.hasNext() == true) {
            val url = mVideoUrlManager?.next() ?: ""
            Timber.i("setVideoDataSource url: $url") // url有可能是本地缓存路径，但是经产品确认不进行提示
            val dataSource = DataSource.builder()
                    .url(url)
                    .build(mContext)
            mVideoPlayer!!.setDataSource(dataSource)
        }
    }

    /**
     * 设置播放器播放源，此操作跟  setVideoDataSource方法一样，只是做了清晰度选择的
     * 操作，如果用户上一个播放的视频选择某种清晰度，本次会优先选择此种清晰度视频源
     */
    private fun setDataSourceByUserLastChoose() {
//        val videoResolutionChoosed = spManager?.videoResolutionChoosed
        setVideoDataSource()
    }

    private fun setProgress() {
        mNextVideoHint = ""
        if (mVideoPlayer == null || mIsSeeking) {
            setProgress(0, 0)
        }
        val position = mVideoPlayer!!.currentPosition
        val duration = mVideoPlayer!!.duration

        if (duration < 0) {
            setProgress(0, 0)
        }

        var pos: Long
        if (duration > 0) {
            // use long to avoid overflow
            pos = 1000L * position / duration
            if (mIsControlViewShowing) {
                playerView?.hideProgressBar()
            } else {
                playerView?.showProgressBar(pos.toInt(), getPlayerBufferingPercent())
            }
        }
        setProgress(position, duration)

        if (mLastPosition > position) mNeedTippedNextVideo = true
        mLastPosition = position
        if (mNeedTippedNextVideo && position > duration - 6000) {
            if (mNextVideo != null) {
                mNextVideoHint = "即将播放：" + mNextVideo!!.commonVideo.video_title
                showController()
            }
            mNeedTippedNextVideo = false
        }

        mPlayListeners.forEach {
            it.onPlayProgress(100 * position / duration)
        }
    }

    private fun setProgress(position: Int, duration: Int) {
        if (duration <= 0) {
            return
        }
        var seekBarProgress = 0
        var pos: Long = 0
        if (duration > 0) {
            // use long to avoid overflow
            pos = 1000L * position / duration
            seekBarProgress = pos.toInt()
        }
        val durationText = changeMillisecondTime2Str(duration)
        val positionText = changeMillisecondTime2Str(position)
        val bufferingProgress = getPlayerBufferingPercent()
        playerView?.renderSeekLayout(seekBarProgress, bufferingProgress, durationText, positionText)
    }

    private fun getPlayerBufferingPercent(): Int {
        return (mVideoPlayer?.bufferPercentage ?: 0) * 10
    }

    override fun start() {
        super.start()
        Timber.i("start() ---> start video")
        if (mLifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == false) return
        isVideoPausedByUserClick = false
        if (mVideoPlayer == null || !checkNetWorkOk()) {
            return
        }
        mVideoPause = false
        mVideoPlayer?.start()
//        logVideoPlayer(mContext, PlayerStatus.VIDEO_START, mVideoInfo!!.commonVideo.stat, getCurrentPosition().toLong())
        playerView?.renderPlayBtn(true)
        startUpdateProgress()

        if (mCurrentStatus == MEDIA_STATUS_INITIAL || mCurrentStatus == MEDIA_STATUS_LOADING)
            return
        mCurrentVideo?.commonVideo?.saveStartTime()
    }

    override fun pause() {
        super.pause()
        Timber.i("pause() ---> pause video")
        if (mVideoPlayer == null) {
            return
        }
        mVideoPause = true
        mVideoPlayer?.pause()
        updatePlayingState()

        if (mCurrentVideo != null) {
            mCurrentVideo!!.commonVideo.saveViewTime()
        }
    }

    private fun getCurrentPosition(): Int {
        if (mCachedSeekPosition >= 0) {
            return mCachedSeekPosition
        } else if (mVideoPlayer != null) {
            return mVideoPlayer?.currentPosition ?: 0
        }
        return 0
    }

    override fun updatePlayingState() {
        if (mVideoPlayer?.isPlaying == true) {
            mVideoPause = false
        }
        if (!mVideoPause) {
            playerView?.renderPlayBtn(true)
        } else {
            playerView?.renderPlayBtn(false)
        }
    }

    override fun playNext() {
        super.playNext()
        playNext(false)
    }

    override fun autoPlayNext() {
        if (mLifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true) {
            playNext(true)
        }
    }

    private fun playNext(isAutoPlay: Boolean) {
        if (mLifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true) {
            if (!videoProxySource.playNextVideo(isAutoPlay)) {
                playerView?.showNoNextVideoLayout()
            }
        }
    }

    override fun clickBackBtn() {
        handleFullScreen()
    }

    override fun handleFullScreen() {
        if (isFullScreen()) {
            setPlayerSize(PlayerSizeMode.PLAYER_SIZE_NORMAL)
        } else {
            setPlayerSize(PlayerSizeMode.PLAYER_SIZE_FULL_SCREEN)
        }
    }

    override fun setPlayerSize(playerSizeMode: PlayerSizeMode) {
        super.setPlayerSize(playerSizeMode)
        if (currentSize == playerSizeMode) return
        Timber.i("setPlayerSize: $playerSizeMode")
        playerView?.cancelAnimation()
        lastSize = currentSize
        currentSize = playerSizeMode
        mPlayListeners.forEach {
            it.onScreenSizeChange(playerSizeMode)
        }
        if (playerSizeMode == PlayerSizeMode.PLAYER_SIZE_FULL_SCREEN) {
            playerView?.hideBottomControlView()

        } else {
            playerView?.hideBottomControlView()
            lockController(false)
            playerView?.showLockView(false)
            playerView?.showUnLockView(false)
        }

        playerView?.onExit()
        playerView = mVideoControllerViews[playerSizeMode]
        playerView?.onEnter(mFullscreenOrientation)

        hideController()
        startUpdateProgress()
    }

    override fun onSeekStart() {
        if (mVideoPlayer == null) {
            // ignore when mPlayer is not ready.
            return
        }
        mCurrentVideo?.commonVideo?.saveViewTime()
        if (!mIsSeeking) {
            updatePlayingState()
        }
        mIsSeeking = true
        stopAutoDismiss()
    }

    override fun onSeeking(progress: Int, fromUser: Boolean) {
        if (!fromUser) {
            return
        }
//            if (!mPlayer?.canSeekBackward() || !mPlayer.canSeekForward()
//                    || mPlayer?.duration!! <= 0) {
//                // seek can not be supported.
//                return
//            }
        val duration = mVideoPlayer!!.duration
        val newPosition = duration.toLong() * progress.toLong() / 1000L

        sendSeekMessage(newPosition.toInt())
        val text = changeMillisecondTime2Str(newPosition.toInt())
        playerView?.renderPlayTimeText(text)
    }

    override fun onSeekEnd() {
        mSeekRunner.run()
        if (!mVideoPause) {
            start()
        }
        mIsSeeking = false
        startAutoDismiss()
        startUpdateProgress()
    }

    private fun stopAutoDismiss() {
        mHandler.removeCallbacks(mAutoDismiss)
    }

    private fun startAutoDismiss() {
        if (mAutoDismiss == null) {
            mAutoDismiss = Runnable {
                if (mCurrentStatus != MEDIA_STATUS_PLAYING) {
                    if (mCurrentStatus == MEDIA_STATUS_BUFFERING) {
                        if (!mIsControlViewShowing) {
                            hideController()
                        }
                    }
                    return@Runnable
                }
                mUserClick = false
                hideController()
            }
        }
        mHandler.removeCallbacks(mAutoDismiss)
        mHandler.postDelayed(mAutoDismiss, AUTO_DISMISS_TIMER.toLong())
    }

    override fun resume() {
        super.resume()
        Timber.i("resume()")
        if (mLifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == false) return
        mVideoPause = false
        if (mVideoPlayer != null && checkNetWorkOk()) {
            if (mCurrentVideo != null) {
                mCurrentVideo!!.commonVideo?.saveTotalTime()
            }
        }
    }

    override fun reset() {
        super.reset()
        Timber.i("reset")
        orientationManager.lockOrientation()
        mVideoReseted = true
        mCurrentVideo = null
        if (mVideoPlayer != null) {
            mVideoPlayer!!.stop()
            mVideoPlayer?.reset()
        }
        playerView?.showLoadingView("", false)
    }

    override fun stop() {
        super.stop()
        Timber.i("stop() -----> stop video")
        if (mVideoPlayer != null) {
            mVideoPlayer?.stop()
        }
        playSubscription?.unsubscribe()
    }

    private fun playVideo(videoInfoParams: VideoInfoParams, position: Int) {
        Timber.i("playVideo: videoInfo -> $videoInfoParams position -> $position")
        mCurrentVideo = videoInfoParams
        val useWifiResolution = NetworkManager.isUseWifiConnected(mContext)
        mVideoUrlManager = VideoUrlManager.parse(videoInfoParams, useWifiResolution)
        Timber.i("playVideo --> mVideoUrlManager size:" + mVideoUrlManager?.size() + "|" + mVideoUrlManager.toString())

        mPosition = position

        setVideoDataSource()
    }

    override fun getPlayVideoInfo(): VideoInfoParams? {
        return mCurrentVideo
    }

    override fun startPlayVideo(videoInfo: VideoInfoParams) {
        startPlayVideo(videoInfo, Observable.just(videoInfo.senderFromPosition))
    }

    override fun startPlayVideo(videoInfo: VideoInfoParams, position: Int) {
        startPlayVideo(videoInfo, Observable.just(position))
    }

    private var playSubscription: Subscription? = null

    override fun startPlayVideo(videoInfo: VideoInfoParams, position: Observable<Int>) {
        Log.d("PPP", "startPlayVideo--")
        preLoad(videoInfo)
        if (mPreloadSuccess) {
            playSubscription?.unsubscribe()
            playSubscription = KPlayerManager.get(mContext)
                    .processVideoInfoParams(videoInfo)
                    .zipWith(position) { videoParams, position ->
                        Timber.i("zip $videoParams")
                        videoParams.senderFromPosition = position
                        videoParams
                    }
                    .subscribe(object : Subscriber<VideoInfoParams>() {
                        override fun onNext(t: VideoInfoParams) {
                            Timber.i("onNext $t")
                            playVideo(t, t.senderFromPosition)
                        }

                        override fun onError(e: Throwable) {
                            Timber.e(e)
                            try {
                                var errorCode = ErrorCode.SOURCE_PLUGIN_HANDLE_ERROR
                                var errorDetail = ""
                                if (e is PluginException) {
                                    errorDetail = e.code
                                }
                                handlePlayError(e, errorCode, errorDetail)
                                var errorMsg = "${errorCode.value}${errorDetail}"
                            } catch (e: Exception) {
                                Timber.e(e)
                            }
                        }

                        override fun onCompleted() {
                        }

                    })
        }
    }

    private fun handlePlayError(e: Throwable, errorCode: ErrorCode, errorDetail: String = "") {
        playerView?.hideLoadingView()
        val message: String
        if (errorCode == ErrorCode.SOURCE_GET_ERROR) {
            message = mContext.getString(R.string.video_take_offline)
        } else {
            message = mContext.getString(R.string.video_play_failure) + ":${errorCode.value}" + "${errorDetail}"
        }
        playerView?.showErrorView(message)
        mPlayListeners.forEach { it ->
            it.onPlayError(e)
        }
    }

    /**
     * 预加载操作
     */
    private fun preLoad(videoInfo: VideoInfoParams) {
        Timber.i("preload: $videoInfo")
        playerView?.hideErrorView()
        orientationManager.unlockOrientation()
        mVideoPause = false
        mVideoReseted = false
        mVideoPause = false
        mCurrentVideo = videoInfo
        mErrorVideo = null

        //如果播放的视频是缓存的视频不需要做网络检查
        if (!videoInfo.isVideoCached && !checkNetWorkOk()) {
            mPreloadSuccess = false
            return
        }

        stopPlayNextRunner()
        //视频未结束播放，手动调用结束当前视频播放
        if (mVideoPlayer?.isPlaying == true) {
            mVideoPlayer!!.stop()
        }

        mNextVideo = videoProxySource.getNextPlayVideo(false)
        mNeedTippedNextVideo = true

        playerView?.showFastGroupView(false)
        updateNextBtn()
        updateStatus(MEDIA_STATUS_LOADING)
        playerView?.hideNoNetworkLayout()
        playerView?.hideNoNextVideoLayout()
        startUpdateProgress()
        playerView?.showProgressBar(0, 0)
        mPreloadSuccess = true
    }

    override fun checkNetWorkOk(): Boolean {
        //如果视频已经缓存，及不需要再关心网络状态
        if (mCurrentVideo?.isVideoCached == true) return true
        if (NetworkManager.isUseWifiConnected(mContext)) {
            playerView?.hideNoNetworkLayout()
            return true
        } else if (isUseMobileNetAndUserPermit()) {
            playerView?.hideUseMobileNetLayout()
//            showToast(mContext, mContext.getString(R.string.play_video_with_mobile_network))
            return true
        } else if (!NetworkManager.isNetworkConnected(mContext)) {
            if (isPlaying()) {
                pause()
            }
            playerView?.showNoNetworkLayout()
        } else {
            if (isPlaying()) {
                pause()
            }
            playerView?.showUseMobileNetLayout()
        }
        return false
    }

    private fun isUseMobileNetAndUserPermit(): Boolean {
        return NetworkManager.isUseMobileNetConnected(mContext)
                && (ignoreCheckWifi || spManager?.noWifiPlay == true)
    }


    override fun clickNoNetworkRetry() {
        super.clickNoNetworkRetry()
        if (mCurrentVideo == null) return
        if (checkNetWorkOk()) {
            if (mPreloadSuccess && isPaused()) {
                start()
            } else {
                startPlayVideo(mCurrentVideo!!, mPosition)
            }
            mErrorHandleListeners.forEach {
                it.onClickRetry()
            }
        } else {
            showToast(mContext, mContext.getString(R.string.load_data_error))
        }
    }

    override fun clickUseMobileNetContinue() {
        super.clickUseMobileNetContinue()
        ignoreCheckWifi = true
        if (checkNetWorkOk()) {
            if (isPaused() && mPreloadSuccess) {
                start()
            } else {
                if (mCurrentVideo != null) {
                    startPlayVideo(mCurrentVideo!!, mPosition)
                }
            }
            mErrorHandleListeners.forEach {
                it.onClickUseMobileContinue()
            }
        }
    }

    override fun clickPlayErrorRetry() {
        super.clickPlayErrorRetry()
        mCurrentVideo?.let {
            startPlayVideo(it)
        }
    }


    private fun updateStatus(status: Int) {
        mCurrentStatus = status
        if (status == MEDIA_STATUS_INITIAL) { // idle
            hideController()
        } else if (status == MEDIA_STATUS_LOADING) { // loading
            if (!isFullScreen()) {
                playerView?.showLoadingView(mCurrentVideo!!.commonVideo.video_title,
                        mCurrentVideo!!.commonVideo.video_image)
            } else {
                playerView?.showLoadingView(mCurrentVideo!!.commonVideo.video_title, false)
            }

        } else if (status == MEDIA_STATUS_BUFFERING) { // buffering
            hideController()
            playerView?.showLoadingView("", true)
        } else { // playing
            playerView?.hideLoadingView()
            playerView?.renderPlayBtn(true)
            if (mIsControlViewShowing) {
                playerView?.showPlayBtn()
            }
        }
    }

    override fun isPaused(): Boolean {
        return mVideoPause
    }

    override fun isFullScreen(): Boolean {
        return currentSize == PlayerSizeMode.PLAYER_SIZE_FULL_SCREEN
    }

    private fun startPlayNextRunner() {
        mHandler.postDelayed(mPlayNextRunner, 3000)
    }

    private fun stopPlayNextRunner() {
        mHandler.removeCallbacks(mPlayNextRunner)
    }

    /**
     *开始刷新进度条
     */
    private fun startUpdateProgress() {
        if (mUpdateProgressRunner == null) {
            mUpdateProgressRunner = Runnable {
                mHandler.removeCallbacks(mUpdateProgressRunner)
                if (mVideoPlayer != null) {
                    if (!mIsSeeking && mVideoPlayer!!.isPlaying) {
                        setProgress()
                        val pos = mVideoPlayer?.currentPosition ?: 0
                        mHandler.postDelayed(mUpdateProgressRunner, (1000 - pos % 1000).toLong())
                    } else {
                        mHandler.postDelayed(mUpdateProgressRunner, 1000)
                    }
                }
            }
        }
        mHandler.removeCallbacks(mUpdateProgressRunner)
        mHandler.post(mUpdateProgressRunner)
    }

    /**
     *停止刷新进度条
     */
    private fun stopUpdateProgress() {
        mHandler.removeCallbacks(mUpdateProgressRunner)
    }

    override fun showController() {
        mIsControlViewShowing = true

        playerView?.showShadow()
        if (!TextUtils.isEmpty(mNextVideoHint)) {
            playerView?.showVideoTitle(mNextVideoHint)
        } else if (isFullScreen()) {
            playerView?.showVideoTitle(mCurrentVideo?.commonVideo?.video_title ?: "")
        } else {
            //详情页模式下不显示title，只显示返回按键
            playerView?.showVideoTitle("")
        }
        playerView?.showBottomControlView()
        playerView?.hideProgressBar()
        if (mCurrentStatus == MEDIA_STATUS_BUFFERING) {
            playerView?.hidePlayBtn()
        } else {
            playerView?.showPlayBtn()
        }
        if (mNextVideo != null) {
            playerView?.showNextBtn(true)
        } else {
            playerView?.showNextBtn(false)
        }
        if (mLocked) {
            playerView?.showLockView(true)
        } else {
            playerView?.showUnLockView(true)
        }

        startUpdateProgress()
        updatePlayingState()
        startAutoDismiss()
    }

    override fun hideController() {
        mIsControlViewShowing = false
        mUserClick = false

        playerView?.hideResolutionPicker()
        playerView?.hideShadow()
        playerView?.hideBottomControlView()
        playerView?.hideVideoTitle()
        playerView?.hidePlayBtn()
        playerView?.showNextBtn(false)
        playerView?.showProgressBar()
        if (mCurrentStatus != MEDIA_STATUS_PLAYING) {
            if (mCurrentStatus == MEDIA_STATUS_BUFFERING) {
                playerView?.renderPlayBtn(true)
                playerView?.showLockView(mLocked)
            }
            return
        }
        playerView?.showLockView(mLocked)

        stopAutoDismiss()
        startUpdateProgress()
    }

    override fun lockController(locked: Boolean) {
        mLocked = locked
        if (mLocked) {
            playerView?.showLockView(mLocked)
            orientationManager.lock()
        } else {
            playerView?.showUnLockView(true)
            orientationManager.unlock()
        }
    }

    override fun updateNextBtn() {
        mNextVideo = videoProxySource.getNextPlayVideo(false)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return playerGestureDetector?.onTouchEvent(event) ?: false
    }

    private fun adjustSeekStart(movementX: Float) {
        val duration = mVideoPlayer?.duration ?: 0
        val currentPosition = getCurrentPosition()
        val stepPosition = getStepPosition(movementX, duration)

        val seekToPosition = getSeekToPosition(stepPosition, currentPosition,
                duration)
        sendSeekMessage(seekToPosition)
        val durationText = changeMillisecondTime2Str(duration)
        val seekText = changeMillisecondTime2Str(seekToPosition)

        if (stepPosition >= 0) {
            playerView?.renderFastMoveLayout(true, durationText, seekText)
        } else {
            playerView?.renderFastMoveLayout(false, durationText, seekText)
        }
    }

    private fun getSeekToPosition(stepPosition: Int, currentPosition: Int,
                                  duration: Int): Int {
        var seekToPosition = currentPosition + stepPosition
        if (stepPosition < 0) {
            if (seekToPosition < 0) {
                seekToPosition = 0
            }
        } else {
            if (seekToPosition > duration) {
                seekToPosition = duration
            }
        }
        return seekToPosition
    }

    private fun getStepPosition(movementX: Float, duration: Int): Int {
        val seekStep = mContext.resources.displayMetrics.widthPixels / 120f
        var seekPosition = (Math.abs(movementX) / seekStep * 1000).toInt()
        if (movementX < 0) {
            seekPosition = -seekPosition
        }
        return seekPosition
    }

    override fun clickLockView() {
        if (mLocked) {
            orientationManager.unlock()
            lockController(false)
            showController()
            showToast(mContext, "屏幕已解锁")
        } else {
            orientationManager.lock()
            lockController(true)
            hideController()
            showToast(mContext, "屏幕已锁定")

        }
    }

    override fun togglePause() {
        super.togglePause()
        if (mVideoPlayer == null) {
            return
        }
        if (mVideoPause) {
            start()
            mVideoPause = false
        } else {
            isVideoPausedByUserClick = true
            pause()
            mVideoPause = true
        }
    }

    override fun toggleVisible() {
        if (mLocked) return
        if (mIsControlViewShowing) {
            mIsControlViewShowing = false
            hideController()
        } else {
            mIsControlViewShowing = true
            mUserClick = true
            showController()
        }
    }

    private fun sendSeekMessage(seekTo: Int) {
        if (seekTo <= mVideoPlayer!!.duration) {
            mCachedSeekPosition = seekTo
        }
    }

    override fun release() {
        Timber.i("release")
        stopUpdateProgress()
        stopAutoDismiss()
        playSubscription?.unsubscribe()
        mVideoPlayer?.release()
        orientationManager.removeOrientationChangedListener(mo)
    }

    override fun getContext(): Context {
        return mContext
    }
}