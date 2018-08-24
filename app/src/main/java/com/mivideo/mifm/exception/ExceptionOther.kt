package com.mivideo.mifm.exception

import okhttp3.Response
import timber.log.Timber

/**
 * Created by aaron on 2017/2/21.
 */
class ExceptionOther : HttpException {
    private var response: Response? = null

    constructor(response: Response) : super("") {
        this.response = response
        Timber.i({ "请求网络出现错误:${response}" }.invoke())
    }

    override fun getCode(): Int {
        return this.response?.code() ?: -1
    }

}