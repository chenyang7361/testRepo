package com.mivideo.mifm.jsbridge

import android.webkit.WebView

/**
 * 调用H5方法代理类
 */
object WebMethodDelegate {

    val JS = "javascript:"
    /**
     * 刷新任务中心页
     */
    val GET_PAGE_INFO = "AndroidGetPageInfo"

    fun callJSMethod(webView: WebView, jsMethodStr: String, vararg arguments: Any) {
        var builder = StringBuilder(JS)
        builder.append(jsMethodStr).append("(")
        for (it in arguments) {
            builder.append(it).append(",")
        }
        if (arguments.size > 0) {
            builder.deleteCharAt(builder.length - 1)
        }
        builder.append(")")
        webView.loadUrl(builder.toString())
    }
}