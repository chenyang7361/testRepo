package com.mivideo.mifm.account.authenticators

import android.accounts.OperationCanceledException
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.TextUtils
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.BuildConfig
import com.mivideo.mifm.MainApp
import com.mivideo.mifm.account.AccountInfo
import com.mivideo.mifm.data.repositories.DuoShouRepository
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.socialize.AuthResult
import com.mivideo.mifm.socialize.exceptions.AuthCancelException
import com.xiaomi.account.openauth.XiaomiOAuthConstants
import com.xiaomi.account.openauth.XiaomiOAuthorize
import org.json.JSONObject
import rx.Observable
import rx.Subscriber
import timber.log.Timber
import java.util.*

/**
 * 基于小米 OAuth 的账号服务管理器
 *
 * @author LiYan(modify)
 */
class MiOAuthenticator(private val activity: Activity) : BaseUserAuthenticator(activity.applicationContext) {

    companion object {
        internal val sKeepCookies = true

        internal val sScopes = intArrayOf(XiaomiOAuthConstants.SCOPE_PROFILE,
                XiaomiOAuthConstants.SCOPE_OPEN_ID)

        private val CHANNEL_XIAOMI = "Xiaomi"
        private val APP_ID = BuildConfig.XIAOMI_APPID
        private val REDIRECT_URL = "http://duoshou.pandora.xiaomi.com/user/xm_callback"

        fun isEmpty(s: String?): Boolean {
            return s == null || s.isEmpty()
        }
    }

    private var mState: String? = null
    private var mAccountInfo: AccountInfo? = null
    private var mLocalAccountInfo: AccountInfo? = null
    private val mAppContext = activity.applicationContext
    private val mHandler: Handler

    private val duoShouRepository: DuoShouRepository = (mAppContext as MainApp).kodein.instance()

    private val fileKey: String
        get() = String.format(Locale.getDefault(), "%s.USER_ACCOUNT_MANAGER_PREF_FILE_KEY",
                mAppContext.packageName)

    private val tokenKey: String
        get() = String.format(Locale.getDefault(), "%s.TOKEN_KEY",
                mAppContext.packageName)

    private val nickName: String
        get() = String.format(Locale.getDefault(), "%s.NICK_NAME_KEY",
                mAppContext.packageName)

    private val avatarUrlKey: String
        get() = String.format(Locale.getDefault(), "%s.AVATAR_URL_KEY",
                mAppContext.packageName)

    private val genderKey: String
        get() = String.format(Locale.getDefault(), "%s.GENDER_KEY",
                mAppContext.packageName)

    private val userIdKey: String
        get() = String.format(Locale.getDefault(), "%s.USER_ID_KEY",
                mAppContext.packageName)

    init {
        mLocalAccountInfo = loadUserAccount(activity.applicationContext)
        mState = generateFlag()
        mHandler = Handler(Looper.getMainLooper())
    }

    private fun loadUserAccount(appContext: Context): AccountInfo? {
        val sharedPref = appContext.getSharedPreferences(
                fileKey, Context.MODE_PRIVATE)
        val token = sharedPref.getString(tokenKey, null)
        val uid = sharedPref.getString(userIdKey, null)
        if (!isEmpty(token) && !isEmpty(token)) {
            val account = AccountInfo()
            account.setUserId(uid)
            account.setAccessToken(token)
            account.setAvatarUrl(sharedPref.getString(avatarUrlKey, null))
            account.setGenderInt(sharedPref.getInt(genderKey, 0))
            account.setNickName(sharedPref.getString(nickName, null))
            return account
        }

        return null
    }


    /**
     * 开始小米OAuth登录验证
     * 如果已经登录过，使用保存的 Token 进行服务端验证
     *
     * @return 用户信息Observable对象
     */
    override fun startLoginAuth(): Observable<AccountInfo> {
        if (mLocalAccountInfo != null) {
            return loginByAccessToken(mLocalAccountInfo!!.getAccessToken()!!)
        } else {
            return loginByOAuth2(activity)
        }
    }

    private fun loginByAccessToken(accessToken: String): Observable<AccountInfo> {
        val savedFlag = mState
        val authResult = AuthResult("", accessToken, "", 0L, "")
        return requestUserInfo(authResult, savedFlag!!)
    }

    private fun loginByOAuth2(activity: Activity): Observable<AccountInfo> {
        val savedFlag = mState
        return Observable.create(XiaomiOAuthorizeOnSubscribe(activity, APP_ID.toLong(), REDIRECT_URL))
                .flatMap { this.requestUserInfo(it, savedFlag!!) }
    }

    private fun requestUserInfo(authResult: AuthResult, savedFlag: String): Observable<AccountInfo> {
        return duoShouRepository.authCodeLogin(CHANNEL_XIAOMI, authResult.authCode, BuildConfig.ACCOUNT_KIND)
                .map { mapAccountInfo(it) }
                .filter { userAccount -> TextUtils.equals(savedFlag, mState) }
                .flatMap { updateOwnServerToken(it) }
                .compose(asyncSchedulers())
    }


    /**
     * 退出登录
     *
     * 更新账户管理器的周期标识，变相取消当前所有正在执行的请求
     */
    override fun logout(): Observable<Boolean> {
        mLocalAccountInfo = null
        mAccountInfo = null
        mState = generateFlag()
        saveUserAccount(mAppContext, null)
        // saveScopes(null);
        return Observable.just(true)
    }


    private fun generateFlag(): String {
        return Random(SystemClock.uptimeMillis()).nextLong().toString()
    }

    private fun saveUserAccount(appContext: Context, accountInfo: AccountInfo?) {
        val sharedPref = appContext.getSharedPreferences(
                fileKey, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        if (accountInfo == null) {
            editor.putString(tokenKey, null)
            editor.putString(userIdKey, null)
            editor.putString(nickName, null)
            editor.putString(avatarUrlKey, null)
            editor.putInt(genderKey, 0)
        } else {
            editor.putString(tokenKey, accountInfo.getAccessToken())
            editor.putString(userIdKey, accountInfo.getUserId())
            editor.putString(nickName, accountInfo.getNickName())
            editor.putString(avatarUrlKey, accountInfo.getAvatarUrl())
            editor.putInt(genderKey, accountInfo.getGender())
        }
        editor.apply()
    }


    inner class XiaomiOAuthorizeOnSubscribe(private val mActivity: Activity,
                                            private val mAppId: Long,
                                            private val mRedirectUrl: String) : Observable.OnSubscribe<AuthResult> {

        override fun call(subscriber: Subscriber<in AuthResult>) {
            val state = Random().nextLong().toString()
            try {
                val authorize = XiaomiOAuthorize()
                        .setAppId(mAppId)
                        .setRedirectUrl(mRedirectUrl)
                        .setState(state)// Passport 调整完获取用户性别的权限后导致客户端 FC
                        .setScope(sScopes)
                        .setKeepCookies(MiOAuthenticator.sKeepCookies)
                        .setSkipConfirm(true)

                // 「设置 MIUI 环境」和「跳过用户确认」必须是系统签名才可以
                //            if (Utils.isSystemSignature(mActivity)) {
                //                authorize.setNoMiui(false);
                //                // 「跳过确认」可以在 MIUI 平台上自动登录当前用户
                //                // authorize.setSkipConfirm(true);
                //            } else {
//                                authorize.setNoMiui(true);
                //            }

                val future = authorize.startGetOAuthCode(mActivity)

                val results = future.result

                if (results.hasError()) {
                    val jsonObject = JSONObject()
                    jsonObject.put("errorCode", results.errorCode)
                    jsonObject.put("errorMessage", results.errorMessage)
//                    if(results.errorCode == XiaomiOAuthConstants.)
                    subscriber.onError(Exception(jsonObject.toString()))
                } else {
                    //                if (!TextUtils.equals(results.getState(), state)) {
                    //                    Utils.e("State mismatch: [%s] != [%s]", state, results.getState());
                    //                }
                    val code = results.code ?: ""
                    val authResult = AuthResult("", "", "", -1L, code)
                    Timber.d("xiaomi oauth result: code:$code ")
                    subscriber.onNext(authResult)
                    subscriber.onCompleted()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (e is OperationCanceledException) {
                    subscriber.onError(AuthCancelException(mAppContext))
                } else {
                    subscriber.onError(e)
                }
            }
        }
    }

}

