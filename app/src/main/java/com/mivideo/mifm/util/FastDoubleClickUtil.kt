package com.mivideo.mifm.util

/**
 * View点击的时候判断屏蔽快速点击事件
 */
object FastDoubleClickUtil {

    // 防止快速点击默认等待时长为1000ms
    private var intervalTime: Long = 1000L
    private var lastClickTime: Long = 0

    @Synchronized
    fun isFastDoubleClick(): Boolean {
        var currentTime = System.currentTimeMillis()
        var time = currentTime - lastClickTime

        if (time in 1..(intervalTime - 1)) {
            return true
        }
        lastClickTime = currentTime
        return false
    }

    /**
     * 设置默认快速点击事件时间间隔
     *
     * @param intervalTime
     */
    fun setIntervalTime(intervalTime: Long) {
        if (intervalTime > 100) {
            this.intervalTime = intervalTime
        }
    }
}
