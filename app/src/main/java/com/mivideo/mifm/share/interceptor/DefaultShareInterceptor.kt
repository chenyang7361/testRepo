package com.mivideo.mifm.share.interceptor

import android.content.Context
import com.mivideo.mifm.R
import com.mivideo.mifm.share.ShareHelper
import com.mivideo.mifm.socialize.ShareResult
import com.mivideo.mifm.socialize.exceptions.ShareCancelException
import com.mivideo.mifm.socialize.exceptions.ShareErrorException
import com.mivideo.mifm.socialize.exceptions.SocialAppNotInstallException
import com.mivideo.mifm.socialize.share.NewShareInfo
import com.mivideo.mifm.util.app.showToast

/**
 * 默认分享结果处理逻辑
 *
 * @author LiYan
 */
class DefaultShareInterceptor(val context: Context)
    : ShareHelper.ShareResultInterceptor {
    override fun interceptSuccess(shareResult: ShareResult, shareInfo: NewShareInfo): Boolean {
        showToast(context, context.resources.getString(R.string.share_toast_success))
        if (shareResult != null) {
            val info = shareResult.shareInfo
            if (info != null && info.shareFrom == ShareHelper.SHARE_FROM_VIDEO) {
                val videoId = info.shareExtensions.getString(ShareHelper.SHARE_EXT_ARG_VIDEO_ID)
                val screen = info.shareExtensions.getString(ShareHelper.SHARE_EXT_ARG_STATISTIC_SCREEN)
//                Statistics.logShared(context, "success", screen, videoId, info.shareTo)
            }
        }
        return false
    }

    override fun interceptError(e: Throwable, shareInfo: NewShareInfo): Boolean {
        if (e is ShareCancelException) {
            showToast(context, context.resources.getString(R.string.share_toast_cancel))
            if (e.getShareInfo() != null) {
                val info = e.getShareInfo()
                if (info != null && info.shareFrom == ShareHelper.SHARE_FROM_VIDEO) {
                    val videoId = info.shareExtensions.getString(ShareHelper.SHARE_EXT_ARG_VIDEO_ID)
                    val screen = info.shareExtensions.getString(ShareHelper.SHARE_EXT_ARG_STATISTIC_SCREEN)
//                    Statistics.logShared(context, "cancel", screen, videoId, info.shareTo)
                }
            }
        } else if (e is SocialAppNotInstallException) {
            showToast(context, e.message ?: context.getString(R.string.app_not_install_yet))
        } else {
            showToast(context, context.resources.getString(R.string.share_toast_error))
            if (e is ShareErrorException) {
                if (e.errorInfo != null) {
                    val info = e.errorInfo
                    if (info.shareFrom == ShareHelper.SHARE_FROM_VIDEO) {
                        val videoId = info.shareExtensions.getString(ShareHelper.SHARE_EXT_ARG_VIDEO_ID)
                        val screen = info.shareExtensions.getString(ShareHelper.SHARE_EXT_ARG_STATISTIC_SCREEN)
//                        Statistics.logShared(context, "failed", screen, videoId, info.shareTo)
                    }
                }
            }
        }
        return false
    }
}