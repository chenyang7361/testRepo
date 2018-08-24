package com.mivideo.mifm.player

import com.mivideo.mifm.data.models.AudioInfo

/**
 * 播放器播控UI通用接口
 * Create by KevinTu on 2018/8/16
 */
interface AudioControllerView {

    /**
     * 显示无网情况下的布局
     */
    fun showNoNetworkLayout()

    /**
     * 隐藏无网情况下播放器上的布局
     */
    fun hideNoNetworkLayout()

    /**
     * 使用移动网络时,提示用户使用流量播放布局
     */
    fun showUseMobileNetLayout()

    /**
     * 隐藏流量播放提示布局
     */
    fun hideUseMobileNetLayout()

    /**
     * 显示播放器视频加载loading
     */
    fun showLoadingView()

    /**
     * 隐藏播放器视频加载loading
     */
    fun hideLoadingView()

    /**
     * 显示错误提示界面
     */
    fun showErrorView(message: String)

    /**
     * 隐藏错误提示界面
     */
    fun hideErrorView()

    /**
     * 更新上一个按钮状态
     */
    fun updateLastBtnStatus(canClick: Boolean)

    /**
     * 更新上一个按钮状态
     */
    fun updateNextBtnStatus(canClick: Boolean)

    /**
     * 显示暂停/播放按钮
     */
    fun showPlayBtn(isPlaying: Boolean)

    /**
     * 更新当前audio信息
     */
    fun updateCurrentAudio(audioInfo: AudioInfo)

    /**
     * 当前播放时间设置
     */
    fun renderPlayTimeText(timeStr: String)


    /**
     * 设置seek操作对应的进度和时间改变布局
     */
    fun renderSeekLayout(seekBarProgress: Int,
                         bufferingProgress: Int,
                         durationText: String,
                         positionText: String)


//    fun onError(what: Int, extra: Int): Boolean

    fun onInfo(what: Int, extra: Int): Boolean

    fun onSeekComplete()

    fun onPrepared()

    fun onCompletion()
}