package com.mivideo.mifm.jsbridge

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.mivideo.mifm.MainApp
import com.mivideo.mifm.NetworkManager
import com.mivideo.mifm.R
import com.mivideo.mifm.network.service.ApiClient
import com.mivideo.mifm.share.ShareHelper
import com.mivideo.mifm.socialize.SocializeManager
import com.mivideo.mifm.socialize.share.NewShareInfo
import com.mivideo.mifm.ui.dialog.ShareSheetDialog
import com.mivideo.mifm.ui.widget.LoadTipView
import com.mivideo.mifm.ui.widget.LoadingView
import me.yamlee.jsbridge.NativeComponentProvider
import me.yamlee.jsbridge.WVJBWebViewClient
import me.yamlee.jsbridge.ui.BridgeActivityDelegate
import org.jetbrains.anko.onClick
import timber.log.Timber

/**
 * 自定义JsBridge Web委托
 * @author LiYan
 */
class CustomWebDelegate(var activity: Activity) : BridgeActivityDelegate(activity),
        ComponentProvider, CustomWebActionView, CustomInteraction {
    private var mKodeIn = (activity.application as MainApp).kodein

    private var mSocializeManager: SocializeManager? = null
    private var mHttpClient: ApiClient? = null
    private var mShareHelper: ShareHelper? = null
    private var shareDialog: ShareSheetDialog? = null


    private var transparentTitle: TextView? = null

    private var mUrl: String = ""

    init {
        webView.overScrollMode = View.OVER_SCROLL_NEVER

        var cachePath = activity.cacheDir.absolutePath +
                "/" + activity.packageManager.getPackageInfo(activity.packageName, 0).versionCode
        if (Build.VERSION.SDK_INT <= 20) {
            //4.4以下需手动设置database路径,方使domstorage生效
            cachePath += "/database"
            webView.settings.setDatabasePath(cachePath)
        }
        webView.settings.setAppCachePath(cachePath)
        webHeader.setBackBtnClickListener {
            if (!handleBackBtn()) {
                finishActivity()
            }

        }
        webHeader.setCloseBtnClickListener {
            finishActivity()
        }
    }

    fun transparentContentView(): View {
        val inflater = LayoutInflater.from(activity)
        val view: FrameLayout = inflater.inflate(R.layout.layout_web_transparent, null) as FrameLayout
        view.findViewById<LinearLayout>(R.id.ll_web_title).onClick {
            finishActivity()
        }
        transparentTitle = view.findViewById(R.id.tv_web_title)
        hideHeader()
        view.addView(contentView, 0)
        return view
    }

    fun handleBackBtn(): Boolean {
        if (webView.canGoBack()) {
            webView.goBack()
            return true
        } else if (activity.isTaskRoot) {
            val intent = Intent()
            intent.data = Uri.parse("kuaiest://main/view/tabHost?from=app&targetTab=homeTab")
            startActivity(intent)
            finishActivity()
            return true
        } else {
            return false
        }
    }

    fun handleFragmentBackBtn(): Boolean {
        if (webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return false
    }

    override fun loadUrl(url: String) {
        super.loadUrl(url)
        Timber.i("web load url: $url")
        mUrl = url
    }

    override fun provideSocialManager(): SocializeManager {
        if (mSocializeManager == null) {
            mSocializeManager = SocializeManager.get(activity)
        }
        return mSocializeManager!!
    }

    override fun provideHttpClient(): ApiClient {
        if (mHttpClient == null) {
            mHttpClient = ApiClient(activity.applicationContext)
        }
        return mHttpClient!!
    }

    override fun provideShareHelper(): ShareHelper {
        if (mShareHelper == null) {
            val socialManager = provideSocialManager()
            mShareHelper = ShareHelper(socialManager, "H5")
        }
        return mShareHelper!!
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (mWebViewClient != null) {
            (mWebViewClient as CustomWebViewClient).handleActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onCreateWebViewClient(): WebViewClient {
        if (mWebViewClient == null) {
            val wvBridgeHandler = WVJBWebViewClient.WVJBHandler { data, callback -> }
            mWebViewClient = CustomWebViewClient(webView, wvBridgeHandler, this)
            (mWebViewClient as CustomWebViewClient).enableLogging()
        }

        return mWebViewClient!!
    }

    private inner class CustomWebViewClient(webView: WebView, wvjbHandler: WVJBWebViewClient.WVJBHandler,
                                            componentProvider: NativeComponentProvider)
        : DefaultWebViewClient(webView, wvjbHandler, componentProvider) {

        @TargetApi(Build.VERSION_CODES.M)
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            if (request != null && request.isForMainFrame) {
                showError()
            }
        }

        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return
            }
            showError()
        }
    }

    private fun showError() {
        renderTitle(activity.getString(R.string.data_error))
        if (NetworkManager.isNetworkUnConnected()) {
            showNetUnconnected()
        } else {
            showLoadFail()
        }
    }

    override fun showProgress() {
        super.showProgress()
        showLoading()
    }

    override fun hideProgress() {
        super.hideProgress()
        hideLoading()
    }

    override fun showShareDialog(shareInfo: NewShareInfo, listener: ShareHelper.ShareHelperListener?) {
        if (shareDialog == null) {
            shareDialog = ShareSheetDialog(activity)
        }
        shareDialog?.setDislikeVisible(false)
        shareDialog?.setDownloadVisible(false)
        shareDialog?.setReportVisible(false)
        listener?.let {
            shareDialog?.setShareListener(it)
        }
        shareDialog?.show(shareInfo)
    }

    private var mLoadingView: LoadingView? = null

    override fun showLoading() {
        if (mLoadingView == null) {
            mLoadingView = LoadingView(activity)
            val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            mLoadingView?.layoutParams = layoutParams
            mLoadingView?.setBackgroundColor(Color.parseColor("#ffffff"))
        }
        if (mLoadingView?.parent == null) {
            mWebContainer.addView(mLoadingView)
        }
    }

    override fun hideLoading() {
        if (mLoadingView != null) {
            mWebContainer.removeView(mLoadingView)
        }
    }

    private var mErrorView: LoadTipView? = null

    fun addInfoView() {
        if (mErrorView == null) {
            mErrorView = LoadTipView(activity)
            mErrorView!!.setRetryListener(object : LoadTipView.OnRetryLoadListener {

                override fun retryLoad() {
                    hideInfoView()
                    loadUrl(mUrl)
                }
            })

        }
        if (mErrorView?.parent == null) {
            mWebContainer.addView(mErrorView)
        }
    }

    override fun showNetUnconnected() {
        addInfoView()
        mErrorView!!.showNetUnconnected()
    }

    override fun showLoadFail() {
        addInfoView()
        mErrorView!!.showLoadFail()
    }

    override fun hideInfoView() {
        if (mErrorView != null) {
            mWebContainer.removeView(mErrorView)
        }
    }

    override fun showHeader(title: String) {
        super.showHeader(title)
        transparentTitle?.text = title
    }

    /**
     * 通知H5拉取新数据，刷新页面
     * @param type   当type为sign时，H5获取最新页面数据后会自动签到
     */
    fun refreshWebView(type: String) {
        WebMethodDelegate.callJSMethod(webView, WebMethodDelegate.GET_PAGE_INFO, type)
    }

    fun release() {
        mWebViewClient = null
    }
}