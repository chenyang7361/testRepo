package com.mivideo.mifm.jsbridge.jscall

import com.mivideo.mifm.jsbridge.ComponentProvider
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.socialize.AuthResult
import com.mivideo.mifm.socialize.exceptions.SocialAppNotInstallException
import me.yamlee.jsbridge.BaseJsCallProcessor
import me.yamlee.jsbridge.BaseJsCallResponse
import me.yamlee.jsbridge.JsCallData
import me.yamlee.jsbridge.WVJBWebViewClient
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber

/**
 * 微信登录验证JsBridge接口支持
 * @author LiYan
 */
class WxAuthProcessor(componentProvider: ComponentProvider) : BaseJsCallProcessor(componentProvider) {
    companion object {
        const val FUNC_NAME = "wxAuth"
    }

    private var responseCallback: WVJBWebViewClient.WVJBResponseCallback? = null

    override fun getFuncName(): String {
        return FUNC_NAME
    }

    override fun onHandleJsQuest(callData: JsCallData?): Boolean {
        if (callData?.func == FUNC_NAME) {
            val socialManager = (componentProvider as ComponentProvider).provideSocialManager()
            //TODO
//            socialManager.wx().loginAuth()
//                    .compose(asyncSchedulers())
//                    .subscribe(object : Subscriber<AuthResult>() {
//                        override fun onNext(result: AuthResult?) {
//                            Timber.i("bridge wx auth result $result")
//                            val response = WxAuthResponse()
//                            response.authCode = result?.authCode ?: ""
//                            response.ret = "ok"
//                            responseCallback?.callback(response)
//                        }
//
//                        override fun onCompleted() {
//                        }
//
//                        override fun onError(e: Throwable?) {
//                            Timber.e(e)
//                            val response = WxAuthResponse()
//                            response.ret = "fail"
//                            if (e is SocialAppNotInstallException) {
//                                response.msg = "wx not installed"
//                                responseCallback?.callback(response)
//                            }
//                        }
//
//                    })
            return true
        }
        return false
    }

    override fun onResponse(callback: WVJBWebViewClient.WVJBResponseCallback?): Boolean {
        responseCallback = callback
        return true
    }

    inner class WxAuthResponse : BaseJsCallResponse() {
        var authCode = ""
        var msg = ""
    }
}