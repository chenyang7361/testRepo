package com.kuaiest.video.jsbridge.jscall

import android.text.TextUtils
import com.mivideo.mifm.account.UserAccountManager
import com.mivideo.mifm.jsbridge.ComponentProvider

import me.yamlee.jsbridge.BaseJsCallProcessor
import me.yamlee.jsbridge.BaseJsCallResponse
import me.yamlee.jsbridge.JsCallData
import me.yamlee.jsbridge.WVJBWebViewClient

/**
 * 获取用户信息JsBridge接口
 * @author LiYan
 */
class GetUserProcessor(val provider: ComponentProvider) : BaseJsCallProcessor(provider) {
    companion object {
        const val FUNC_NAME = "getUser"
    }

    private var callback: WVJBWebViewClient.WVJBResponseCallback? = null
    private var currentCallData: JsCallData? = null

    override fun getFuncName(): String {
        return FUNC_NAME
    }

    override fun onHandleJsQuest(callData: JsCallData?): Boolean {
        currentCallData = callData
        return callData?.func == FUNC_NAME
    }

    override fun onResponse(callback: WVJBWebViewClient.WVJBResponseCallback?): Boolean {
        this.callback = callback
        realHandleJsQuest(currentCallData)
        return true
    }

    private fun realHandleJsQuest(callData: JsCallData?) {
        val user = UserAccountManager(provider.provideApplicationContext()).user()
        val userResponse = UserResponse()
        userResponse.ret = "ok"
        userResponse.userName = user?.getNickName() ?: ""
        userResponse.avatarUrl = user?.getAvatarUrl() ?: ""
        userResponse.isLogin = !TextUtils.isEmpty(user?.getAccessToken() ?: "")
        this.callback?.callback(userResponse)
    }

    inner class UserResponse : BaseJsCallResponse() {
        var userName: String = ""
        var avatarUrl: String = ""
        var isLogin: Boolean = false
    }
}