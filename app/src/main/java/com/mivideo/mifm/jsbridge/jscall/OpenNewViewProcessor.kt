package com.mivideo.mifm.jsbridge.jscall

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v4.app.Fragment

import com.mivideo.mifm.WebActivity
import com.mivideo.mifm.jsbridge.CustomInteraction
import me.yamlee.jsbridge.*
import org.json.JSONObject

/**
 * 传入一个URL,起一个新的Activity来加载这个URL
 * @author LiYan
 */
class OpenNewViewProcessor(provider: NativeComponentProvider, var fragment: Fragment? = null) : BaseJsCallProcessor(provider) {
    companion object {
        const val FUNC_NAME = "openNewView"
        const val REQUEST_CODE_DEFAULT = 1001
    }

    private var callBacks: HashMap<Int, WVJBWebViewClient.WVJBResponseCallback> = HashMap()
    private var currentRequest: OpenNewViewRequest? = null

    override fun getFuncName(): String {
        return FUNC_NAME
    }

    override fun onHandleJsQuest(callData: JsCallData?): Boolean {
        if (callData?.func == FUNC_NAME) {
            val request = convertJsonToObject(callData.params, OpenNewViewRequest::class.java)
            currentRequest = request
            val interaction = componentProvider.provideWebInteraction() as CustomInteraction
            if (request.url.startsWith("http")) {
                val sendContext = componentProvider.provideActivityContext()
                val intent = WebActivity.getUrlJumpIntent(request.url, sendContext,
                        request.newViewTheme ?: WebActivity.VIEW_THREME_NORMAL)
                if (fragment == null) {
                    interaction.startActivityForResult(intent, request.requestCode)
                } else {
                    fragment!!.startActivityForResult(intent, request.requestCode)
                }
            } else {
                val intent = Intent()
                intent.data = Uri.parse(request.url)
                interaction.startActivity(intent)
            }
            return true
        }
        return false
    }

    override fun onResponse(callback: WVJBWebViewClient.WVJBResponseCallback?): Boolean {
        if (callback != null && currentRequest != null) {
            callBacks.put(currentRequest!!.requestCode, callback)
            return true
        } else {
            return false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        val callback = callBacks.get(requestCode)
        if (callback != null) {
            val response = OpenNewViewResponse()
            response.requestCode = requestCode
            if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_CANCELED) {
                response.ret = "ok"
            } else {
                response.ret = "fail"
            }
            callback.callback(response)
            return true
        } else {
            return super.onActivityResult(requestCode, resultCode, data)
        }
    }

    inner class OpenNewViewRequest {
        var url: String = ""
        var newViewTheme: String? = "normal"
        var requestCode: Int = REQUEST_CODE_DEFAULT
    }

    inner class OpenNewViewResponse : BaseJsCallResponse() {
        var requestCode: Int = REQUEST_CODE_DEFAULT
        var resultData: JSONObject = JSONObject()
    }
}