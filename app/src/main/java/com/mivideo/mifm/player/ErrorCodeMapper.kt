package com.mivideo.mifm.player

/**
 * 错误码转换器接口，对于不同播放器，错误码规范可能不一样，但是KPlayer
 * 对外提供统一的错误码，这里就需要不同播放器的实现转换一下错误码
 * @author LiYan
 */
interface ErrorCodeMapper {
    fun map(what: Int, extra: Int): ErrorCode
}