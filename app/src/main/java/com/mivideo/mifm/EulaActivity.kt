package com.mivideo.mifm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.mivideo.mifm.ui.fragment.EulaFragment
import com.mivideo.mifm.util.SystemUtil.Companion.isCanChangeStatusBarSystem
import com.mivideo.mifm.util.app.DisplayUtil
import timber.log.Timber

class EulaActivity : BaseActivity() {
    companion object {

        private const val ARG_URL = "url"
        private const val ARG_THEME = "theme"

        const val VIEW_THEME_TRANSPARENT = "transparent"
        const val VIEW_THREME_NORMAL = "normal"

        /**
         *获得跳转用户协议界面的Intent
         */
        fun getAgreementIntent(sendContext: Context): Intent {
            val intent = Intent(sendContext, EulaActivity::class.java)
            intent.putExtra(EulaFragment.ARG_TYPE, EulaFragment.USER_AGREEMENT)
            return intent
        }

        /**
         *获得跳转隐私协议界面的Intent
         */
        fun getPrivacyClasueIntent(sendContext: Context): Intent {
            val intent = Intent(sendContext, EulaActivity::class.java)
            intent.putExtra(EulaFragment.ARG_TYPE, EulaFragment.PRIVACY_CLAUSE)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isCanChangeStatusBarSystem()) {
            // 默认MIUI系统StatusBar字的颜色为黑色
            DisplayUtil.setStatusBarLightMode(this, true)
        }

        val routerUrl = intent?.data?.getQueryParameter("url")
        Timber.i("urlArgument is: $routerUrl")
        val param = intent.getStringExtra(EulaFragment.ARG_TYPE)
        val urlArg = intent?.getStringExtra(ARG_URL)
        val theme = intent?.getStringExtra(ARG_THEME)

        //微博分享回调会莫名偶尔拉起一个新的activity，如果非正常参数传进来创建activity，不予拉起
        if (TextUtils.isEmpty(routerUrl) && TextUtils.isEmpty(param) &&
                TextUtils.isEmpty(urlArg)) {
            finish()
        }
        if (param == EulaFragment.USER_AGREEMENT) {
            loadRootFragment(android.R.id.content, EulaFragment.userAgreementFragment())
        } else if (param == EulaFragment.PRIVACY_CLAUSE) {
            loadRootFragment(android.R.id.content, EulaFragment.privacyClauseFragment())
        }
    }
}