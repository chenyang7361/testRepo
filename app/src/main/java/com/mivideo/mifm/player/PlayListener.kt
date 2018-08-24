package com.mivideo.mifm.player

/**
 * 播放监听器
 *
 * @author LiYan
 */
abstract class PlayListener {
    /**
     * 监听播放器开始播放
     */
    open fun onStart() {

    }

    /**
     * 监听播放器暂停播放
     */
    open fun onPause() {

    }

    /**
     * 监听播放器播放结束
     */
    open fun onComplete() {

    }

    /**
     * 播放进度回调
     */
    open fun onPlayProgress(percent: Int) {

    }

    /**
     * 播放器播放错误回调
     */
    open fun onPlayError(e: Throwable) {

    }

    /**
     * 播放器尺寸变化是回调
     */
    open fun onScreenSizeChange(playerSizeMode: PlayerSizeMode) {

    }
}