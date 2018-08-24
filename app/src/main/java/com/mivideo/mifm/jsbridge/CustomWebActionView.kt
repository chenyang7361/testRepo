package com.mivideo.mifm.jsbridge


import com.mivideo.mifm.share.ShareHelper
import com.mivideo.mifm.socialize.share.NewShareInfo
import me.yamlee.jsbridge.ui.WebActionView

/**
 * 自定义web view接口
 * @author LiYan
 */
interface CustomWebActionView : WebActionView {
    /**
     * 分享弹窗
     */
    fun showShareDialog(shareInfo: NewShareInfo, listener: ShareHelper.ShareHelperListener? = null)

    /**
     * 无网络连接提示
     */
    fun showNetUnconnected()

    /**
     * 加载失败提示
     */
    fun showLoadFail()

    /**
     * 隐藏信息提示页
     */
    fun hideInfoView()
}