package com.mivideo.mifm.jsbridge.jscall


import com.mivideo.mifm.jsbridge.CustomWebActionView
import com.mivideo.mifm.share.ShareHelper
import com.mivideo.mifm.socialize.share.NewShareInfo
import me.yamlee.jsbridge.*

/**
 * JsBridge分享能力处理器
 * @author LiYan
 */
class ShareJsCallProcessor(provider: NativeComponentProvider) : BaseJsCallProcessor(provider) {
    companion object {
        const val FUNC_NAME = "share"
    }

    private var mCallback: WVJBWebViewClient.WVJBResponseCallback? = null
    private var shareListener = object : ShareHelper.ShareHelperListener() {
        override fun onShareSuccess(): Boolean {
            val response = ShareResponse()
            response.ret = "ok"
            mCallback?.callback(response)
            return super.onShareSuccess()
        }

        override fun onShareCancel(): Boolean {
            val response = ShareResponse()
            response.ret = "cancel"
            mCallback?.callback(response)
            return super.onShareCancel()
        }

        override fun onShareError(e: Throwable): Boolean {
            val response = ShareResponse()
            response.ret = "fail"
            mCallback?.callback(response)
            return super.onShareError(e)
        }
    }

    override fun getFuncName(): String {
        return FUNC_NAME
    }

    override fun onHandleJsQuest(callData: JsCallData?): Boolean {
        if (callData?.func == FUNC_NAME) {
            val shareRequest = convertJsonToObject(callData.params, ShareRequest::class.java)
            val shareInfo = NewShareInfo()
            shareInfo.shareContentType = shareRequest.contentType ?: NewShareInfo.SHARE_CONTENT_TYPE_LINK
            shareInfo.textContent = shareRequest.textContent ?: ""
            shareInfo.targetUrl = shareRequest.targetUrl ?: ""
            shareInfo.title = shareRequest.title ?: ""
            shareInfo.description = shareRequest.description ?: ""
            shareInfo.shareImageUrl = shareRequest.icon ?: ""
            val view = componentProvider.provideWebLogicView() as CustomWebActionView
            view.showShareDialog(shareInfo, shareListener)
            return true
        }
        return false
    }

    override fun onResponse(callback: WVJBWebViewClient.WVJBResponseCallback?): Boolean {
        mCallback = callback
        return true
    }

    inner class ShareRequest {
        var contentType: String? = NewShareInfo.SHARE_CONTENT_TYPE_LINK
        var textContent: String? = ""
        var title: String? = ""
        var description: String? = ""
        var icon: String? = ""
        var targetUrl: String? = ""
    }

    inner class ShareResponse : BaseJsCallResponse() {
    }
}