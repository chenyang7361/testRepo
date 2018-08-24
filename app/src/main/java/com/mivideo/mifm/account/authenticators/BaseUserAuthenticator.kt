package com.mivideo.mifm.account.authenticators

import android.content.Context
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.MainApp
import com.mivideo.mifm.account.AccountInfo
import com.mivideo.mifm.account.UserAuthenticator
import com.mivideo.mifm.data.repositories.UserRepository
import com.mivideo.mifm.push.MiPushManager
import org.json.JSONObject
import rx.Observable
import timber.log.Timber


/**
 * 登录器的抽象基类
 * Created by yamlee on 25/05/2017.
 * @author LiYan
 */
abstract class BaseUserAuthenticator(private val mContext: Context) : UserAuthenticator {
    private val userRepo: UserRepository = (mContext as MainApp).kodein.instance()

    protected fun mapAccountInfo(json: JSONObject?): AccountInfo {
        val accountInfo = AccountInfo()
        accountInfo.setAccessToken(json?.optString("serviceToken"))
        accountInfo.setUserId(json?.optString("id"))
        accountInfo.setAvatarUrl(json?.optString("icon"))
        accountInfo.setGender(getGender(json?.optString("gender") ?: ""))
        accountInfo.setNickName(json?.optString("nickName"))
        return accountInfo
    }

    private fun getGender(genderStr: String): Int {
        var result = AccountInfo.GENDER_UNKNOWN
        when (genderStr) {
            "male" -> result = AccountInfo.GENDER_MALE
            "female" -> result = AccountInfo.GENDER_FEMALE
            else -> result = AccountInfo.GENDER_UNKNOWN
        }
        return result
    }

    protected fun updateOwnServerToken(accountInfo: AccountInfo): Observable<AccountInfo> {
        return userRepo.tokenUpdate(accountInfo.getUserId() ?: "",
                accountInfo.getAccessToken() ?: "", MiPushManager.get().getRegId())
                .map { t: JSONObject? ->
                    mapAccessToken(t, accountInfo)
                }
    }

    protected fun mapAccessToken(t: JSONObject?, accountInfo: AccountInfo): AccountInfo {
        Timber.d("json ->${t?.toString()}")
        val result = accountInfo
        val data: JSONObject? = t?.optJSONObject("data")
        Timber.d("json data ->${data?.toString()}")
        result.setAccessToken(data?.optString("token"))
        return result
    }

}