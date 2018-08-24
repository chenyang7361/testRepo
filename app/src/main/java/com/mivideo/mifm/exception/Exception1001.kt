package com.mivideo.mifm.exception
import android.content.Context
import com.mivideo.mifm.util.app.showToast
import org.jetbrains.anko.runOnUiThread

/**
 * Created by aaron on 2017/2/21.
 */
class Exception1001 : HttpException {

    companion object {
        val EXCEPTION_MSG_1001 = "当前系统繁忙，请稍后再试..."
    }

    constructor(mContext: Context) : super(EXCEPTION_MSG_1001) {
        mContext.runOnUiThread {
            showToast(mContext, EXCEPTION_MSG_1001)
        }
    }

    override fun getCode(): Int {
        return 1001
    }


}