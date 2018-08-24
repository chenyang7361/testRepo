package com.mivideo.mifm.jsbridge.jscall

import android.graphics.Color
import com.mivideo.mifm.util.SystemUtil
import com.mivideo.mifm.util.app.DisplayUtil

import me.yamlee.jsbridge.BaseJsCallProcessor
import me.yamlee.jsbridge.JsCallData
import me.yamlee.jsbridge.NativeComponentProvider

/**
 * 设置状态栏Js接口处理
 * @author LiYan
 */
class SetStatusBarProcessor(val provider: NativeComponentProvider) : BaseJsCallProcessor(provider) {
    companion object {
        const val FUNC_NAME = "setStatusBar"
    }

    override fun getFuncName(): String {
        return FUNC_NAME
    }

    override fun onHandleJsQuest(callData: JsCallData?): Boolean {
        if (callData?.func == FUNC_NAME) {
            val activity = provider.provideActivityContext()
            val request = convertJsonToObject(callData.params, StatusBarRequest::class.java)
            if (SystemUtil.isCanChangeStatusBarSystem()) {
                DisplayUtil.setStatusBarLightMode(activity, true)
            }
            DisplayUtil.setColor(activity, Color.parseColor(request.color))
            return true
        }
        return false
    }

    inner class StatusBarRequest {
        var color: String = ""
    }
}