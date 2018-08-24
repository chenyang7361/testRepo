package com.mivideo.mifm.socialize.internel

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.mivideo.mifm.R
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.socialize.*
import com.mivideo.mifm.socialize.exceptions.*
import com.mivideo.mifm.socialize.share.NewShareInfo
import com.mivideo.mifm.util.ApkUtil
import com.tencent.connect.common.Constants
import com.tencent.connect.share.QQShare
import com.tencent.connect.share.QzoneShare
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import org.json.JSONObject
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.BehaviorSubject
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import timber.log.Timber
import java.util.*

/**
 * QQ登录认证
 * Created by yamlee on 19/05/2017.
 *
 * @author LiYan
 */
class QQSocializer(var activity: Activity?) : BaseSocializer() {

    companion object {
        val SCOPE: String = "get_simple_userinfo,add_topic"
        const val APP_ID = "1106062968"
    }

    private lateinit var authListener: AuthListener
    private var tencent = Tencent.createInstance(APP_ID, activity?.applicationContext)

    init {
        Tencent.createInstance(APP_ID, activity?.applicationContext)
    }


    override fun isAppInstalled(): Observable<Boolean> {
        if (activity == null) return Observable.just(false)
        val applicationContext = activity!!.applicationContext
        val observable = Observable.unsafeCreate<Boolean> {
            val qqInstalled = tencent.isQQInstalled(applicationContext)
            if (qqInstalled) {
                it.onNext(qqInstalled)
            } else {
                val apkResult = ApkUtil.isInstalled(applicationContext, ApkUtil.QQ_PACKAGE) ||
                        ApkUtil.isInstalled(applicationContext, ApkUtil.TIM_PACKAGE)
                it.onNext(apkResult)
            }
            it.onCompleted()
        }
        return observable.compose(asyncSchedulers())

    }

    override fun loginAuth(): Observable<AuthResult> {
        return isAppInstalled()
                .flatMap { installed ->
                    if (!installed) {
                        val msg = activity?.getString(R.string.need_install_qq) ?: ""
                        Observable.error<AuthResult>(SocialAppNotInstallException(msg))
                    } else {
                        val subject = BehaviorSubject<AuthResult>()
                        Timber.d("start qq auth")
                        authListener = AuthListener(subject)
                        tencent?.login(activity, SCOPE, authListener)
                        subject
                    }
                }

    }

    private inner class AuthListener(val subject: BehaviorSubject<AuthResult>) : IUiListener {
        override fun onComplete(data: Any?) {
            Timber.d("qq auth response:$data")
            /*data返回的内容
             {
                "ret":0,
                "openid":"5C3D242E24D5A2E669AF7501863565ED",
                "access_token":"13B1B322E7539E829750404F6AD195E6",
                "pay_token":"BED399FEC33396E58EF8490E548F42BA",
                "expires_in":7776000,
                "pf":"desktop_m_qq-10000144-android-2002-",
                "pfkey":"f97f71607768812e7bc7351f9486d908",
                "msg":"",
                "login_cost":247,
                "query_authority_cost":1397,
                "authority_cost":0
            }
             */
            if (data is JSONObject) {
                val uid = data.opt("openid").toString()
                val accessToken = data.opt("access_token").toString()
                val expireTime = System.currentTimeMillis() +
                        data.opt("expires_in").toString().toLong() * 1000
                val authResult = AuthResult(uid, accessToken, "", expireTime, "")
                subject.onNext(authResult)
            }
            subject.onCompleted()
        }

        override fun onCancel() {
            Timber.d("qq auth cancel")
            subject.onError(AuthCancelException(activity!!.applicationContext!!))
        }

        override fun onError(error: UiError?) {
            val eCode = error?.errorCode
            val eMsg = error?.errorMessage
            val eDetail = error?.errorDetail
            Timber.d("qq auth error:$eCode $eMsg $eDetail")

            val authError = AuthErrorException()
            authError.errCode = eCode.toString()
            authError.errMsg = eMsg!!
            authError.errDetail = eDetail!!
            subject.onError(authError)
        }

    }

    override fun share(info: NewShareInfo): Observable<ShareResult> {
        return isAppInstalled()
                .flatMap { installed ->
                    if (!installed) {
                        val msg = activity?.getString(R.string.need_install_qq) ?: ""
                        Observable.error<ShareResult>(SocialAppNotInstallException(msg))
                    } else {
                        shareSubject = BehaviorSubject()
                        mShareInfo = info
                        if (info.shareContentType == NewShareInfo.SHARE_CONTENT_TYPE_TEXT) {
                            activity?.let {
                                if (info.shareTo == NewShareInfo.QQ) {
                                    ShareUtils.shareToQQText(info.textContent, it.applicationContext)
                                } else if (info.shareTo == NewShareInfo.QQ_ZONE) {
                                    val targetUrl = "http://a.app.qq.com/o/simple.jsp?pkgname=com.mivideo.mifm&ckey=CK1396629838479"
                                    val shareImage = "http://img.cdn.mvideo.xiaomi.com/mobilevideo/10000/3/d00409a1da0220fa3e3c9c02e7624fdb.png"
                                    val shareTitle = activity?.getString(R.string.app_name) ?: ""
                                    val params = Bundle()
                                    params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT)
                                    params.putString(QQShare.SHARE_TO_QQ_TITLE, shareTitle)
                                    params.putString(QQShare.SHARE_TO_QQ_SUMMARY, info.textContent)
                                    val url = appendFromToTargetUrl(targetUrl, SocializeManager.FROM_QS)
                                    params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, url)
                                    val mList = ArrayList<String>()
                                    mList.add(shareImage)
                                    params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, mList)
                                    tencent!!.shareToQzone(activity, params, ShareListener(info))
                                }
                            }
                        } else {
                            val params = Bundle()
                            params.putString(QQShare.SHARE_TO_QQ_TITLE, info.title)
                            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, activity!!.applicationContext.resources.getString(R.string.share_qq_summary))
                            if (info.shareTo == NewShareInfo.QQ) {
                                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT)
                                val url = appendFromToTargetUrl(info.targetUrl, SocializeManager.FROM_QQ)
                                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, url)
                                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, info.shareImageUrl)
                                params.putString(QQShare.SHARE_TO_QQ_APP_NAME, activity?.getString(R.string.app_name))
                                tencent!!.shareToQQ(activity, params, ShareListener(info))
                            } else if (info.shareTo == NewShareInfo.QQ_ZONE) {
                                params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT)
                                val url = appendFromToTargetUrl(info.targetUrl, SocializeManager.FROM_QS)
                                params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, url)
                                val mList = ArrayList<String>()
                                mList.add(info.shareImageUrl)
                                params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, mList)
                                tencent!!.shareToQzone(activity, params, ShareListener(info))
                            }
                        }
                        shareSubject
                    }
                }
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("handleActivityResult:$requestCode $resultCode")
        //登录回调
        if (requestCode == Constants.REQUEST_LOGIN) {
            Tencent.onActivityResultData(requestCode, resultCode, data, authListener)
        }
        // 分享回调
        if (requestCode == Constants.REQUEST_QQ_SHARE ||
                requestCode == Constants.REQUEST_QZONE_SHARE ||
                requestCode == Constants.REQUEST_OLD_SHARE) {
            Tencent.onActivityResultData(requestCode, resultCode, data, ShareListener(mShareInfo))
        }
    }


    private inner class ShareListener(private val info: NewShareInfo?) : IUiListener {
        override fun onComplete(p0: Any?) {
            Timber.d("QQ share complete")
            shareSubject.onNext(ShareResult(info, NewShareInfo.QQ))
            shareSubject.onCompleted()
        }

        override fun onCancel() {
            Timber.d("QQ share cancel")
            shareSubject.onError(ShareCancelException(activity!!.applicationContext, info, NewShareInfo.QQ))
        }

        override fun onError(uiError: UiError?) {
            val shareError = ShareErrorException(info, NewShareInfo.QQ)
            shareError.errCode = uiError?.errorCode.toString()
            shareError.errMsg = uiError?.errorMessage.toString()
            shareError.errDetail = uiError?.errorDetail.toString()
            shareSubject.onError(shareError)
        }

    }


    override fun release() {
        super.release()
        tencent = null
        activity = null
    }
}