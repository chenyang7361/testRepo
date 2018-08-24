package com.mivideo.mifm.socialize.exceptions

import android.content.Context
import com.mivideo.mifm.R
import com.mivideo.mifm.socialize.share.NewShareInfo

/**
 * 发起分享，用户手动选择取消分享时Socializer的登录方法会抛出此异常，需要捕获
 * Created by yamlee on 22/05/2017.
 *
 * @author LiYan
 */
class ShareCancelException(private val context: Context,
                           private val info: NewShareInfo?,
                           private val target: String) : Throwable() {

    override val message: String?
        get() = context.getString(R.string.share_toast_cancel)

    fun getShareInfo(): NewShareInfo? {
        return info
    }
}