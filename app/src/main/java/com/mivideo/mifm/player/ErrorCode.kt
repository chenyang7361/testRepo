package com.mivideo.mifm.player

/**
 * 播放器播放错误代码
 *
 * @author LiYan
 */
enum class ErrorCode(val value: String) {
    SOURCE_GET_ERROR("000"),
    SOURCE_PLUGIN_HANDLE_ERROR("001"),
    MEDIA_CODEC_ERROR("100"),
    RENDER_ERROR("200"),
    UNKNOWN_ERROR("300"),

}