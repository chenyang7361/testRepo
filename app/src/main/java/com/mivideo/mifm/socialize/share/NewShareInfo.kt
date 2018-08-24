package com.mivideo.mifm.socialize.share

import android.os.Bundle
import android.support.annotation.StringDef


/**
 * 通用分享信息数据类
 * @author LiYan
 */
class NewShareInfo {
    @Retention(AnnotationRetention.SOURCE)
    @StringDef(QQ, QQ_ZONE, WX, WX_MOMENTS, WEIBO, DEFAULT)
    annotation class ShareType

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(SHARE_CONTENT_TYPE_LINK, SHARE_CONTENT_TYPE_TEXT,
            SHARE_CONTENT_TYPE_IMAGE, SHARE_CONTENT_TYPE_FILE)
    annotation class ShareContentType

    companion object {
        /**
         * QQ分享
         */
        const val QQ = "qq"
        /**
         * QQ空间分享
         */
        const val QQ_ZONE = "qq_zone"
        /**
         * 微信分享
         */
        const val WX = "wx"
        /**
         * 微信朋友圈
         */
        const val WX_MOMENTS = "wx_moments"
        /**
         * 微博分享
         */
        const val WEIBO = "weibo"
        /**
         * 未知类型
         */
        const val DEFAULT = "default"

        /**
         * 分享内容类型：网页链接
         */
        const val SHARE_CONTENT_TYPE_LINK = "link"

        /**
         * 分享内容类型：文字
         */
        const val SHARE_CONTENT_TYPE_TEXT = "text"

        /**
         * 分享内容类型：图片
         */
        const val SHARE_CONTENT_TYPE_IMAGE = "image"

        /**
         * 分享内容类型：文件
         */
        const val SHARE_CONTENT_TYPE_FILE = "file"
    }


    /**
     * 当前信息是否支持分享
     * （不支持分享的信息，shareSupport为false）
     */
    var shareSupport: Boolean = true
    /**
     * 分享点击跳转的URL
     */
    var targetUrl: String = ""
    /**
     * 分享的标题
     */
    var title: String = ""
    /**
     * 分享主题描述信息
     */
    var description: String = "快人一步，畅享精彩！"
    /**
     * 分享信息展示的图片URL
     * （微信等sdk对分享图片大小有限制，需要注意，微信超过32K就会报错）
     */
    var shareImageUrl: String = ""

    /**
     * 分享文字内容
     */
    var textContent: String = ""

    /**
     * 分享到哪里
     */
    @ShareType
    var shareTo: String = DEFAULT

    /**
     * 从哪里发起的分享
     */
    var shareFrom: String = "default"

    /**
     * 分享信息的扩展参数
     */
    var shareExtensions: Bundle = Bundle()

    /**
     * 分享文件路径
     */
    var shareFilePath: String = ""

    /**
     * 分享文件类型
     */
    @ShareContentType
    var shareContentType: String = SHARE_CONTENT_TYPE_LINK
}