package com.mivideo.mifm.jsbridge

import com.mivideo.mifm.network.service.ApiClient
import com.mivideo.mifm.share.ShareHelper
import com.mivideo.mifm.socialize.SocializeManager
import me.yamlee.jsbridge.NativeComponentProvider

/**
 * 原生组件提供者
 * @author LiYan
 */
interface ComponentProvider : NativeComponentProvider {
    fun provideSocialManager(): SocializeManager

    fun provideHttpClient(): ApiClient

    /**
     * 返回分享帮助类
     */
    fun provideShareHelper(): ShareHelper

}