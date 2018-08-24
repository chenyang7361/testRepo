package com.mivideo.mifm.network.interceptor

import android.content.Context
import com.mivideo.mifm.network.commonurl.NetworkParams
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class CommonParamsAddInterceptor(val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        Timber.i("header:" + request.header("Signature"))
        val needSign = request.header("Signature") == "true"
        val commonParams = NetworkParams.getCommonParamsByMap(context, needSign)
        val newUrlBuilder = request.url().newBuilder()
        commonParams.forEach { (key, value) ->
            newUrlBuilder.addQueryParameter(key, value)
        }
        val newUrl = newUrlBuilder.build()
        val newRequest = request.newBuilder()
                .url(newUrl)
                .build()
        return chain.proceed(newRequest)
    }
}
