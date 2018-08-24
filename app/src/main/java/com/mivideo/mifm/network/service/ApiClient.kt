package com.mivideo.mifm.network.service

import android.content.Context
import android.text.TextUtils
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.MainApp
import com.mivideo.mifm.network.service.APIParams.NETWORK_GET
import com.mivideo.mifm.network.service.APIParams.NETWORK_POST
import com.mivideo.mifm.network.service.APIParams.NETWORK_TYPE
import com.mivideo.mifm.network.service.APIParams.parserParams
import okhttp3.*
import org.json.JSONObject
import rx.Observable
import rx.Subscriber
import java.io.IOException

/**
 * dataCollection网络操作工具类
 */
@Deprecated("ApiClient不建议在新功能中使用", ReplaceWith("Retrofit"))
open class ApiClient constructor(private val mContext: Context) {

    companion object {
        val MEDIA_TYPE_JSON: MediaType = MediaType.parse("application/json; charset=utf-8")!!
        val MEDIA_TYPE_URLENCODED: MediaType = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8")!!
        val MEDIA_TYPE_FORM_DATA: MediaType = MediaType.parse("multipart/form-data; charset=utf-8")!!
    }

    private val okHttpClient: OkHttpClient = (mContext.applicationContext as MainApp).kodein.instance()

    fun doGetRequest(url: String,
                     paramsMap: MutableMap<String, String> = mutableMapOf(),
                     sign: Boolean = false): Observable<JSONObject> {
        return doRequest(url, paramsMap, null, NETWORK_GET, sign)
    }

    fun doPostRequest(url: String,
                      paramsMap: MutableMap<String, String>,
                      mediaType: MediaType,
                      sign: Boolean = false): Observable<JSONObject> {
        paramsMap.put("mediaType", mediaType.toString())
        return doRequest(url, paramsMap, null, NETWORK_POST, sign)
    }

    fun doPostRequest(url: String,
                      requestBody: RequestBody,
                      sign: Boolean = false): Observable<JSONObject> {
        val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
        return doRequest(url, null, request, NETWORK_POST, sign)
    }

    fun doPostJsonRequest(url: String, paramsStr: String, sign: Boolean = false): Observable<JSONObject> {
        val requestBody = RequestBody.create(MEDIA_TYPE_JSON, paramsStr)
        val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
        return doRequest(url, null, request, NETWORK_POST, sign)
    }

    private fun doRequest(url: String,
                          paramsMap: MutableMap<String, String>?,
                          request: Request?,
                          requestType: Int,
                          sign: Boolean = false): Observable<JSONObject> {
        return Observable.create<JSONObject> { subscriber ->
            try {
                paramsMap?.put(NETWORK_TYPE, requestType.toString())
                var tmpRequest: Request = request ?: parserParams(url, paramsMap!!)
                if (sign) {
                    tmpRequest = tmpRequest.newBuilder()
                            .addHeader("Signature", "true")
                            .build()
                }
                okHttpClient.newCall(tmpRequest)
                        .enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                subscriber.onError(e)
                            }

                            @Throws(IOException::class)
                            override fun onResponse(call: Call, response: Response) {
                                try {
                                    if (!handleResponseCode(response, subscriber)) {
                                        subscriber.onNext(JSONObject(response.body()?.string()))
                                        subscriber.onCompleted()
                                    }
                                } catch (e: Exception) {
                                    subscriber.onError(e)
                                }

                            }
                        })
            } catch (e: Exception) {
                subscriber.onError(e)
            }
        }
    }

    private fun handleResponseCode(response: Response, subscriber: Subscriber<in JSONObject>): Boolean {
        //TODO
//        if (response.code() in 500..599) {
//            if (response.code() == 555) {
//                subscriber.onError(Exception555(mContext))
//            } else {
//                subscriber.onError(Exception500())
//            }
//            return true
//        } else if (response.code() == 1001) {
//            subscriber.onError(Exception1001(mContext))
//            return true
//        } else if (response.code() == 401) {
//            subscriber.onError(Exception401(mContext))
//            return true
//        } else if (response.code() == 214) {
//            val result = JSONObject(response.body()?.string())
//            subscriber.onError(Exception214(mContext, result.toString()))
//            return true
//        } else if (response.code() != 200) {
//            subscriber.onError(ExceptionOther(response))
//            return true
//        }

        return false
    }
}

object APIParams {
    // 网络请求类型-GET请求、POST请求
    val NETWORK_GET = 2
    val NETWORK_POST = 1
    val NETWORK_TYPE = "network_type"

    private fun getPostParams(busMap: Map<String, String>?, mediaType: MediaType): RequestBody {
        if (mediaType == ApiClient.MEDIA_TYPE_URLENCODED) {
            return getPostUrlEncodeParams(busMap)
        } else {
            return getPostJsonParams(busMap, mediaType)
        }
    }

    private fun getPostJsonParams(busMap: Map<String, String>?, mediaType: MediaType): RequestBody {
        val sb = StringBuffer("{")
        if (busMap != null && busMap.isNotEmpty()) {
            val it = busMap.entries.iterator()
            while (it.hasNext()) {
                val entry = it.next()
                if ("mediaType" == entry.key) {
                    continue
                }
                if (!NETWORK_TYPE.equals(entry.key)) {
                    sb.append("\"").append(entry.key).append("\"").append(":")
                            .append("\"").append(entry.value).append("\"").append(",")
                }
            }
        }

        var result = sb.toString()
        if (sb.length > 1) {
            result = sb.substring(0, sb.length - 1)
        }
        return RequestBody.create(mediaType, StringBuffer(result).append("}").toString())
    }

    private fun getPostUrlEncodeParams(busMap: Map<String, String>?): RequestBody {
        val sb = StringBuffer()
        if (busMap != null && busMap.isNotEmpty()) {
            val it = busMap.entries.iterator()
            while (it.hasNext()) {
                val entry = it.next()
                if ("mediaType" == entry.key) {
                    continue
                }
                if (!NETWORK_TYPE.equals(entry.key)) {
                    sb.append(entry.key)
                            .append("=")
                            .append(entry.value)
                            .append("&")
                }
            }
        }

        var result = sb.toString()
        if (sb.length > 1) {
            result = sb.substring(0, sb.length - 1)
        }
        return RequestBody.create(ApiClient.MEDIA_TYPE_URLENCODED, result)
    }

    /**
     * 解析网络请求参数
     * @param url
     * @param paramsMap
     * @return
     */
    fun parserParams(url: String,
                     paramsMap: Map<String, String>?): Request {
        var request: Request

        if (paramsMap != null && NETWORK_GET.toString() == paramsMap[NETWORK_TYPE]) {
            val httpUrl = HttpUrl.parse(url)
                    ?: throw IllegalArgumentException("unexpected url: $url")
            val urlBuilder = httpUrl.newBuilder()
            val it = paramsMap.entries.iterator()
            while (it.hasNext()) {
                val entry = it.next()
                if (NETWORK_TYPE != entry.key) {
                    urlBuilder.addQueryParameter(entry.key, entry.value)
                }
            }
            request = Request.Builder()
                    .get()
                    .url(urlBuilder.build())
                    .build()
        } else {
            val mediaTypeStr = paramsMap?.get("mediaType")
            var mediaType: MediaType = ApiClient.MEDIA_TYPE_JSON
            if (!TextUtils.isEmpty(mediaTypeStr)) {
                mediaType = MediaType.parse(mediaTypeStr)!!
            }
            request = Request.Builder()
                    .post(getPostParams(paramsMap, mediaType))
                    .url(url)
                    .build()

        }
        return request
    }
}

