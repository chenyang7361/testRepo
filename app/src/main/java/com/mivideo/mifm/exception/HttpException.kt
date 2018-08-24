package com.mivideo.mifm.exception

/**
 *
 * @author LiYan
 */
abstract class HttpException(msg: String) : Exception(msg) {
    abstract fun getCode(): Int

    open fun getErrorMsg(): String {
        return message ?: ""
    }

    open fun getHintMsg(): String {
        return ""
    }
}