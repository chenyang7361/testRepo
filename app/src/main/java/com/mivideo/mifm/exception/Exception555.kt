package com.mivideo.mifm.exception

import android.content.Context
import com.mivideo.mifm.R
import com.mivideo.mifm.util.app.showToast
import timber.log.Timber

class Exception555 : Exception {

    companion object {
        val EXCEPTION_MSG_555 = "服务正在维护，请稍后重试！"
    }

    constructor(context: Context) : super(EXCEPTION_MSG_555) {
        showToast(context, context.getString(R.string.server_maintained))
        Timber.e("Exception555", EXCEPTION_MSG_555)
    }
}