package com.mivideo.mifm.player

/**
 * 播放器View控件通用接口
 * @author LiYan
 */
interface VideoControllerView : ControllerView {

    /**
     * 设置播放器UI控件点击事件
     */
    fun setPlayerUIListener(listener: PlayerUIListener?)

    /**
     * 获取播放器控制器
     *
     * @see VideoController
     */
    fun getController(): VideoController

    /**
     * 设置播放器控制器
     */
    fun setController(controller: VideoController)

    /**
     * 设置视频标题
     */
    fun showVideoTitle(videoTitle: String)

    /**
     * 隐藏视频标题
     */
    fun hideVideoTitle()


    fun renderSeekLayout(seekBarProgress: Int,
                         bufferingProgress: Int,
                         durationText: String,
                         positionText: String)

    /**
     * 显示播控布局
     *
     * 调用此方法会在播放界面显示播放进度条，下一个按钮等控件
     *
     */
    fun showBottomControlView()

    /**
     * 隐藏播控布局
     *
     * 调用此方法会隐藏播控布局，如在小窗状态下就不需要播控布局
     */
    fun hideBottomControlView()

    /**
     * 显示播放下一个无下个内容时布局
     */
    fun showNoNextVideoLayout()

    /**
     * 隐藏播放下一个无下个内容时布局
     */
    fun hideNoNextVideoLayout()

    /**
     * 当前播放时间设置
     */
    fun renderPlayTimeText(text: String)

    /**
     * 显示无播控时会在视频底部展示一个视频播放进度条
     */
    fun showProgressBar(percent: Int, bufferingPercent: Int)

    fun showProgressBar()

    /**
     * 隐藏视频播放进度条
     */
    fun hideProgressBar()

    /**
     * 显示播放器视频加载loading，背景为封面图
     * @param title loading下面会有一个title，如需设置就传入相应的文案
     * @param coverUrl 视屏封面图
     */
    fun showLoadingView(title: String,
                        coverUrl: String)

    /**
     * 显示播放器视频加载loading
     * @param title loading下面会有一个title，如需设置就传入相应的文案
     * @param transparentBackground true表示loading会有黑色背景，false表示透明背景
     */
    fun showLoadingView(title: String,
                        transparentBackground: Boolean)

    /**
     * 隐藏播放器视频加载loading
     */
    fun hideLoadingView()

    /**
     * 显示暂停/播放按钮
     */
    fun showPlayBtn()

    /**
     * 隐藏暂停/播放按钮
     */
    fun hidePlayBtn()

    /**
     * 设置暂停/播放按钮上对应的icon
     */
    fun renderPlayBtn(isPlaying: Boolean)

    /**
     * 设置是否显示快进快退布局（在全屏状态下左滑右滑会显示此种布局）
     */
    fun showFastGroupView(setShow: Boolean)

    /**
     * 设置是否显示下一个按钮
     */
    fun showNextBtn(setShow: Boolean)

    /**
     * 设置是否显示锁定状态icon
     */
    fun showLockView(setShow: Boolean)

    /**
     * 设置是否显示解锁状态icon
     */
    fun showUnLockView(setShow: Boolean)

    fun setSeekBarEnable(enable: Boolean)

    /**
     * 用户左滑右滑快进快退操作时ui展示
     * @param fastFaward true表示快进，false表示快退
     * @param durationText 视频时长文案如 :09:45
     * @param seekText 快进或快退操作最终位置的时间 如：05：30，
     *                  与durationText组合就是05：30 / 09:45
     */
    fun renderFastMoveLayout(fastFaward: Boolean,
                             durationText: String,
                             seekText: String)

    /**
     * 用户在屏幕左半部分上下滑动调整亮度对应的ui展示
     */
    fun showBrightAdjustLayout(brightValue: String)

    /**
     * 用户在屏幕右半部分调整英两对应的文字显示
     */
    fun showAdjustVolumeLayout(newValue: Int, volumeValue: String)


    fun cancelAnimation()

    fun renderVideoTitle(videoTitle: String)

    /**
     * 隐藏播控显示的透明阴影背景
     */
    fun hideShadow()

    /**
     * 显示播控显示的透明阴影背景
     */
    fun showShadow()

    /**
     * 隐藏清晰度选择弹框
     */
    fun hideResolutionPicker()

    /**
     * 设置指定清晰度文案
     */
    fun renderResolutionText(text: String, enable: Boolean)
}