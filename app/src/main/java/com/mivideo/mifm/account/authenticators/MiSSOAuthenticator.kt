package com.mivideo.mifm.account.authenticators

import android.accounts.Account
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.BuildConfig
import com.mivideo.mifm.MainApp
import com.mivideo.mifm.account.AccountInfo
import com.mivideo.mifm.account.exception.GetTokenFailedException
import com.mivideo.mifm.account.exception.LogoutErrorException
import com.mivideo.mifm.account.exception.UserAccountException
import com.mivideo.mifm.account.exception.UserAccountNotFoundException
import com.mivideo.mifm.data.api.APIUrl
import com.mivideo.mifm.data.repositories.UserRepository
import com.mivideo.mifm.push.MiPushManager
import com.mivideo.mifm.socialize.exceptions.AuthCancelException
import com.xiaomi.accountsdk.account.XMPassport
import com.xiaomi.accountsdk.account.data.XiaomiUserInfo
import com.xiaomi.accountsdk.request.AuthenticationFailureException
import com.xiaomi.accountsdk.request.SecureRequest
import com.xiaomi.accountsdk.request.SimpleRequest
import com.xiaomi.accountsdk.utils.EasyMap
import com.xiaomi.passport.accountmanager.MiAccountManager
import com.xiaomi.passport.data.XMPassportInfo
import rx.Observable
import rx.Subscriber
import rx.subjects.BehaviorSubject
import timber.log.Timber
import java.io.UnsupportedEncodingException
import java.util.*

/**
 * 基于小米Passport登录的账户认证
 */
class MiSSOAuthenticator(private val activity: Activity) : BaseUserAuthenticator(activity.applicationContext) {

    companion object {
        private val TAG = MiSSOAuthenticator::class.java.simpleName
        //        private val SSO_PASSPORT_SERVICE_ID = "tvbsso"
        val SSO_PASSPORT_SERVICE_ID = "duoshou"
        private val SSO_LOGIN_API_URL = APIUrl.DOMAIN_ACCOUNT + "/user/sso_login"
        private val CHANNEL_NAME = "Xiaomi_"
        private val PASSPORT_SERVICE_ID = "passportapi"
    }

    private val mAppContext: Context = activity.applicationContext
    private val userRepo: UserRepository = (mAppContext as MainApp).kodein.instance()
    private var mState: String? = null
    private val mAccountManager: MiAccountManager
    private val systemAccount: Account?
        get() {
            for (account in mAccountManager.accounts) {
                if (TextUtils.equals(account.type, MiAccountManager.XIAOMI_ACCOUNT_TYPE)) {
                    return account
                }
            }
            Log.d(TAG, "not find sso system account")
            return null
        }

    init {
        mState = generateFlag()
        mAccountManager = MiAccountManager.get(mAppContext)
        if (mAccountManager.canUseSystem()) {
            Timber.i("userSystemAccount")
            mAccountManager.setUseSystem()
        } else {
            Timber.i("userLocalAccount")
            mAccountManager.setUseLocal()
        }
    }

    private fun generateFlag(): String {
        return Random(SystemClock.uptimeMillis()).nextLong().toString()
    }

    override fun startLoginAuth(): Observable<AccountInfo> {
        return startLoginAuth(false)

    }

    /**
     * 开始进行sso登录
     *
     * @param justCheckSystemUser 此参数用来辅助系统账号自动登录，当值为true时sso登录根据拿到的缓存账号进行token登录
     *                            没有拿到缓存账号就取消操作；当值为false时如果拿到系统账号进行token登录，没有拿到账号会
     *                            跳转sso登录界面让用户输入用户名，密码继续进行登录操作
     *
     */
    fun startLoginAuth(justCheckSystemUser: Boolean): Observable<AccountInfo> {
        return ssoLogin(justCheckSystemUser, activity, SSO_PASSPORT_SERVICE_ID, SSO_LOGIN_API_URL)
                .flatMap { ssoAccountInfo -> flatMapAccountInfo(ssoAccountInfo) }

    }

    private fun ssoLogin(justCheckSystemUser: Boolean,
                         activity: Activity,
                         sid: String,
                         url: String): Observable<SSOAccountInfo> {
        Log.d(TAG, "ssoLogin start")
        val account = systemAccount
        if (account != null) {
            return ssoLogin(sid, url)
        } else if (justCheckSystemUser) {
            return Observable.error(AuthCancelException(activity.applicationContext))
        } else {
            return Observable
                    .create<String>({ subscriber ->
                        blockingAddXiaomiAccount(activity, subscriber)
                    })
                    .flatMap { name ->
                        if (!TextUtils.isEmpty(name)) {
                            ssoLogin(sid, url)
                        } else {
                            Log.d(TAG, "system account not found")
                            Observable.error<SSOAccountInfo>(UserAccountNotFoundException())
                        }
                    }
        }
    }


    private fun blockingAddXiaomiAccount(activity: Activity,
                                         subscriber: Subscriber<in String>): String? {
        Log.d(TAG, "blockingAddXiaomiAccount")

        // 当前已经存在小米账户，直接返回
        val accounts = mAccountManager.accounts
        for (account in accounts) {
            if (TextUtils.equals(account.type, "com.xiaomi")) {
                subscriber.onNext(account.name)
                subscriber.onCompleted()
                return account.name
            }
        }

        val authTokenType = PASSPORT_SERVICE_ID
        val requiredFeatures: Array<String>? = null
        val addAccountOptions = Bundle()

        val future = mAccountManager.addAccount(
                MiAccountManager.XIAOMI_ACCOUNT_TYPE,
                authTokenType,
                requiredFeatures,
                addAccountOptions,
                activity, null, null)// Activity
        // Callback
        // Handler

        try {
            val bundle = future.result

            // 正常值返回 XiaomiId
            val accountName = bundle.getString(MiAccountManager.KEY_ACCOUNT_NAME)
            //            Log.d(TAG, "blockingAddXiaomiAccount accountName: " + accountName);

            if (accountName != null) {
                subscriber.onNext(accountName)
                subscriber.onCompleted()
                return accountName
            }

            // 返回 Intent 表示需要用户登录
            //            Intent intent = bundle.getParcelable(MiAccountManager.KEY_INTENT);
            //            if (intent != null) {
            //                Bundle extras = intent.getExtras();
            //                for (String key : extras.keySet()) {
            //                }
            //                return null;
            //            }

            val errorCode = bundle.getInt(MiAccountManager.KEY_ERROR_CODE)
            if (errorCode == MiAccountManager.ERROR_CODE_CANCELED) {
                throw AuthCancelException(activity.applicationContext)
            }
            val errorMessage = bundle.getString(MiAccountManager.KEY_ERROR_MESSAGE)
            Timber.d(TAG, "blockingAddXiaomiAccount errorCode: $errorCode; errorMessage: $errorMessage")

        } catch (e: Exception) {
            subscriber.onError(e)
        }
        return null
    }

    private fun ssoLogin(sid: String,
                         url: String): Observable<SSOAccountInfo> {
        return Observable.create(Observable.OnSubscribe<SSOAccountInfo> { subscriber ->
            Log.d(TAG, "ssoLogin begin")
            val account = systemAccount
            if (account == null) {
                Log.d(TAG, "not find account")
                subscriber.onError(UserAccountNotFoundException())
                return@OnSubscribe
            }
            val passportInfo = XMPassportInfo.build(mAppContext.applicationContext, sid)
            val cookies = EasyMap<String, String>()
                    .easyPut("cUserId", passportInfo.encryptedUserId)
                    .easyPut("serviceToken", passportInfo.serviceToken)

            var content: SimpleRequest.StringContent? = null
            try {
                val params = EasyMap<String, String>()
                        .easyPut("userId", passportInfo.userId)
                        .easyPut("kind", BuildConfig.ACCOUNT_KIND)
                content = SecureRequest.getAsString(url, params, cookies, true, passportInfo.security)
            } catch (e: Exception) {
                if (e is AuthenticationFailureException) {
                    passportInfo.refreshAuthToken(mAppContext)
//                    mAccountManager.invalidateAuthToken(account.type, passportInfo.serviceToken + ";" + passportInfo.security)
                }
                e.printStackTrace()
                subscriber.onError(e)
            }

            if (content != null && !TextUtils.isEmpty(content.body)) {
                val info = XMPassportInfo.build(mAppContext.applicationContext, PASSPORT_SERVICE_ID)
                var userInfo: XiaomiUserInfo? = null
                try {
                    userInfo = XMPassport.getXiaomiUserInfo(info)
                } catch (e: Exception) {
                    if (e is AuthenticationFailureException) {
                        passportInfo.refreshAuthToken(mAppContext)
                    }
                    e.printStackTrace()
                    subscriber.onError(e)
                }

                val ssoAccountInfo = SSOAccountInfo()
                ssoAccountInfo.uid = userInfo!!.userId
                ssoAccountInfo.token = content.body
                ssoAccountInfo.nickname = userInfo.userName
                ssoAccountInfo.avatarUrl = userInfo.avatarAddress
                subscriber.onNext(ssoAccountInfo)
                subscriber.onCompleted()
            } else {
                subscriber.onError(GetTokenFailedException())
            }
        }).retry(1)
    }


    private fun flatMapAccountInfo(info: SSOAccountInfo): Observable<AccountInfo> {
        Log.d(TAG, "onSuccess")
        var uid = CHANNEL_NAME + info.uid!!
        try {
            uid = Base64.encodeToString(uid.toByteArray(charset("UTF-8")), Base64.DEFAULT)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            throw UserAccountException()
        }

        return userRepo.tokenUpdate(uid, info.token ?: "", MiPushManager.get().getRegId())
                .map { jsonObject ->
                    val data = jsonObject.optJSONObject("data")
                    val token = data.optString("token")
                    val accountInfo = AccountInfo()
                    accountInfo.setUserId(CHANNEL_NAME + info.uid!!)
                    accountInfo.setAvatarUrl(info.avatarUrl)
                    accountInfo.setNickName(info.nickname)
                    accountInfo.setAccessToken(token)
                    accountInfo
                }
    }


    override fun logout(): Observable<Boolean> {
        return removeAccount()
                .flatMap { aBoolean ->
                    mState = generateFlag()
                    Observable.just(aBoolean)
                }
    }

    private fun removeAccount(): Observable<Boolean> {
        Log.d(TAG, "removeAccount")
        val subject = BehaviorSubject.create<Boolean>()
        if (mAccountManager.isUseLocal) {
            val account = systemAccount
            if (account == null) {
                Log.d(TAG, "not find account")
                subject.onError(UserAccountNotFoundException())
            } else {
                mAccountManager.removeAccount(account, {
                    val account = systemAccount
                    if (account == null) {
                        subject.onNext(true)
                        subject.onCompleted()
                    } else {
                        subject.onError(LogoutErrorException())
                    }
                }, null)
            }
        } else {
            subject.onNext(true)
            subject.onCompleted()
        }

        return subject
    }


    inner class SSOAccountInfo {
        var uid: String? = null
        var token: String? = null
        var nickname: String? = null
        var avatarUrl: String? = null
    }


}
