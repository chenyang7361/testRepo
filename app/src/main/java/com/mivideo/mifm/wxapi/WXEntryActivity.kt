package com.mivideo.mifm.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.hwangjr.rxbus.RxBus
import com.mivideo.mifm.socialize.internel.WxSocializer
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler

/**
 * 微信分享回调监听类
 */
class WXEntryActivity : Activity(), IWXAPIEventHandler {

    public override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        WxSocializer.api?.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        WxSocializer.api?.handleIntent(intent, this)
    }

    override fun onReq(baseReq: BaseReq) {
        RxBus.get().post(WxSocializer.WxEntryOnReqEvent(baseReq))
    }

    override fun onResp(resp: BaseResp) {
        RxBus.get().post(WxSocializer.WxEntryOnRespEvent(resp))
        finish()
    }
}