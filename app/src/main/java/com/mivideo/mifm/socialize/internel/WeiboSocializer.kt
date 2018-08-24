package com.mivideo.mifm.socialize.internel

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.mivideo.mifm.R
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.socialize.AuthResult
import com.mivideo.mifm.socialize.BaseSocializer
import com.mivideo.mifm.socialize.ShareResult
import com.mivideo.mifm.socialize.SocializeManager
import com.mivideo.mifm.socialize.exceptions.*
import com.mivideo.mifm.socialize.share.NewShareInfo
import com.mivideo.mifm.util.ApkUtil
import com.mivideo.mifm.util.app.showToast
import com.sina.weibo.sdk.WbSdk
import com.sina.weibo.sdk.api.ImageObject
import com.sina.weibo.sdk.api.TextObject
import com.sina.weibo.sdk.api.WeiboMultiMessage
import com.sina.weibo.sdk.auth.AccessTokenKeeper
import com.sina.weibo.sdk.auth.AuthInfo
import com.sina.weibo.sdk.auth.Oauth2AccessToken
import com.sina.weibo.sdk.auth.WbConnectErrorMessage
import com.sina.weibo.sdk.auth.sso.SsoHandler
import com.sina.weibo.sdk.share.WbShareCallback
import com.sina.weibo.sdk.share.WbShareHandler
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.BehaviorSubject
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import timber.log.Timber

/**
 * 微博OAuth2认证登录器
 *
 * Created by yamlee on 15/05/2017.
 * @author LiYan
 */
class WeiboSocializer(val activity: Activity) : BaseSocializer() {


    companion object {
        private const val AUTH_URL = "https://api.weibo.com/oauth2/authorize"
        const val APP_ID = "2613672466"
        /**
         * WEIBO OAuth认证时应用的回调页，第三方应用可以使用自己的回调页。
         *
         * 注：关于授权回调页对移动客户端应用来说对用户是不可见的，所以定义为何种形式都将不影响，
         * 但是没有定义将无法使用 SDK 认证登录。
         * 建议使用默认回调页：https://api.weibo.com/oauth2/default.html
         *
         */
        const val REDIRECT_URL = "https://kuaishipin.cn/"
        /**
         * Scope 是 OAuth2.0 授权机制中 authorize 接口的一个参数。通过 Scope，平台将开放更多的微博
         * 核心功能给开发者，同时也加强用户隐私保护，提升了用户体验，用户在新 OAuth2.0 授权页中有权利
         * 选择赋予应用的功能。

         * 我们通过新浪微博开放平台-->管理中心-->我的应用-->接口管理处，能看到我们目前已有哪些接口的
         * 使用权限，高级权限需要进行申请。

         * 目前 Scope 支持传入多个 Scope 权限，用逗号分隔。

         * 有关哪些 OpenAPI 需要权限申请，请查看：http://open.weibo.com/wiki/%E5%BE%AE%E5%8D%9AAPI
         * 关于 Scope 概念及注意事项，请查看：http://open.weibo.com/wiki/Scope
         */
        val SCOPE = "email," +
                "direct_messages_read," +
                "direct_messages_write," +
                "friendships_groups_read," +
                "friendships_groups_write," +
                "statuses_to_me_read," +
                "follow_app_official_microblog," +
                "invitation_write"
    }

    private var wbShareAPI: WbShareHandler? = null
    private var mSsoHandler: SsoHandler

    init {
        WbSdk.install(activity.applicationContext,
                AuthInfo(activity.applicationContext, APP_ID, REDIRECT_URL, SCOPE))
        wbShareAPI = WbShareHandler(activity)
        wbShareAPI?.registerApp()
        mSsoHandler = SsoHandler(activity)
    }

    override fun isAppInstalled(): Observable<Boolean> {
        val applicationContext = activity.applicationContext
        val observable = Observable.unsafeCreate<Boolean> {
            val apkResult = ApkUtil.isInstalled(applicationContext, ApkUtil.WB_PACKAGE)
            it.onNext(apkResult)
            it.onCompleted()
        }
        return observable.compose(asyncSchedulers())
    }

    override fun loginAuth(): Observable<AuthResult> {
        //微博登录可以通过oAuth登录不一定需要安装app，所以此处没有验证app是否已安装
        val authSubject = BehaviorSubject<AuthResult>()
        mSsoHandler.authorize(SelfWbAuthListener(authSubject))
        return authSubject
    }

    private inner class SelfWbAuthListener(val subject: BehaviorSubject<AuthResult>)
        : com.sina.weibo.sdk.auth.WbAuthListener {
        override fun onSuccess(weiboToken: Oauth2AccessToken) {
            val authResult = AuthResult(weiboToken.uid, weiboToken.token,
                    weiboToken.refreshToken, weiboToken.expiresTime, "")
            subject.onNext(authResult)
            activity.runOnUiThread(java.lang.Runnable {
                if (weiboToken.isSessionValid()) {
                    // 保存 Token 到 SharedPreferences
                    AccessTokenKeeper.writeAccessToken(activity.applicationContext, weiboToken)
                    Timber.d("weiboToken is $weiboToken")
                }
            })
            subject.onCompleted()
        }

        override fun cancel() {
            Timber.d("weibo auth caneled")
            subject.onError(AuthCancelException(activity.applicationContext))
        }

        override fun onFailure(weiboErr: WbConnectErrorMessage) {
            Timber.d("onFailure() called with: weiboErr = [" + weiboErr.errorMessage + "]")
            with(weiboErr) {
                val authErrorException = AuthErrorException()
                authErrorException.errCode = errorCode
                authErrorException.errMsg = errorMessage
                subject.onError(authErrorException)
            }
        }
    }

    override fun share(info: NewShareInfo): Observable<ShareResult> {
        return isAppInstalled()
                .flatMap { installed ->
                    if (!installed) {
                        val msg = activity.getString(R.string.need_install_qq) ?: ""
                        Observable.error<ShareResult>(SocialAppNotInstallException(msg))
                    } else {
                        shareSubject = BehaviorSubject<ShareResult>()
                        mShareInfo = info
                        if (info.shareContentType == NewShareInfo.SHARE_CONTENT_TYPE_TEXT) {
                            val wbMessage = WeiboMultiMessage()
                            val textObject = TextObject()

                            textObject.text = getWbText(info)
                            wbMessage.textObject = textObject
                            wbShareAPI!!.shareMessage(wbMessage, true)
                        } else {
                            val wbMessage = WeiboMultiMessage()
                            val textObject = TextObject()

                            textObject.text = getWbText(info)
                            wbMessage.textObject = textObject

                            val imageObject = ImageObject()

                            Glide.with(activity.applicationContext)
                                    .load(info.shareImageUrl)
                                    .asBitmap()
                                    .into(object : SimpleTarget<Bitmap>() {
                                        override fun onResourceReady(resource1: Bitmap,
                                                                     glideAnimation: GlideAnimation<in Bitmap>?) {
                                            imageObject.setImageObject(resource1)
                                            wbMessage.imageObject = imageObject
                                            wbShareAPI!!.shareMessage(wbMessage, true)
                                        }
                                    })
                        }

                        shareSubject
                    }
                }

    }

    private fun getWbText(title: String, url: String): String {
        val resultUrl = appendFromToTargetUrl(url, SocializeManager.FROM_WB)
        return "【小米快视频：$title】 点此进入>> $resultUrl (分享自小米快视频)"
    }

    private fun getWbText(shareInfo: NewShareInfo): String {
        if (shareInfo.shareContentType == NewShareInfo.SHARE_CONTENT_TYPE_TEXT) {
            return shareInfo.textContent
        } else {
            val title = shareInfo.title
            val url = shareInfo.targetUrl
            val resultUrl = appendFromToTargetUrl(url, SocializeManager.FROM_WB)
            return "【小米快视频：$title】 点此进入>> $resultUrl (分享自小米快视频)"
        }
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mSsoHandler.authorizeCallBack(requestCode, resultCode, data)
    }

    override fun handleNewIntent(intent: Intent?) {
        wbShareAPI?.doResultIntent(intent, ShareCallback(mShareInfo))
    }

    /**
     * 微博分享回调
     */
    private inner class ShareCallback(private val info: NewShareInfo?) : WbShareCallback {
        override fun onWbShareFail() {
            showToast(activity.applicationContext,
                    activity.resources.getString(R.string.share_toast_error))
            shareSubject.onError(ShareErrorException(info, NewShareInfo.WEIBO))
        }

        override fun onWbShareCancel() {
            showToast(activity.applicationContext,
                    activity.resources.getString(R.string.share_toast_cancel))
            shareSubject.onError(ShareCancelException(activity.applicationContext, info, NewShareInfo.WEIBO))
        }

        override fun onWbShareSuccess() {
            showToast(activity.applicationContext,
                    activity.resources.getString(R.string.share_toast_success))
            shareSubject.onNext(ShareResult(info, NewShareInfo.WEIBO))
            shareSubject.onCompleted()
        }

    }

}