package com.mivideo.mifm.player

import android.arch.lifecycle.Lifecycle
import android.content.Context
import android.view.MotionEvent
import com.github.salomonbrys.kodein.Kodein
import com.mivideo.mifm.data.models.jsondata.common.CommonPageVideo
import com.mivideo.mifm.data.models.jsondata.common.VideoInfoParams
import rx.Observable

/**
 * 播放器UI控制器通用接口
 * @author LiYan
 */
interface VideoController {

    /**
     *  判断用户是否手动点击暂停按钮暂停视频
     */
    var isVideoPausedByUserClick: Boolean

    var mLifecycle: Lifecycle?
    var playerLifecycleObserver: PlayerLifecycleObserver?
    /**
     * 设置是否手机方向转换，播控屏幕对应切换
     */
    var enableOrientationListener: Boolean

    var playTabId: String
    var commonPageVideo: CommonPageVideo?

    /**
     * 设置是否允许循环播放
     */
    fun setRepeatPlayVideo(enable: Boolean)

    /**
     * 获取Android Context
     */
    fun getContext(): Context

    /**
     * 播放视频
     */
    fun startPlayVideo(videoInfo: VideoInfoParams)

    /**
     * 播放视频
     */
    fun startPlayVideo(videoInfo: VideoInfoParams, position: Int)

    /**
     * 加载视频信息，异步加载播放开始值，开始播放视频
     */
    fun startPlayVideo(videoInfo: VideoInfoParams, position: Observable<Int>)

    /**
     * 播放器开始播放
     */
    fun start()

    /**
     * 暂停播放
     */
    fun pause()

    /**
     * 恢复播放
     */
    fun resume()

    /**
     * 停止播放
     */
    fun stop()

    /**
     * 重置播放
     */
    fun reset()

    /**
     * 修改播放器尺寸
     */
    fun handleFullScreen()

    /**
     * 设置播放器尺寸模式
     *
     * @see PlayerSizeMode
     * @param playerSizeMode
     *
     *
     */
    fun setPlayerSize(playerSizeMode: PlayerSizeMode)

    /**
     * 设置播放器错误情况处理监听器
     */
    fun addErrorListener(listener: ErrorHandleListener)

    /**
     * 移除播放器错误情况处理监听器
     */
    fun removeErrorListener(listener: ErrorHandleListener)

    /**
     * 此方法需要在初始化执行
     */
    fun init(appKodein: () -> Kodein)

    /**
     * 设置播控UI
     */
    fun attachView(view: VideoControllerView)

    /**
     * 设置播放器
     */
    fun attachMediaPlayer(player: FixedVideoView)

    fun getView(): VideoControllerView?

    /**
     * 获取当前播放视频信息
     */
    fun getPlayVideoInfo(): VideoInfoParams?

    /**
     * 播放下一个视频
     */
    fun playNext()

    /**
     * 自动播放下一个视频
     */
    fun autoPlayNext()


    fun updatePlayingState()

    fun onPlayerPrepared()

    fun onPlayerCompletion()

    fun onPlayerSeekComplete()

    fun onPlayerError(what: Int, extra: Int): Boolean

    fun onPlayerInfo(what: Int, extra: Int): Boolean

    /**
     * 当前视频是否正在暂停
     */
    fun isPaused(): Boolean

    /**
     * 当前视频是否正在播放
     */
    fun isPlaying(): Boolean

    /**
     * 判断当前是否是全屏模式
     */
    fun isFullScreen(): Boolean

    /**
     * 显示播控
     */
    fun showController()

    /**
     * 隐藏播控
     */
    fun hideController()

    fun addPlayListener(listener: PlayListener)

    fun removePlayListener(listener: PlayListener)

    fun onTouchEvent(event: MotionEvent): Boolean

    /**
     * 开始seek操作的回调
     */
    fun onSeekStart()

    /**
     * 正在seek操作回调
     */
    fun onSeeking(progress: Int, fromUser: Boolean)

    /**
     * seek操作结束回调
     */
    fun onSeekEnd()

    /**
     * 控制视频暂停
     */
    fun togglePause()

    /**
     * 控制显示播控
     */
    fun toggleVisible()

    /**
     * 锁定屏幕
     */
    fun lockController(locked: Boolean)

    fun updateNextBtn()

    /**
     * 点击返回按钮
     */
    fun clickBackBtn()

    /**
     * 点击锁定播控按钮
     */
    fun clickLockView()

    /**
     * 当播放错误时，点击播放错误重试
     */
    fun clickPlayErrorRetry()

    /**
     * 当无网络网络切换或播放错误会在播放界面上显示“重试”
     * 此方法用于中间按钮点击处理
     */
    fun clickNoNetworkRetry()

    /**
     * 当无网络网络切换或播放错误会在播放界面上显示“继续”
     * 此方法用于中间按钮点击处理
     */
    fun clickUseMobileNetContinue()

    /**
     * 外部监听到网络断开连接调用此方法，播放器内部会显示
     * 对应的网络断开界面
     */
    fun onNetworkDisConnected()

    /**
     * 外部监听到wifi连接时调用此方法，播放器会自动继续播放
     */
    fun onWifiConnected()

    /**
     * 通过手机移动网络时调用此方法，播放器内部会做相应处理
     */
    fun onMobileNetConnected()

    /**
     * 网络检查连接正常
     */
    fun checkNetWorkOk(): Boolean

    fun release()

    fun addLifecycleInterceptor(playerLifecycleInterceptor: PlayerLifecycleInterceptor)

    fun removeLifecycleInterceptor(playerLifecycleInterceptor: PlayerLifecycleInterceptor)

    /**
     * 添加播控View
     */
    fun putVideoControllerView(sizeMode: PlayerSizeMode, view: VideoControllerView)

    fun removeVideoControllerView(sizeMode: PlayerSizeMode)

    fun updateData(tabId: String, video: CommonPageVideo)
}