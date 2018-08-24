package com.mivideo.mifm.share.interceptor

import com.mivideo.mifm.share.ShareHelper
import com.mivideo.mifm.socialize.ShareResult
import com.mivideo.mifm.socialize.exceptions.ShareCancelException
import com.mivideo.mifm.socialize.share.NewShareInfo

/**
 * 外部监听过滤器
 * @author LiYan
 */
class ListenerInterceptor(val listener: ShareHelper.ShareHelperListener?) : ShareHelper.ShareResultInterceptor {
    override fun interceptSuccess(shareResult: ShareResult, shareInfo: NewShareInfo): Boolean {
        return listener?.onShareSuccess() ?: false
    }

    override fun interceptError(e: Throwable, shareInfo: NewShareInfo): Boolean {
        if (e is ShareCancelException) {
            return listener?.onShareCancel() ?: false
        } else {
            return listener?.onShareError(e) ?: false
        }
    }
}