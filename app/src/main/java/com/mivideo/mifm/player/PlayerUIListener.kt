package com.mivideo.mifm.player

/**
 * 播放器UI按钮点击等监听器
 * @author LiYan
 */
open class PlayerUIListener {

    open fun onClickBackBtn(): Boolean {
        return false
    }

    open fun onClickFullScreen(): Boolean {
        return false
    }

    open fun onSingleClick(): Boolean {
        return false
    }

    open fun onDoubleClick(): Boolean {
        return false
    }
}