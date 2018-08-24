package com.mivideo.mifm.exception

import android.content.Context
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.R
import com.mivideo.mifm.account.UserAccountManager
import com.mivideo.mifm.util.app.showToast
import org.jetbrains.anko.runOnUiThread

/**
 * Created by aaron on 2017/2/21.
 */
class Exception401 : HttpException, KodeinInjected {
    override val injector = KodeinInjector()

    val userAccountManager: UserAccountManager by instance()

    constructor(mContext: Context) : super("401") {
        mContext.runOnUiThread {
            inject(mContext.appKodein())
            userAccountManager.logout()
            showToast(mContext,getString(R.string.user_info_error_hint))
//            userAccountManager.showLoginDialog()
        }
    }

    override fun getCode(): Int {
        return 401
    }

}