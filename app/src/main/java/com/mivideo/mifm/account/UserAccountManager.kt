package com.mivideo.mifm.account

import android.Manifest
import android.accounts.Account
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.PermissionChecker
import android.text.TextUtils
import com.hwangjr.rxbus.RxBus
import com.mivideo.mifm.BuildConfig
import com.mivideo.mifm.EulaActivity
import com.mivideo.mifm.R
import com.mivideo.mifm.SupportActivity
import com.mivideo.mifm.account.authenticators.*
import com.mivideo.mifm.events.PauseMediaEvent
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.events.ResumeMediaEvent
import com.mivideo.mifm.socialize.SocializeManager
import com.mivideo.mifm.socialize.exceptions.AuthCancelException
import com.mivideo.mifm.socialize.exceptions.SocialAppNotInstallException
import com.mivideo.mifm.ui.dialog.LoginDialog2
import com.mivideo.mifm.ui.fragment.TabHostFragment
import com.mivideo.mifm.util.ApkUtil
import com.mivideo.mifm.util.SystemUtil
import com.mivideo.mifm.util.app.showToast
import com.xiaomi.passport.accountmanager.MiAccountManager
import junit.framework.Assert
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import java.util.*

/**
 *用户登录信息统一管理类
 *
 * @see <a href="http://git.mvideo.xiaomi.srv/videodaily/videodaily-android/wikis/account%20module">
 *     用户管理模块文档</a>
 * @author LiYan(modify)
 */
@SuppressWarnings("unused")
class UserAccountManager(val context: Context) {

    companion object {
        const val CHANNEL_WX = "WeChat"
        const val CHANNEL_QQ = "QQ"
        const val CHANNEL_WEIBO = "Weibo"
        const val CHANNEL_XIAOMI = "Xiaomi"
        const val PERMISSION_GET_ACCOUNTS_REQUEST = 1000
    }

    private val accountCache = AccountInfoCache(context.applicationContext)
    //添加账号信息更新监听器
    private var mUserAccountUpdateListeners: MutableList<UserAccountUpdateListener> = ArrayList()
    private var mUserLoginListener: UserLoginListener? = null
    private var wxAuthenticator: AuthCodeAuthenticator? = null
    private var qqAuthenticator: AccessTokenAuthenticator? = null
    private var weiboAuthenticator: AccessTokenAuthenticator? = null
    private var xiaomiAuthenticator: BaseUserAuthenticator? = null
    private var socialMgr: SocializeManager? = null
    private val subscriptions: CompositeSubscription = CompositeSubscription()
    private var miAccountMgr: MiAccountManager = MiAccountManager.get(context.applicationContext)
    private var accountInfo: AccountInfo? = null
    private var isRequesting = false
    private val systemAccount: Account?
        get() {
            for (account in miAccountMgr.accounts) {
                if (TextUtils.equals(account.type, MiAccountManager.XIAOMI_ACCOUNT_TYPE)) {
                    return account
                }
            }
            Timber.d("not find sso system account")
            return null
        }
    var dialog: Dialog? = null


    /**
     * 获取缓存的用户信息
     */
    fun user(): AccountInfo? {
        synchronized(this, {
            if (accountInfo == null) {
                accountInfo = accountCache.get()
            }
            return accountInfo
        })
    }

    /**
     * 获取系统账户信息
     */
    fun systemUser(): Account? {
        return systemAccount
    }

    /**
     * 判断用户是否已登录方法
     */
    fun userLoggedIn(): Boolean {
        val accountInfo = user()
        val accessToken = accountInfo?.getAccessToken()
        return !TextUtils.isEmpty(accessToken)
    }

    /**
     * 进行登录操作
     *
     * 如果当前为外发标准版弹出包含微信，微博，QQ等选项的弹框，让用户进行选择操作
     * 反之如果为白牌只显示loading进行小米登录操作
     */
    fun startLogin(context: Activity, userLoginListener: UserLoginListener? = null) {
        mUserLoginListener = userLoginListener
        buildLoginDialog(context)
        dialog!!.setOnDismissListener {
            RxBus.get().post(ResumeMediaEvent("login"))
        }
        dialog!!.window.attributes.windowAnimations = R.style.publish_dialog
        dialog!!.show()
        RxBus.get().post(PauseMediaEvent("login"))
    }


    private fun buildLoginDialog(context: Activity) {
        val appContext = context.applicationContext
        dialog = LoginDialog2.builder()
                .setLoginListener(object : LoginDialog2.LoginListener() {


                    override fun onClickClose(dialog: LoginDialog2) {
                        dialog.dismiss()
                    }

                    override fun onClickWx(dialog: LoginDialog2) {
                        if (!ApkUtil.isInstalled(appContext, ApkUtil.WX_PACKAGE)) {
                            showToast(appContext, appContext.getString(R.string.need_install_wx))
                            return
                        }
                        dialog.startLoading()
                        val subscription = loginWX(context)
                                .compose(asyncSchedulers())
                                .subscribe(AuthSubscriber(CHANNEL_WX + " ", dialog))
                        subscriptions.add(subscription)
                    }

                    override fun onClickQQ(dialog: LoginDialog2) {
                        if (!ApkUtil.isInstalled(appContext, ApkUtil.QQ_PACKAGE)) {
                            showToast(appContext, appContext.getString(R.string.need_install_qq))
                            return
                        }
                        dialog.startLoading()
                        val subscription = loginQQ(context)
                                .compose(asyncSchedulers())
                                .subscribe(AuthSubscriber(CHANNEL_QQ + " ", dialog))
                        subscriptions.add(subscription)
                    }

                    override fun onClickWeibo(dialog: LoginDialog2) {
                        dialog.startLoading()
                        val subscription = loginWeiBo(context)
                                .compose(asyncSchedulers())
                                .subscribe(AuthSubscriber(CHANNEL_WEIBO + " ", dialog))
                        subscriptions.add(subscription)
                    }

                    override fun onClickXiaoMi(dialog: LoginDialog2) {
                        Observable.create<Boolean> {
                            it.onNext(SystemUtil.isMiui())
                        }
                                .compose(asyncSchedulers())
                                .subscribe { isMiui ->
                                    if (isMiui && systemAccount == null
                                            && PermissionChecker.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS)
                                            == PackageManager.PERMISSION_DENIED) {
                                        if (!isRequesting) {
                                            ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.GET_ACCOUNTS),
                                                    PERMISSION_GET_ACCOUNTS_REQUEST)
                                            isRequesting = true
                                        }
                                    } else {
                                        xiaomiLogin(context)
                                    }
                                }
                    }

                    override fun onClickPrivacyClause(dialog: LoginDialog2) {
                        if (context is SupportActivity) {
                            context.startActivity(EulaActivity.getPrivacyClasueIntent(context))
                        }
                    }

                    override fun onClickUserAgreement(dialog: LoginDialog2) {
                        if (context is SupportActivity) {
                            context.startActivity(EulaActivity.getAgreementIntent(context))
                        }
                    }

                    override fun onClickSuccessConfirm(dialog: LoginDialog2) {
                        super.onClickSuccessConfirm(dialog)
                        if (context is SupportActivity) {
//                            val intent = Intent()
//                            intent.data = Uri.parse("kuaiest://main/view/tabHost?from=accountManager&targetTab=${TabHostFragment.TAB_TASK}")
//                            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
//                            (context as Activity).startActivity(intent)
                            dialog.dismiss()
                        }
                    }
                })
                .build(context)
    }

    private fun xiaomiLogin(context: Activity) {
        if (this.dialog !is LoginDialog2) {
            buildLoginDialog(context)
        }
        val dialog = this.dialog as LoginDialog2
        dialog.startLoading()
        val subscription = loginXiaoMi(context)
                .compose(asyncSchedulers())
                .subscribe(AuthSubscriber(CHANNEL_XIAOMI + " ", dialog))
        subscriptions.add(subscription)
    }

    fun handlePermissionsResult(activity: Activity, requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        isRequesting = false
        when (requestCode) {
            PERMISSION_GET_ACCOUNTS_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    xiaomiLogin(activity)
                } else {    // 用户点了拒绝或直接设置了禁止询问
                    val dialog = AlertDialog.Builder(activity)
                            .setMessage(activity.getString(R.string.login_need_permission))
                            .setPositiveButton(activity.getString(R.string.go_to_setting), { dialogInterface, _ ->
                                dialogInterface.dismiss()

                                val miUiIntent = Intent("miui.intent.action.APP_PERM_EDITOR")
                                miUiIntent.putExtra("extra_pkgname", activity.packageName)

                                // 检测是否有能跳转到权限设置页面
                                val info = activity.packageManager.queryIntentActivities(miUiIntent, PackageManager.MATCH_DEFAULT_ONLY)
                                if (info.size > 0) {
                                    activity.startActivity(miUiIntent)
                                } else {
                                    val packageURI = Uri.parse("package:" + activity.packageName)
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI)
                                    activity.startActivity(intent)
                                }
                            })
                            .setNegativeButton(activity.getString(R.string.cancel), { dialogInterface, _ ->
                                dialogInterface.dismiss()
                            })
                            .create()
                    dialog.show()
                }
            }
        }
    }

    private inner class AuthSubscriber(val tag: String,
                                       val dialog: LoginDialog2) : Subscriber<AccountInfo>() {
        override fun onError(e: Throwable?) {
            Timber.d("$tag auth onError-->${e?.message}")
            dialog.dismiss()
            mUserLoginListener?.loginFail()
            mUserLoginListener = null
            if (e is AuthCancelException) {
                showToast(context, context.getString(R.string.login_cancel))
            } else if (e is SocialAppNotInstallException) {
                showToast(context, e.message ?: context.getString(R.string.app_not_install_yet))
            } else {
                showToast(context, context.getString(R.string.login_fail))
                e?.printStackTrace()
            }
        }

        override fun onNext(t: AccountInfo?) {
            Timber.d("$tag auth onNext-->${t.toString()}")
            if (t == null) {
                showToast(context, context.getString(R.string.login_fail))
                mUserLoginListener?.loginFail()
                mUserLoginListener = null
            } else {
                if (mUserLoginListener == null) {
                    dialog.loadingSuccess()
                } else {
                    mUserLoginListener!!.loginSuccess()
                    dialog.dismiss()
                }
            }
//            showToast(context.applicationContext, context.getString(R.string.login_success))
        }

        override fun onCompleted() {
            Timber.d("$tag auth complete")
//            delayDismissLoginDialog()
        }

        private fun delayDismissLoginDialog() {
            Handler().postDelayed({ dialog.dismiss() }, 1500)
        }

    }


    /**
     * 登出用户并删除用户缓存信息
     */
    fun logout(): Observable<Boolean> {
        accountInfo = null
        wxAuthenticator?.logout()
        qqAuthenticator?.logout()
        xiaomiAuthenticator?.logout()
        weiboAuthenticator?.logout()
        accountCache.clear()
        notifyUserChange(null)
        return Observable.just(true)
    }

    /**
     * 微信登录
     */
    fun loginWX(activity: Activity): Observable<AccountInfo> {
        socialMgr = SocializeManager.get(activity)
        wxAuthenticator = AuthCodeAuthenticator(context.applicationContext,
                socialMgr!!.wx(), CHANNEL_WX)
        return wxAuthenticator!!.startLoginAuth()
                .map { accountCache.save(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { notifyUserChange(it) }
    }

    /**
     * QQ登录
     */
    fun loginQQ(activity: Activity): Observable<AccountInfo> {
        socialMgr = SocializeManager.get(activity)
        qqAuthenticator = AccessTokenAuthenticator(context.applicationContext,
                socialMgr!!.qq(), CHANNEL_QQ)
        return qqAuthenticator!!.startLoginAuth()
                .map { accountCache.save(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { notifyUserChange(it) }
    }

    fun loginXiaoMiOAuth(activity: Activity): Observable<AccountInfo> {
        xiaomiAuthenticator = MiOAuthenticator(activity)
        return xiaomiAuthenticator!!.startLoginAuth()
                .subscribeOn(Schedulers.io())
                .map { accountCache.save(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { notifyUserChange(it) }
    }

    /**
     * 小米登录
     */
    fun loginXiaoMi(activity: Activity): Observable<AccountInfo> {
        return Observable
                .create<Boolean> { subscriber ->
                    subscriber.onNext(SystemUtil.isMiui())
                    subscriber.onCompleted()
                }
                .flatMap { isMiui ->
                    if (isMiui && systemAccount != null) {
                        xiaomiAuthenticator = MiSSOAuthenticator(activity)
                    } else {
                        xiaomiAuthenticator = MiOAuthenticator(activity)
                    }
                    xiaomiAuthenticator!!.startLoginAuth()
                }
                .map { accountCache.save(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    notifyUserChange(it)
                }
                .onErrorResumeNext {
                    if (it !is AuthCancelException) {
                        loginXiaoMiOAuth(activity)
                    } else {
                        Observable.error(it)
                    }
                }
    }

    /**
     * 小米系统用户登录
     */
    fun loginXiaoMiBySystemUser(activity: Activity): Observable<AccountInfo> {
        xiaomiAuthenticator = MiSSOAuthenticator(activity)
        return (xiaomiAuthenticator as MiSSOAuthenticator).startLoginAuth(true)
                .map { accountCache.save(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { notifyUserChange(it) }
    }

    /**
     * 微博登录
     */
    fun loginWeiBo(activity: Activity): Observable<AccountInfo> {
        socialMgr = SocializeManager.get(activity)
        weiboAuthenticator = AccessTokenAuthenticator(context.applicationContext,
                socialMgr!!.weibo(), CHANNEL_WEIBO)
        return weiboAuthenticator!!.startLoginAuth()
                .map { accountCache.save(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { notifyUserChange(it) }
    }

    private fun notifyUserChange(accountInfo: AccountInfo?) {
        for (listener in mUserAccountUpdateListeners) {
            listener.onUserAccountUpdated(accountInfo)
        }
    }

    /**
     * 添加账号信息更新监听器

     * @param listener 监听器
     */
    fun addOnAccountUpdatedListener(listener: UserAccountUpdateListener) {
        Assert.assertNotNull(listener)
        mUserAccountUpdateListeners.add(listener)
    }

    /**
     * 移除账号信息更新监听器

     * @param listener 监听器
     */
    fun removeOnAccountUpdatedListener(listener: UserAccountUpdateListener) {
        Assert.assertNotNull(listener)
        mUserAccountUpdateListeners.remove(listener)
    }


    fun release() {
        weiboAuthenticator = null
        wxAuthenticator = null
        qqAuthenticator = null
        xiaomiAuthenticator = null
        socialMgr?.release()
        socialMgr = null
        dialog = null
        subscriptions.clear()
        mUserLoginListener = null
    }

    fun clearLoginListener() {
        mUserLoginListener = null
    }

}
