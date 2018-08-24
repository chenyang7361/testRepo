package com.mivideo.mifm.util

object FastClickUtils {

    private var lastClickTime: Long = 0//上次点击的时间

    private val spaceTime = 1000//时间间隔

    val isFastClick: Boolean
        get() {
            val currentTime = System.currentTimeMillis()
            val isFastClick: Boolean

            isFastClick = currentTime - lastClickTime <= spaceTime
            lastClickTime = currentTime
            return isFastClick
        }

}
