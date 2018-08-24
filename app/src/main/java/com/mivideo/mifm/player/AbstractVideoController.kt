package com.mivideo.mifm.player

import android.arch.lifecycle.Lifecycle
import com.mivideo.mifm.data.models.jsondata.common.CommonPageVideo

/**
 * 抽象播放器控制器
 *
 * @author LiYan
 */
abstract class AbstractVideoController : VideoController {
    protected var currentSize: PlayerSizeMode = PlayerSizeMode.PLAYER_SIZE_NORMAL
    protected var playerView: VideoControllerView? = null
    protected var mVideoPlayer: FixedVideoView? = null

    protected var mPlayListeners = ArrayList<PlayListener>()
    protected var mVideoControllerViews = HashMap<PlayerSizeMode, VideoControllerView>()

    protected var mErrorHandleListeners = ArrayList<ErrorHandleListener>()

    //视频是否暂停
    protected var mVideoPause = false

    override var isVideoPausedByUserClick: Boolean = false


    override var mLifecycle: Lifecycle? = null

    override var enableOrientationListener: Boolean = true

    override fun setRepeatPlayVideo(enable: Boolean) {
        mVideoPlayer?.loopPlayVideo = enable
    }

    override fun attachView(view: VideoControllerView) {
        this.playerView = view

    }

    override fun attachMediaPlayer(player: FixedVideoView) {
        this.mVideoPlayer = player
    }

    override fun getView(): VideoControllerView? {
        return playerView
    }

    override fun addPlayListener(listener: PlayListener) {
        mPlayListeners.add(listener)
    }

    override fun removePlayListener(listener: PlayListener) {
        mPlayListeners.remove(listener)
    }

    override fun addErrorListener(listener: ErrorHandleListener) {
        mErrorHandleListeners.add(listener)
    }

    override fun removeErrorListener(listener: ErrorHandleListener) {
        mErrorHandleListeners.remove(listener)
    }

    override fun putVideoControllerView(sizeMode: PlayerSizeMode, view: VideoControllerView) {
        mVideoControllerViews.put(sizeMode, view)
    }

    override fun removeVideoControllerView(sizeMode: PlayerSizeMode) {
        mVideoControllerViews.remove(sizeMode)

    }

    override fun isPlaying(): Boolean {
        return mVideoPlayer?.isPlaying == true
    }

    override fun isPaused(): Boolean {
        return mVideoPause
    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun reset() {

    }

    override fun start() {

    }

    override fun stop() {

    }

    override fun playNext() {

    }

    override fun setPlayerSize(playerSizeMode: PlayerSizeMode) {

    }

    /**
     * 开始seek操作的回调
     */
    override fun onSeekStart() {

    }

    /**
     * 正在seek操作回调
     */
    override fun onSeeking(progress: Int, fromUser: Boolean) {

    }

    /**
     * seek操作结束回调
     */
    override fun onSeekEnd() {

    }

    /**
     * 控制视频暂停
     */
    override fun togglePause() {

    }

    /**
     * 锁定屏幕
     */
    override fun lockController(locked: Boolean) {

    }

    override fun updateNextBtn() {

    }

    override fun clickLockView() {

    }

    override fun clickBackBtn() {

    }

    /**
     * 当播放错误时，点击播放错误重试
     */
    override fun clickPlayErrorRetry() {

    }


    /**
     * 当无网络网络切换或播放错误会在播放界面上显示“重试”
     * 此方法用于中间按钮点击处理
     */
    override fun clickNoNetworkRetry() {

    }

    /**
     * 当无网络网络切换或播放错误会在播放界面上显示“继续”
     * 此方法用于中间按钮点击处理
     */
    override fun clickUseMobileNetContinue() {

    }

    override var playerLifecycleObserver: PlayerLifecycleObserver? = null

    override fun addLifecycleInterceptor(playerLifecycleInterceptor: PlayerLifecycleInterceptor) {
        playerLifecycleObserver?.addInterceptor(playerLifecycleInterceptor)
    }

    override fun removeLifecycleInterceptor(playerLifecycleInterceptor: PlayerLifecycleInterceptor) {
        playerLifecycleObserver?.removeInterceptor(playerLifecycleInterceptor)
    }

    override var playTabId: String = ""
    override var commonPageVideo: CommonPageVideo? = null

    override fun updateData(tabId: String, video: CommonPageVideo) {
        playTabId = tabId
        commonPageVideo = video
    }

}