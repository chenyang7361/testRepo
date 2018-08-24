package com.mivideo.mifm.jsbridge.jscall

import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.account.UserAccountManager
import com.mivideo.mifm.account.UserLoginListener
import com.mivideo.mifm.jsbridge.ComponentProvider

import me.yamlee.jsbridge.BaseJsCallProcessor
import me.yamlee.jsbridge.BaseJsCallResponse
import me.yamlee.jsbridge.JsCallData
import me.yamlee.jsbridge.WVJBWebViewClient

/**
 * JsBridge登录接口支持
 *
 * Create by KevinTu on 2018/6/29
 */
class StartLoginProcessor(val provider: ComponentProvider) : BaseJsCallProcessor(provider), KodeinInjected {

    override val injector = KodeinInjector()

    init {
        inject(provider.provideApplicationContext().appKodein())
    }

    companion object {
        const val FUNC_NAME = "startLogin"
    }

    private var callback: WVJBWebViewClient.WVJBResponseCallback? = null
    private val userAccountManager: UserAccountManager by instance()

    override fun getFuncName(): String {
        return FUNC_NAME
    }

    override fun onHandleJsQuest(callData: JsCallData?): Boolean {
        if (callData?.func == FUNC_NAME) {
            if (userAccountManager.userLoggedIn()) {
                callback?.callback(LoginResult(true))
            } else {
                userAccountManager.startLogin(provider.provideActivityContext(), object : UserLoginListener() {
                    override fun loginSuccess() {
                        callback?.callback(LoginResult(true))
                        userAccountManager.clearLoginListener()
                    }
                })
            }
            return true
        }
        return false
    }

    override fun onResponse(callback: WVJBWebViewClient.WVJBResponseCallback?): Boolean {
        this.callback = callback
        return true
    }

    inner class LoginResult(var result: Boolean = false) : BaseJsCallResponse()
}
