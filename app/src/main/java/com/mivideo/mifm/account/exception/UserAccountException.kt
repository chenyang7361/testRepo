package com.mivideo.mifm.account.exception

/**
 * 通用用户账户异常
 */
open class UserAccountException : RuntimeException {

    var type = ACCOUNT_EXCEPTION_UNKNOWN

    constructor() : super() {
    }

    constructor(detailMessage: String) : super(detailMessage) {
    }

    constructor(type: Int, detailMessage: String) : super(detailMessage) {
        this.type = type
    }

    constructor(detailMessage: String, throwable: Throwable) : super(detailMessage, throwable) {
    }

    companion object {
        val ACCOUNT_EXCEPTION_UNKNOWN = 0
        val ACCOUNT_EXCEPTION_NOT_LOGIN = 1
    }
}
