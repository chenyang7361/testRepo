package com.mivideo.mifm.account.authenticators

import android.content.Context
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.BuildConfig
import com.mivideo.mifm.MainApp
import com.mivideo.mifm.account.AccountInfo
import com.mivideo.mifm.data.repositories.DuoShouRepository
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.socialize.AuthResult
import com.mivideo.mifm.socialize.Socializer
import org.json.JSONObject
import rx.Observable


/**
 * 通过第三方sdk oauth登录(如微信登录)拿到authorize code的来进行后台登录
 * Created by yamlee on 24/05/2017.
 *
 * @author LiYan
 */
class AuthCodeAuthenticator(
        private val mContext: Context,
        private val socializer: Socializer,
        private val channel: String) : BaseUserAuthenticator(mContext) {

    private val duoShouRepository: DuoShouRepository = (mContext as MainApp).kodein.instance()

    override fun startLoginAuth(): Observable<AccountInfo> {
        return socializer.loginAuth()
                .flatMap { t -> flatMapAccountInfo(t) }
    }

    private fun flatMapAccountInfo(authResult: AuthResult): Observable<AccountInfo> {
        return duoShouRepository.authCodeLogin(channel, authResult.authCode, BuildConfig.ACCOUNT_KIND)
                .map { t: JSONObject? ->
                    mapAccountInfo(t)
                }
                .flatMap { t -> updateOwnServerToken(t) }
                .compose(asyncSchedulers())
    }


    override fun logout(): Observable<Boolean> {
        //暂时没有QQ登录的登出逻辑
        return Observable.just(true)
    }
}