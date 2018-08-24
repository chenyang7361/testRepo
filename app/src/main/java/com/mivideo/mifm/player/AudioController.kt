package com.mivideo.mifm.player

/**
 * 播放器UI控制器通用接口
 * Create by KevinTu on 2018/8/16
 */
interface AudioController {

    /**
     * 播放器开始播放
     */
    fun start()

    /**
     * 暂停播放
     */
    fun pause()

    /**
     * 添加AudioControllerView
     */
    fun attachControllerView(controllerView: AudioControllerView)

    /**
     * 移除AudioControllerView
     */
    fun dettachControllerView()

    /**
     * 播放下一个视频
     */
    fun playNext()

    /**
     * 播放上一个视频
     */
    fun playLast()

    /**
     * 当前视频是否正在暂停
     */
    fun isPaused(): Boolean

    /**
     * 当前视频是否正在播放
     */
    fun isPlaying(): Boolean

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
     * 倍速播放
     */
    fun speedUp(speed: Float)
}