package com.mivideo.mifm.ui.fragment

import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import com.mivideo.mifm.R
import com.mivideo.mifm.data.api.APIUrl
import kotlinx.android.synthetic.main.fragment_eula.*
import org.jetbrains.anko.onClick

/**
 * 使用条款
 */
class EulaFragment : BaseFragment() {

    companion object {
        val USER_AGREEMENT = "user-agreement"
        val PRIVACY_CLAUSE = "privacy-clause"
        private val PRIVACY_URL = "http://www.miui.com/res/doc/privacy/cn.html"
        val ARG_TYPE = "type"

        fun userAgreementFragment(): EulaFragment {
            val fragment = EulaFragment()
            val args = Bundle()
            args.putString(ARG_TYPE, USER_AGREEMENT)
            fragment.arguments = args
            return fragment
        }

        fun privacyClauseFragment(): EulaFragment {
            val fragment = EulaFragment()
            val args = Bundle()
            args.putString(ARG_TYPE, PRIVACY_CLAUSE)
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_eula, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        simpleTitleBar.backLayout!!.onClick {
            if (!handleWebViewBack()) {
                activity?.finish()
            }

        }
    }

    private fun handleWebViewBack(): Boolean {
        if (wv_eula.canGoBack()) {
            wv_eula.goBack()
            return true
        } else {
            return false
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val type = arguments?.getString(ARG_TYPE)
        setWebView(wv_eula)
        if (type == USER_AGREEMENT) {
            simpleTitleBar.setText(getString(R.string.video_mine_use_rule))
//            wv_eula.loadUrl(APIUrl.H5_AGREEMENT_USER)
            //TODO
            wv_eula.loadUrl(PRIVACY_URL)

        } else {
            simpleTitleBar.setText(getString(R.string.privacy_clause))
            wv_eula.loadUrl(PRIVACY_URL)
        }
    }

    private fun setWebView(webView: WebView) {
        //优先使用缓存:
        //LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
        //LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据。
        //LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
        //LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。
        val settings = webView.settings
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        // 开启 DOM storage API 功能
        settings.domStorageEnabled = true
        //开启 database storage API 功能
        settings.databaseEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        //开启 Application Caches 功能
        settings.setAppCacheEnabled(true)
        webView.setWebChromeClient(object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                pb_web_view?.progress = newProgress
            }
        })
        webView.setWebViewClient(object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                pb_web_view?.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                pb_web_view?.visibility = View.GONE

            }


            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
            }


            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url)
                return false
            }
        })
    }

    override fun onBackPressedSupport(): Boolean {
        if (handleWebViewBack()) {
            return true
        } else {
            return super.onBackPressedSupport()
        }
    }
}