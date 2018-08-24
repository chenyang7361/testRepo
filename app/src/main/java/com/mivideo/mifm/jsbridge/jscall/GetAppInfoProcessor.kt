package com.mivideo.mifm.jsbridge.jscall


import com.mivideo.mifm.jsbridge.ComponentProvider
import com.mivideo.mifm.network.commonurl.NetworkParams
import me.yamlee.jsbridge.BaseJsCallProcessor
import me.yamlee.jsbridge.BaseJsCallResponse
import me.yamlee.jsbridge.JsCallData
import me.yamlee.jsbridge.WVJBWebViewClient

/**
 * 获取APP基本信息JsBridge接口
 *
 * @author KevinTu
 */
class GetAppInfoProcessor(val provider: ComponentProvider) : BaseJsCallProcessor(provider) {
    companion object {
        const val FUNC_NAME = "getAppInfo"
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
        val appInfoProcessor = AppInfoResponse()
        appInfoProcessor.ret = "ok"
        appInfoProcessor.commonParams = NetworkParams.getCommonParamsByMap(provider.provideApplicationContext())
        this.callback?.callback(appInfoProcessor)
    }

    inner class AppInfoResponse : BaseJsCallResponse() {
        var commonParams: Map<String, String> = HashMap()
    }
}