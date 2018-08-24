package com.mivideo.mifm.socialize.exceptions

import android.content.Context
import com.mivideo.mifm.R

/**
 * 第三方正在登录，用户手动选择取消登录时Socializer的登录方法会抛出此异常，需要捕获
 * Created by yamlee on 22/05/2017.
 *
 * @author LiYan
 */
class AuthCancelException(private val context: Context) : Throwable() {

    override val message: String?
        get() = context.getString(R.string.login_cancel)
}