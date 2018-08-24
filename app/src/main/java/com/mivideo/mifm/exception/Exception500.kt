package com.mivideo.mifm.exception

import timber.log.Timber

/**
 * Created by aaron on 2017/2/21.
 */
class Exception500 : HttpException {


    companion object {
        val EXCEPTION_MSG_500 = "服务器出现500错误啦..."
    }

    constructor() : super(EXCEPTION_MSG_500) {
        Timber.e("Exception500", EXCEPTION_MSG_500)
    }

    constructor(message: String?) : super(EXCEPTION_MSG_500) {
        Timber.e("Exception500", EXCEPTION_MSG_500)
    }

    override fun getCode(): Int {
        return 500
    }
}