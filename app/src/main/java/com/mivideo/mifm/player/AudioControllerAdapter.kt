package com.mivideo.mifm.player

/**
 * Created by Jiwei Yuan on 18-8-16.
 */

open class AudioControllerAdapter : AudioController {
    /**
     * 倍速播放
     */
    override fun speedUp(speed: Float) {
    }

    /**
     * 播放器开始播放
     */
    override fun start() {
    }

    /**
     * 暂停播放
     */
    override fun pause() {
    }

    /**
     * 添加AudioControllerView
     */
    override fun attachControllerView(controllerView: AudioControllerView) {
    }

    /**
     * 移除AudioControllerView
     */
    override fun dettachControllerView() {
    }

    /**
     * 播放下一个视频
     */
    override fun playNext() {
    }

    /**
     * 播放上一个视频
     */
    override fun playLast() {
    }

    /**
     * 当前视频是否正在暂停
     */
    override fun isPaused(): Boolean {
        return false
    }

    /**
     * 当前视频是否正在播放
     */
    override fun isPlaying(): Boolean {
        return false
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

    /**
     * 外部监听到网络断开连接调用此方法，播放器内部会显示
     * 对应的网络断开界面
     */
    override fun onNetworkDisConnected() {
    }

    /**
     * 外部监听到wifi连接时调用此方法，播放器会自动继续播放
     */
    override fun onWifiConnected() {
    }

    /**
     * 通过手机移动网络时调用此方法，播放器内部会做相应处理
     */
    override fun onMobileNetConnected() {
    }

}