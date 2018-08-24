package com.mivideo.mifm.account

import android.content.Context
import android.text.TextUtils
import com.google.gson.Gson
import com.mivideo.mifm.account.exception.UserCacheErrorException
import com.mivideo.mifm.util.SecurityUtil

/**
 * 用户信息缓存类
 * @author LiYan
 */
class AccountInfoCache(val context: Context) {
    companion object {
        private const val USER_CACHE_KEY = "user_cache"
    }

    private val sp = context.getSharedPreferences(context.packageName + "_preferences",
            Context.MODE_PRIVATE)

    fun save(accountInfo: AccountInfo): AccountInfo {
        val editor = sp.edit()
        val json = Gson().toJson(accountInfo)
        val content = SecurityUtil.enBase64(json)
        editor.putString(USER_CACHE_KEY, content)
        editor.apply()
        return accountInfo
    }

    fun get(): AccountInfo? {
        var accountInfo: AccountInfo? = null
        try {
            val json = SecurityUtil.deBase64(sp.getString(USER_CACHE_KEY, ""))
            accountInfo = Gson().fromJson(json, AccountInfo::class.java)
            if (TextUtils.isEmpty(accountInfo?.getAccessToken())) {
                return null
            }
        } catch(e: Exception) {
            throw UserCacheErrorException()
        }
        return accountInfo
    }

    /**
     * 清除缓存
     */
    fun clear(): Boolean {
        return sp.edit().remove(USER_CACHE_KEY).commit()
    }
}