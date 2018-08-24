package com.mivideo.mifm.exception

import android.content.Context
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber

/**
 * Created by aaron on 2017/2/21.
 */
class Exception214 : HttpException {
    override fun getCode(): Int {
        return 214
    }

    constructor(mContext: Context, result: String) : super("214") {
        mContext.runOnUiThread {
            Timber.i("服务接口数据错误： $result")
        }
    }

}