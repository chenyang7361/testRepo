package com.kuaiest.video.jsbridge.jscall

import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import com.mivideo.mifm.R
import com.mivideo.mifm.jsbridge.ComponentProvider
import com.mivideo.mifm.rx.asyncSchedulers
import me.yamlee.jsbridge.BaseJsCallProcessor
import me.yamlee.jsbridge.BaseJsCallResponse
import me.yamlee.jsbridge.JsCallData
import me.yamlee.jsbridge.WVJBWebViewClient
import me.yamlee.jsbridge.utils.ToastUtil
import rx.Observable

/**
 * Created by Jiwei Yuan on 18-6-29.
 * 提现成功后js回调
 */
class Go2AppStoreProcessor(val provider: ComponentProvider) : BaseJsCallProcessor(provider) {

    private var availableMarket = ""

    companion object {
        const val FUNC_NAME = "go2AppStore"
        val stores = arrayOf("com.xiaomi.market",
                "com.huawei.appcenter",
                "com.oppo.market",
                "com.bbk.appstore",//vivo
                "com.tencent.android.qqdownloader",
                "com.baidu.appsearch",
                "com.wandoujia.phoenix2",
                "com.meizu.mstore")
    }

    init {
        Observable.from(stores)
                .filter {
                    val context = provider.provideActivityContext()
                    val intent = prepareIntent(it)
                    intent.resolveActivity(context.packageManager) != null
                }
                .compose(asyncSchedulers())
                .subscribe({
                    if (TextUtils.isEmpty(availableMarket)) {
                        availableMarket = it
                    }
                })
    }

    override fun getFuncName(): String {
        return FUNC_NAME
    }

    private var callback: WVJBWebViewClient.WVJBResponseCallback? = null

    override fun onHandleJsQuest(callData: JsCallData?): Boolean {
        if (callData?.func == FUNC_NAME) {
            go2AppStore()
            return true
        }
        return false
    }

    private fun go2AppStore() {
        val context = provider.provideActivityContext()
        if (!TextUtils.isEmpty(availableMarket)) {
            val intent = prepareIntent(availableMarket)
            try {
                context.startActivity(intent)
            } catch (e: Throwable) {
                ToastUtil.showShort(context, context.resources.getString(R.string.noAppStore))
            }
        } else {
            ToastUtil.showShort(context, context.resources.getString(R.string.noAppStore))
        }
    }

    private fun prepareIntent(packageName: String): Intent {
        val uri = Uri.parse("market://details?id=com.kuaiest.video")
        val intent = Intent()
        intent.data = uri
        intent.action = Intent.ACTION_VIEW
        intent.`package` = packageName
        if (packageName.equals("com.xiaomi.market")) {
            intent.data = Uri.parse("market://comments?id=com.kuaiest.video")
        }
        return intent
    }


    override fun onResponse(callback: WVJBWebViewClient.WVJBResponseCallback?): Boolean {
        this.callback = callback
        val response = Response()
        response.hasStore = !TextUtils.isEmpty(availableMarket)
        callback?.callback(response)
        return true
    }

    inner class Response : BaseJsCallResponse() {
        var hasStore: Boolean = false
    }
}
