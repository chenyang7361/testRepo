package com.mivideo.mifm.network.interceptor

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response


class ErrorHandleInterceptor(val context: Context) : Interceptor {
//    private val userAccountManager: UserAccountManager = (context.applicationContext as MainApp).kodein.instance()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        when {
//        //系统维护提示返回码
//            response.code() == 555 -> showToast(context, context.getString(R.string.server_maintained))
//        //用户token过期或者用户信息异常返回码
//            response.code() == 401 ->
//                context.runOnUiThread {
//                    userAccountManager.logout()
//                    showToast(context, getString(R.string.user_info_error_hint))
//                }
//        //系统繁忙返回码
//            response.code() == 1001 -> context.runOnUiThread {
//                showToast(context, getString(R.string.system_busy))
//            }
        }
        return response
    }
}
