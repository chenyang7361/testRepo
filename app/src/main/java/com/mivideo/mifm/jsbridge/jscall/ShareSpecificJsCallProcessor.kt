package com.mivideo.mifm.jsbridge.jscall


import com.mivideo.mifm.jsbridge.ComponentProvider
import com.mivideo.mifm.share.ShareHelper
import com.mivideo.mifm.socialize.share.NewShareInfo
import me.yamlee.jsbridge.BaseJsCallProcessor
import me.yamlee.jsbridge.BaseJsCallResponse
import me.yamlee.jsbridge.JsCallData
import me.yamlee.jsbridge.WVJBWebViewClient

/**
 * 通过具体方式如指定微信或QQ等进行分享
 * @author LiYan
 */
class ShareSpecificJsCallProcessor(val provider: ComponentProvider) : BaseJsCallProcessor(provider) {
    companion object {
        const val FUNC_NAME = "shareSpecific"
        /**
         * 微信
         */
        const val TYPE_WX = "wx"
        /**
         * 微信朋友圈
         */
        const val TYPE_WX_MOMENTS = "wx-moments"
        /**
         * QQ
         */
        const val TYPE_QQ = "qq"
        /**
         * QQ空间
         */
        const val TYPE_QQ_ZONE = "qq-zone"
        /**
         * 微博
         */
        const val TYPE_WEIBO = "weibo"
        /**
         * 赋值链接
         */
        const val TYPE_URL = "url"

    }

    private var responseCallback: WVJBWebViewClient.WVJBResponseCallback? = null

    private var shareListener = object : ShareHelper.ShareHelperListener() {
        override fun onShareSuccess(): Boolean {
            val response = ShareSpecificResponse()
            response.ret = "ok"
            responseCallback?.callback(response)
            return super.onShareSuccess()
        }

        override fun onShareCancel(): Boolean {
            val response = ShareSpecificResponse()
            response.ret = "cancel"
            responseCallback?.callback(response)
            return super.onShareCancel()
        }

        override fun onShareError(e: Throwable): Boolean {
            val response = ShareSpecificResponse()
            response.ret = "fail"
            responseCallback?.callback(response)
            return super.onShareError(e)
        }
    }
    private val shareHelper = provider.provideShareHelper()

    init {
        shareHelper.addShareListener(shareListener)
    }

    override fun getFuncName(): String = FUNC_NAME


    override fun onHandleJsQuest(callData: JsCallData?): Boolean {
        if (callData?.func == FUNC_NAME) {
            val request = convertJsonToObject(callData.params, ShareSpecificRequest::class.java)
            val shareInfo = NewShareInfo()
            shareInfo.targetUrl = request.targetUrl ?: ""
            shareInfo.title = request.title ?: ""
            shareInfo.description = request.description ?: ""
            shareInfo.shareImageUrl = request.icon ?: ""
            shareInfo.shareContentType = request.contentType ?: ""
            shareInfo.textContent = request.textContent ?: ""

            if (TYPE_QQ == request.type) {
                shareHelper.shareQQ(shareInfo)
            } else if (TYPE_QQ_ZONE == request.type) {
                shareHelper.shareQQZone(shareInfo)
            } else if (TYPE_WX == request.type) {
                shareHelper.shareWx(shareInfo)
            } else if (TYPE_WX_MOMENTS == request.type) {
                shareHelper.shareWxMoments(shareInfo)
            } else if (TYPE_WEIBO == request.type) {
                shareHelper.shareWeiBo(shareInfo)
            } else if (TYPE_URL == request.type) {
                shareHelper.copyUrl(shareInfo, shareListener)
            } else {
                return false
            }
            return true
        }
        return false
    }

    override fun onResponse(callback: WVJBWebViewClient.WVJBResponseCallback?): Boolean {
        responseCallback = callback
        return true
    }


    inner class ShareSpecificRequest {
        var type: String = ""
        var contentType: String? = "link"
        var textContent: String? = ""
        var title: String? = ""
        var description: String? = ""
        var icon: String? = ""
        var targetUrl: String? = ""
    }

    inner class ShareSpecificResponse : BaseJsCallResponse() {
        var msg: String = ""
    }
}