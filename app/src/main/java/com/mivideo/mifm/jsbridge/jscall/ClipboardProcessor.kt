package com.mivideo.mifm.jsbridge.jscall

import android.content.Context
import me.yamlee.jsbridge.BaseJsCallProcessor
import me.yamlee.jsbridge.BaseJsCallResponse
import me.yamlee.jsbridge.JsCallData
import me.yamlee.jsbridge.WVJBWebViewClient
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.os.Build.VERSION_CODES.O
import android.text.TextUtils
import com.mivideo.mifm.jsbridge.ComponentProvider


/**
 * 剪切板复制JsBridge接口
 * @author LiYan
 */
class ClipboardProcessor(val provider: ComponentProvider) : BaseJsCallProcessor(provider) {
    companion object {
        const val FUNC_NAME = "clipboard"
    }

    private var callback: WVJBWebViewClient.WVJBResponseCallback? = null

    override fun getFuncName(): String {
        return FUNC_NAME
    }

    override fun onHandleJsQuest(callData: JsCallData?): Boolean {
        if (callData?.func == FUNC_NAME) {
            val request = convertJsonToObject(callData.params, ClipboardRequest()::class.java)
            val clipboardResponse = ClipboardResponse()
            if (!TextUtils.isEmpty(request.content)) {
                val context = provider.provideApplicationContext()
                if (Build.VERSION.SDK_INT >= O) {
                    val clipManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("text", request.content)
                    clipManager.primaryClip = clip
                } else {
                    val clipManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.text.ClipboardManager
                    clipManager.text = request.content
                }
                clipboardResponse.value = 1
            } else {
                clipboardResponse.value = 0
            }
            callback?.callback(clipboardResponse)
            return true
        }
        return false
    }

    override fun onResponse(callback: WVJBWebViewClient.WVJBResponseCallback?): Boolean {
        this.callback = callback
        return true
    }

    inner class ClipboardRequest {
        var content: String = ""
    }

    inner class ClipboardResponse : BaseJsCallResponse() {
        var value = 0
    }
}