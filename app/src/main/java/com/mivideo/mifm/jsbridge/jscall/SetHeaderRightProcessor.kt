package com.mivideo.mifm.jsbridge.jscall

import com.mivideo.mifm.WebActivity
import com.mivideo.mifm.jsbridge.ComponentProvider
import me.yamlee.jsbridge.jscall.AbstractSetHeaderRightProcessor

/**
 * 标题栏右边按钮设置
 * @author LiYan
 */
class SetHeaderRightProcessor(val provider: ComponentProvider) : AbstractSetHeaderRightProcessor(provider) {
    override fun onClickRightBtn(clickJumpUrl: String) {
        val intent = WebActivity.getUrlJumpIntent(clickJumpUrl, provider.provideActivityContext())
        provider.provideWebInteraction().startActivity(intent)
    }
}