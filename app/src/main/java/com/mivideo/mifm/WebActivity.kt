package com.mivideo.mifm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.kuaiest.video.jsbridge.jscall.GetUserProcessor
import com.kuaiest.video.jsbridge.jscall.Go2AppStoreProcessor
import com.kuaiest.video.jsbridge.jscall.HttpGetProcessor
import com.mivideo.mifm.jsbridge.ComponentProvider
import com.mivideo.mifm.jsbridge.CustomWebDelegate
import com.mivideo.mifm.jsbridge.jscall.*
import com.mivideo.mifm.socialize.SocializeManager
import com.mivideo.mifm.ui.fragment.EulaFragment
import com.mivideo.mifm.util.SystemUtil.Companion.isCanChangeStatusBarSystem
import com.mivideo.mifm.util.app.DisplayUtil
import com.mivideo.mifm.util.app.showToast
import me.yamlee.jsbridge.jscall.SetHeaderProcessor
import me.yamlee.jsbridge.model.ListIconTextModel
import me.yamlee.jsbridge.ui.DelegateListener
import qiu.niorgai.StatusBarCompat
import timber.log.Timber

/**
 * WebView界面
 * @author LiYan
 */
class WebActivity : BaseActivity() {
    companion object {

        private const val ARG_URL = "url"
        private const val ARG_THEME = "theme"

        const val VIEW_THEME_TRANSPARENT = "transparent"
        const val VIEW_THREME_NORMAL = "normal"

        /**
         *获得跳转用户协议界面的Intent
         */
        fun getAgreementIntent(sendContext: Context): Intent {
            val intent = Intent(sendContext, WebActivity::class.java)
            intent.putExtra(EulaFragment.ARG_TYPE, EulaFragment.USER_AGREEMENT)
            return intent
        }

        /**
         *获得跳转隐私协议界面的Intent
         */
        fun getPrivacyClasueIntent(sendContext: Context): Intent {
            val intent = Intent(sendContext, WebActivity::class.java)
            intent.putExtra(EulaFragment.ARG_TYPE, EulaFragment.PRIVACY_CLAUSE)
            return intent
        }

        /**
         * 获取跳转指定URL的Intent
         */
        fun getUrlJumpIntent(url: String, sendContext: Context, theme: String = VIEW_THREME_NORMAL): Intent {
            val intent = Intent(sendContext, WebActivity::class.java)
            intent.putExtra(ARG_URL, url)
            intent.putExtra(ARG_THEME, theme)
            return intent
        }
    }

    private var activityDelegate: CustomWebDelegate? = null
    private var socialManager: SocializeManager? = null

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
        socialManager = SocializeManager.get(this)
        if (param == EulaFragment.USER_AGREEMENT) {
            loadRootFragment(android.R.id.content, EulaFragment.userAgreementFragment())
        } else if (param == EulaFragment.PRIVACY_CLAUSE) {
            loadRootFragment(android.R.id.content, EulaFragment.privacyClauseFragment())
        } else {
            handleCommonWebViewLoad(urlArg, routerUrl, theme)
        }
    }

    private fun handleCommonWebViewLoad(urlArg: String?, routerUrl: String?, theme: String?) {
        val url = urlArg ?: routerUrl
        if (!TextUtils.isEmpty(url)) {
            activityDelegate = CustomWebDelegate(this)
            activityDelegate?.setDelegateListener(object : DelegateListener {
                override fun onClickErrorView() {
                }

                override fun onClickHeaderRight(clickUri: String?) {
                }

                override fun onClickMoreMenuItem(menuItem: ListIconTextModel?) {
                }

            })
            addJsCallProcessors()
            if (theme == VIEW_THEME_TRANSPARENT) {
                StatusBarCompat.translucentStatusBar(this, true)
                setContentView(activityDelegate?.transparentContentView())
            } else {
                setContentView(activityDelegate?.contentView)
            }
            activityDelegate?.loadUrl(url!!)
        } else {
            showToast(applicationContext, "数据错误，请退出重试")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        socialManager?.handleActivityResult(requestCode, resultCode, data)
        activityDelegate?.handleActivityResult(requestCode, resultCode, data)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        socialManager?.handleNewIntent(intent)
    }

    private fun addJsCallProcessors() {
        val provider: ComponentProvider = activityDelegate!!
        activityDelegate?.addJsCallProcessor(ShareJsCallProcessor(provider))
        activityDelegate?.addJsCallProcessor(ShareSpecificJsCallProcessor(provider))
        activityDelegate?.addJsCallProcessor(OpenNewViewProcessor(provider))
        activityDelegate?.addJsCallProcessor(WxAuthProcessor(provider))
        activityDelegate?.addJsCallProcessor(SetHeaderProcessor(provider))
        activityDelegate?.addJsCallProcessor(SetHeaderRightProcessor(provider))
        activityDelegate?.addJsCallProcessor(GetUserProcessor(provider))
        activityDelegate?.addJsCallProcessor(HttpGetProcessor(provider))
        activityDelegate?.addJsCallProcessor(HttpPostProcessor(provider))
        activityDelegate?.addJsCallProcessor(SetStatusBarProcessor(provider))
        activityDelegate?.addJsCallProcessor(ClipboardProcessor(provider))
        activityDelegate?.addJsCallProcessor(StartLoginProcessor(provider))
        activityDelegate?.addJsCallProcessor(GetAppInfoProcessor(provider))
        activityDelegate?.addJsCallProcessor(Go2AppStoreProcessor(provider))

    }

    override fun onDestroy() {
        super.onDestroy()
        socialManager?.release()
        activityDelegate?.release()
    }

    override fun onBackPressedSupport() {
        if (activityDelegate?.handleBackBtn() == true) {
            return
        } else {
            super.onBackPressedSupport()
        }
    }

}