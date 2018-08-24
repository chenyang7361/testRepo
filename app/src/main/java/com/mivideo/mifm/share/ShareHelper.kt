package com.mivideo.mifm.share

import com.mivideo.mifm.share.interceptor.DefaultShareInterceptor
import com.mivideo.mifm.MainConfig
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.jsondata.MediaDetailData
import com.mivideo.mifm.data.models.jsondata.common.CommonAuthor
import com.mivideo.mifm.data.models.jsondata.common.CommonShare
import com.mivideo.mifm.data.models.jsondata.common.CommonVideo
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.share.interceptor.ListenerInterceptor
import com.mivideo.mifm.socialize.ShareResult
import com.mivideo.mifm.socialize.SocializeManager
import com.mivideo.mifm.socialize.share.NewShareInfo
import com.mivideo.mifm.util.ApkUtil
import com.mivideo.mifm.util.app.copyToClipboard
import com.mivideo.mifm.util.app.showToast
import rx.Observable
import rx.Subscriber
import rx.Subscription
import java.io.File

/**
 * 分享帮助类，将分享代码统一管理
 * @author LiYan
 */
class ShareHelper(val socialManager: SocializeManager, val from: String) {

    companion object {
        /**
         * 分享视频
         */
        const val SHARE_FROM_VIDEO = "share-video"
        /**
         * 分享作者
         */
        const val SHARE_FROM_AUTHOR = "share-author"
        /**
         * 从H5中发起的分享
         */
        const val SHARE_FROM_H5 = "share-h5"
        /**
         * shareInfo扩展参数视频ID
         */
        const val SHARE_EXT_ARG_VIDEO_ID = "videoId"

        /**
         * shareInfo扩展参数视频Info
         */
        const val SHARE_EXT_ARG_VIDEO = "videoInfo"

        /**
         * shareInfo扩展参数作者Info
         */
        const val SHARE_EXT_ARG_AUTHOR = "authorInfo"

        /**
         * shareInfo扩展参数，用于统计分享时当前屏幕状态
         */
        const val SHARE_EXT_ARG_STATISTIC_SCREEN = "screen"

        fun buildShareInfo(videoInfo: CommonVideo): NewShareInfo {
            val shareInfo = NewShareInfo()
            shareInfo.shareImageUrl = videoInfo.video_image
            shareInfo.title = videoInfo.video_title
            if (videoInfo.share_info != null) {
                shareInfo.targetUrl = videoInfo.share_info.share_url
                shareInfo.shareSupport = videoInfo.share_info.status_code == MainConfig.VIDEO_CAN_SHARE
            }
            shareInfo.shareExtensions.putString(SHARE_EXT_ARG_VIDEO_ID, videoInfo.video_id)
            shareInfo.shareExtensions.putParcelable(SHARE_EXT_ARG_VIDEO, videoInfo)
            return shareInfo
        }

        fun buildShareInfo(author: CommonAuthor, share: CommonShare): NewShareInfo {
            val shareInfo = NewShareInfo()
            shareInfo.shareImageUrl = author.poster_url
            shareInfo.title = "看看" + author.name + "分享的视频"
            shareInfo.targetUrl = share.share_url
            shareInfo.shareSupport = share.status_code == MainConfig.VIDEO_CAN_SHARE
            shareInfo.shareExtensions.putString(SHARE_EXT_ARG_VIDEO_ID, author.videoId)
            shareInfo.shareExtensions.putParcelable(SHARE_EXT_ARG_AUTHOR, author)
            return shareInfo
        }

        fun buildShareInfo(info: MediaDetailData): NewShareInfo {
            val shareInfo = NewShareInfo()
            shareInfo.shareImageUrl = info.cover
            shareInfo.title = info.title
//            if (videoInfo.share_info != null) {
//                shareInfo.targetUrl = videoInfo.share_info.share_url
//                shareInfo.shareSupport = videoInfo.share_info.status_code == MainConfig.VIDEO_CAN_SHARE
//            }
            shareInfo.shareExtensions.putString(SHARE_EXT_ARG_VIDEO_ID, info.id)
            shareInfo.shareExtensions.putParcelable(SHARE_EXT_ARG_VIDEO, info)
            return shareInfo
        }

        /**
         * NewShareInfo for 分享文件到微信好友
         */
        fun buildShareInfo(filePath: String): NewShareInfo? {
            if (!File(filePath).exists()) {
                return null
            }
            val shareInfo = NewShareInfo()
            shareInfo.title = filePath.substring(filePath.lastIndexOf("/") + 1)
            shareInfo.shareFilePath = filePath
            shareInfo.shareTo = NewShareInfo.WX
            return shareInfo
        }

        /**
         * 从ShareInfo中获取视频信息附加参数
         */
        fun getVideoFromShareInfo(shareInfo: NewShareInfo): CommonVideo? {
            val video: CommonVideo? = shareInfo.shareExtensions.getParcelable(SHARE_EXT_ARG_VIDEO)
            return video
        }

        /**
         * 从AuthorInfo中获取作者信息附加参数
         */
        fun getAuthorFromShareInfo(shareInfo: NewShareInfo): CommonAuthor? {
            val author: CommonAuthor? = shareInfo.shareExtensions.getParcelable(SHARE_EXT_ARG_AUTHOR)
            return author
        }

    }

    private val context = socialManager.activity.applicationContext

    private var videoInfo: CommonVideo? = null
    private var authorInfo: CommonAuthor? = null
    private var shareListener: ShareHelperListener? = null

    private var mShareListenerInterceptor: ListenerInterceptor? = null
    private val shareInterceptors = ArrayList<ShareResultInterceptor>()

    init {
        addInterceptor(ListenerInterceptor(shareListener))
        addInterceptor(DefaultShareInterceptor(context))
    }

    /**
     * 添加分享过滤器
     */
    fun addInterceptor(interceptor: ShareResultInterceptor) {
        shareInterceptors.add(interceptor)
    }

    /**
     * 移除分享过滤器
     */
    fun removeInterceptor(interceptor: ShareResultInterceptor) {
        shareInterceptors.remove(interceptor)
    }


    /**
     * 添加分享监听
     */
    fun addShareListener(shareListener: ShareHelperListener) {
        if (mShareListenerInterceptor != null) {
            removeInterceptor(mShareListenerInterceptor!!)
        }
        mShareListenerInterceptor = ListenerInterceptor(shareListener)
        addInterceptor(mShareListenerInterceptor!!)
    }

    /**
     * 默认分享回调处理，只是简单的toast提示，如果有特殊
     * 需求需要在调用处自己单独处理observable
     */
    private fun handleShareDefault(observable: Observable<ShareResult>,
                                   shareInfo: NewShareInfo): Subscription {
        return observable.compose(asyncSchedulers())
                .subscribe(ShareResultSubscriber(shareInfo))
    }

    private inner class ShareResultSubscriber(val shareInfo: NewShareInfo) : Subscriber<ShareResult>() {
        override fun onNext(t: ShareResult) {
            shareInterceptors.forEach {
                val intercepted = it.interceptSuccess(t, shareInfo)
                if (intercepted) return@forEach
            }
        }

        override fun onCompleted() {
        }

        override fun onError(e: Throwable) {
            shareInterceptors.forEach {
                val intercepted = it.interceptError(e, shareInfo)
                if (intercepted) return@forEach
            }
        }

    }

    /**
     * 复制链接
     */
    fun copyUrl(shareInfo: NewShareInfo, listener: ShareHelperListener? = null) {
        this.shareListener = listener
        listener?.onShareStart()
        this.videoInfo = getVideoFromShareInfo(shareInfo)
        this.authorInfo = getAuthorFromShareInfo(shareInfo)
        if (shareInfo.shareContentType == NewShareInfo.SHARE_CONTENT_TYPE_TEXT) {
            copyToClipboard(shareInfo.textContent, context)
        } else {
            copyToClipboard(shareInfo.targetUrl, context)
        }
        showToast(context, context.resources.getString(R.string.share_copy_success))
        listener?.onShareSuccess()
    }

    /**
     * QQ分享
     */
    fun shareQQ(shareInfo: NewShareInfo, listener: ShareHelperListener? = null) {
        this.shareListener = listener
        listener?.onShareStart()
        this.videoInfo = getVideoFromShareInfo(shareInfo)
        this.authorInfo = getAuthorFromShareInfo(shareInfo)
        logShareClick(NewShareInfo.QQ)
        if (ApkUtil.isInstalled(context, ApkUtil.QQ_PACKAGE)
                || ApkUtil.isInstalled(context, ApkUtil.TIM_PACKAGE)) {
            if (shareInfo.shareSupport) {
                shareInfo.shareTo = NewShareInfo.QQ
                shareInfo.shareExtensions.putString(SHARE_EXT_ARG_STATISTIC_SCREEN, from)
                val observable = socialManager.qq().share(shareInfo)
                handleShareDefault(observable, shareInfo)
                listener?.onShareStart()
            } else {
                showUnShareToast()
                logShareUnsupport(NewShareInfo.QQ)
            }
        } else {
            showToast(context, context.resources.getString(R.string.need_install_qq))
            logShareUnInstall(NewShareInfo.QQ)
        }
    }

    /**
     * QQ空间分享
     */
    fun shareQQZone(shareInfo: NewShareInfo, listener: ShareHelperListener? = null) {
        this.shareListener = listener
        listener?.onShareStart()
        this.videoInfo = getVideoFromShareInfo(shareInfo)
        this.authorInfo = getAuthorFromShareInfo(shareInfo)
        logShareClick(NewShareInfo.QQ_ZONE)
        if (ApkUtil.isInstalled(context, ApkUtil.QQ_PACKAGE)
                || ApkUtil.isInstalled(context, ApkUtil.TIM_PACKAGE)) {
            if (shareInfo.shareSupport) {
                shareInfo.shareTo = NewShareInfo.QQ_ZONE
                shareInfo.shareExtensions.putString(SHARE_EXT_ARG_STATISTIC_SCREEN, from)
                val observable = socialManager.qq().share(shareInfo)
                handleShareDefault(observable, shareInfo)
                listener?.onShareStart()
            } else {
                showUnShareToast()
                logShareUnsupport(NewShareInfo.QQ_ZONE)
            }
        } else {
            showToast(context, context.resources.getString(R.string.need_install_qq))
            logShareUnInstall(NewShareInfo.QQ_ZONE)
        }
    }

    /**
     * 微信朋友圈分享
     */
    fun shareWxMoments(shareInfo: NewShareInfo, listener: ShareHelperListener? = null) {
        this.shareListener = listener
        listener?.onShareStart()
        this.videoInfo = getVideoFromShareInfo(shareInfo)
        this.authorInfo = getAuthorFromShareInfo(shareInfo)
        logShareClick(NewShareInfo.WX_MOMENTS)
        if (ApkUtil.isInstalled(context, ApkUtil.WX_PACKAGE)) {
            if (shareInfo.shareSupport) {
                shareInfo.shareTo = NewShareInfo.WX_MOMENTS
                shareInfo.shareExtensions.putString(SHARE_EXT_ARG_STATISTIC_SCREEN, from)
                val observable = socialManager.wx().share(shareInfo)
                handleShareDefault(observable, shareInfo)
                listener?.onShareStart()
            } else {
                showUnShareToast()
                logShareUnsupport(NewShareInfo.WX_MOMENTS)
            }
        } else {
            showToast(context, context.resources.getString(R.string.need_install_wx))
            logShareUnInstall(NewShareInfo.WX_MOMENTS)
        }
    }

    /**
     * 微信分享
     */
    fun shareWx(shareInfo: NewShareInfo, listener: ShareHelperListener? = null) {
        this.shareListener = listener
        listener?.onShareStart()
        this.videoInfo = getVideoFromShareInfo(shareInfo)
        this.authorInfo = getAuthorFromShareInfo(shareInfo)
        logShareClick(NewShareInfo.WX)
        if (ApkUtil.isInstalled(context, ApkUtil.WX_PACKAGE)) {
            if (shareInfo.shareSupport) {
                shareInfo.shareTo = NewShareInfo.WX
                shareInfo.shareExtensions.putString(SHARE_EXT_ARG_STATISTIC_SCREEN, from)
                val observable = socialManager.wx().share(shareInfo)
                handleShareDefault(observable, shareInfo)
                listener?.onShareStart()
            } else {
                showUnShareToast()
                logShareUnsupport(NewShareInfo.WX)
            }
        } else {
            showToast(context, context.resources.getString(R.string.need_install_wx))
            logShareUnInstall(NewShareInfo.WX)
        }
    }

    /**
     *微博分享
     */
    fun shareWeiBo(shareInfo: NewShareInfo, listener: ShareHelperListener? = null) {
        this.shareListener = listener
        listener?.onShareStart()
        this.videoInfo = getVideoFromShareInfo(shareInfo)
        this.authorInfo = getAuthorFromShareInfo(shareInfo)
        logShareClick(NewShareInfo.WEIBO)
        if (ApkUtil.isInstalled(context, ApkUtil.WB_PACKAGE)) {
            if (shareInfo.shareSupport) {
                shareInfo.shareTo = NewShareInfo.WEIBO
                shareInfo.shareExtensions.putString(SHARE_EXT_ARG_STATISTIC_SCREEN, from)
                val observable = socialManager.weibo().share(shareInfo)
                handleShareDefault(observable, shareInfo)
                listener?.onShareStart()
            } else {
                showUnShareToast()
                logShareUnsupport(NewShareInfo.WEIBO)
            }
        } else {
            showToast(context, context.resources.getString(R.string.need_install_wb))
            logShareUnInstall(NewShareInfo.WEIBO)
        }
    }

    private fun showUnShareToast() {
        if (videoInfo != null) {
            showToast(context, context.resources.getString(R.string.sorry_no_able_share))
        } else if (authorInfo != null) {
            showToast(context, context.resources.getString(R.string.sorry_no_able_share_author))
        }
    }

    private fun logShareClick(type: String) {
        if (videoInfo != null) {
//            Statistics.logShared(context, "click", from, videoInfo!!.video_id, type)
        }
        if (authorInfo != null) {
//            Statistics.logSharedAuthor(context, "click", from, authorInfo!!.id, type)
        }
    }

    private fun logShareUnsupport(type: String) {
        if (videoInfo != null) {
//            Statistics.logShared(context, "unsupport", from, videoInfo!!.video_id, type)
        }
        if (authorInfo != null) {
//            Statistics.logSharedAuthor(context, "unsupport", from, authorInfo!!.id, type)
        }
    }

    private fun logShareUnInstall(type: String) {
        if (videoInfo != null) {
//            Statistics.logShared(context, "uninstall", from, videoInfo!!.video_id, type)
        }
        if (authorInfo != null) {
//            Statistics.logSharedAuthor(context, "uninstall", from, authorInfo!!.id, type)
        }
    }

    fun release() {
        socialManager.release()
    }


    abstract class ShareHelperListener {
        open fun onShareStart(): Boolean = false

        open fun onShareError(e: Throwable): Boolean = false

        open fun onShareCancel(): Boolean = false

        open fun onShareSuccess(): Boolean = false
    }

    interface ShareResultInterceptor {
        fun interceptSuccess(shareResult: ShareResult, shareInfo: NewShareInfo): Boolean

        fun interceptError(e: Throwable, shareInfo: NewShareInfo): Boolean
    }

}