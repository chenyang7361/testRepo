package com.kuaiest.video.jsbridge.jscall

import android.text.TextUtils
import com.mivideo.mifm.exception.HttpException
import com.mivideo.mifm.jsbridge.ComponentProvider
import com.mivideo.mifm.rx.asyncSchedulers
import me.yamlee.jsbridge.BaseJsCallProcessor
import me.yamlee.jsbridge.BaseJsCallResponse
import me.yamlee.jsbridge.JsCallData
import me.yamlee.jsbridge.WVJBWebViewClient
import org.json.JSONException
import org.json.JSONObject
import rx.Subscriber


/**
 * 网络请求
 * @author LiYan
 */
class HttpGetProcessor(val provider: ComponentProvider) : BaseJsCallProcessor(provider) {
    companion object {
        const val FUNC_NAME = "httpGet"
    }

    private var callback: WVJBWebViewClient.WVJBResponseCallback? = null
    private var callBacks: HashMap<JsCallData, WVJBWebViewClient.WVJBResponseCallback> =
            HashMap()
    private var currentCall: JsCallData? = null

    override fun getFuncName(): String {
        return FUNC_NAME
    }

    override fun onHandleJsQuest(callData: JsCallData?): Boolean {
        if (callData?.func == FUNC_NAME) {
            currentCall = callData
            val getRequest = GetRequest()
            val jsonObject = JSONObject(callData.params)
            getRequest.url = jsonObject.optString("url")
            getRequest.jsonParam = jsonObject.optJSONObject("jsonParam")
            getRequest.sign = jsonObject.optString("sign")
            val httpClient = provider.provideHttpClient()
            var params = generateGetParams(getRequest.jsonParam.toString())
            var isSign = getRequest.sign == "true"
            httpClient.doGetRequest(getRequest.url, params, isSign)
                    .compose(asyncSchedulers())
                    .subscribe(object : Subscriber<JSONObject>() {
                        override fun onNext(result: JSONObject?) {
                            val resp = GetResp()
                            resp.ret = "ok"
                            resp.data = result.toString()
                            callBacks.get(callData)?.callback(resp)
                            callBacks.remove(callData)
                        }

                        override fun onCompleted() {
                        }

                        override fun onError(e: Throwable?) {
                            val resp = GetResp()
                            resp.ret = "fail"
                            resp.msg = e?.message ?: ""
                            if (e is HttpException) {
                                resp.code = e.getCode().toString()
                            } else {
                                resp.code = "-1"
                            }
                            callBacks.get(callData)?.callback(resp)
                            callBacks.remove(callData)
                        }

                    })
            return true
        }
        return false
    }


    private fun generateGetParams(jsonParams: String): MutableMap<String, String> {
        if (TextUtils.isEmpty(jsonParams)) {
            return mutableMapOf()
        }
        val params = mutableMapOf<String, String>()
        var paramJsonObj: JSONObject? = null
        try {
            paramJsonObj = JSONObject(jsonParams)
            val keys = paramJsonObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = paramJsonObj.optString(key)
                params.put(key, value)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return params
    }


    override fun onResponse(callback: WVJBWebViewClient.WVJBResponseCallback?): Boolean {
        this.callback = callback
        if (currentCall != null) {
            callBacks.put(currentCall!!, callback!!)
        }
        return true
    }

    inner class GetRequest {
        var url: String = ""
        var jsonParam: JSONObject = JSONObject()
        var sign = "false"
    }

    inner class GetResp : BaseJsCallResponse() {
        var data: String = ""
        var msg: String = ""
        var code: String = "200"
    }

}