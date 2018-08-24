package com.mivideo.mifm.player

/**
 * 播放器尺寸模式
 * PLAYER_SIZE_FULL_SCREEN，PLAYER_SIZE_NORMAL，PLAYER_SIZE_MINI
 */
enum class PlayerSizeMode {
    /**
     * 播放器全屏模式
     */
    PLAYER_SIZE_FULL_SCREEN,
    /**
     * 播放器正常大小模式
     */
    PLAYER_SIZE_NORMAL,
    /**
     * 播放器小窗模式
     *
     * 小窗状态下不包含播控布局
     */
//    PLAYER_SIZE_MINI,

    /**
     * 播放器内联模式
     */
//    PLAYER_SIZE_INLINE,

    /**
     * 小视频播放器尺寸，基础样式为全屏模式
     */
//    PLAYER_SIZE_SMALL_VIDEO,
}


class PlayerStatus {
    companion object {
        // 视频开始播放
        var VIDEO_START = "play"
        // 视频暂停播放
        val VIDEO_PAUSE = "pause"
        // 视频重新开始播放
        val VIDEO_RESUME = "resume"
        // 视频播放错误
        val VIDEO_ERROR = "error"
        // 视频播放中离开
        val VIDEO_END = "end"
        // 视频播放完成
        val VIDEO_FINISH = "finish"
        // 跳转到指定位置播放
        val VIDEO_SEEK = "seek"
        // 浏览
        val VIDEO_VIEW = "view"
    }
}