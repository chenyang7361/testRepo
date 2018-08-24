package com.mivideo.mifm.jsbridge.jscall

import android.text.TextUtils
import com.mivideo.mifm.exception.HttpException
import com.mivideo.mifm.jsbridge.ComponentProvider
import com.mivideo.mifm.network.service.ApiClient
import com.mivideo.mifm.rx.asyncSchedulers

import me.yamlee.jsbridge.BaseJsCallProcessor
import me.yamlee.jsbridge.BaseJsCallResponse
import me.yamlee.jsbridge.JsCallData
import me.yamlee.jsbridge.WVJBWebViewClient
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import rx.Observable
import rx.Subscriber

/**
 * 网络请求
 * @author LiYan
 */
class HttpPostProcessor(val provider: ComponentProvider) : BaseJsCallProcessor(provider) {
    companion object {
        const val FUNC_NAME = "httpPost"
        const val MEDIA_TYPE_URL_ENCODE = "urlEncoded"
        const val MEDIA_TYPE_JSON = "json"
        const val MEDIA_TYPE_FORM_DATA = "formData"
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
            var postRequest = PostRequest()
            val jsonObject = JSONObject(callData.params)
            postRequest.url = jsonObject.optString("url")
            postRequest.mediaType = jsonObject.optString("mediaType")
            postRequest.jsonParam = jsonObject.optJSONObject("jsonParam")
            postRequest.sign = jsonObject.optString("sign")
            val httpClient = provider.provideHttpClient()
            var params = generatePostParam(postRequest.jsonParam.toString())
            var mediaType = getMediaType(postRequest.mediaType)
            var isSign = postRequest.sign == "true"
            val observable: Observable<JSONObject>
            if (mediaType == ApiClient.MEDIA_TYPE_JSON) {
                val requestBody = RequestBody.create(ApiClient.MEDIA_TYPE_JSON, postRequest.jsonParam.toString())
                observable = httpClient.doPostRequest(postRequest.url, requestBody, isSign)
            } else {
                observable = httpClient.doPostRequest(postRequest.url, params, mediaType, isSign)
            }
            observable
                    .compose(asyncSchedulers())
                    .subscribe(object : Subscriber<JSONObject>() {
                        override fun onNext(result: JSONObject?) {
                            val resp = PostResp()
                            resp.ret = "ok"
                            resp.data = result.toString()
                            callBacks.get(callData)?.callback(resp)
                            callBacks.remove(callData)
                        }

                        override fun onCompleted() {
                        }

                        override fun onError(e: Throwable?) {
                            val resp = PostResp()
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

    private fun generatePostParam(jsonParams: String): MutableMap<String, String> {
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

    private fun getMediaType(arg: String): MediaType {
        if (arg == MEDIA_TYPE_JSON) {
            return ApiClient.MEDIA_TYPE_JSON
        } else if (arg == MEDIA_TYPE_FORM_DATA) {
            return ApiClient.MEDIA_TYPE_FORM_DATA
        } else if (arg == MEDIA_TYPE_URL_ENCODE) {
            return ApiClient.MEDIA_TYPE_URLENCODED
        }
        return ApiClient.MEDIA_TYPE_URLENCODED
    }

    override fun onResponse(callback: WVJBWebViewClient.WVJBResponseCallback?): Boolean {
        this.callback = callback
        if (currentCall != null) {
            callBacks.put(currentCall!!, callback!!)
        }
        return true
    }

    inner class PostRequest {
        var url: String = ""

        var jsonParam: JSONObject = JSONObject()

        var mediaType: String = "urlEncoded"

        var sign = "false"
    }

    inner class PostResp : BaseJsCallResponse() {
        var data: String = ""
        var msg: String = ""
        var code: String = "200"
    }

}