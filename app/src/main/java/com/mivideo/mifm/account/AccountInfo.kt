package com.mivideo.mifm.account

import android.support.annotation.IntDef
import com.mivideo.mifm.BuildConfig
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.*

/**
 * 用户账号信息
 */
@SuppressWarnings("unused")
class AccountInfo {

    @IntDef(GENDER_MALE.toLong(), GENDER_FEMALE.toLong(), GENDER_UNKNOWN.toLong())
    @Retention(RetentionPolicy.SOURCE)
    annotation class GenderType

    private var mUserId: String? = null
    private var mNickName: String? = null
    private var mAvatarUrl: String? = null
    private var mGender: Int = 0

    /**
     * 当前服务的对应 AccessToken
     */
    private var mAccessToken: String ?= null

    fun setUserId(userId: String?): AccountInfo {
        this.mUserId = userId
        return this
    }

    /**
     * 设置用户昵称
     */
    fun setNickName(nickName: String?): AccountInfo {
        this.mNickName = nickName
        return this
    }

    /**
     * 设置用户头像地址
     */
    fun setAvatarUrl(avatarAddress: String?): AccountInfo {
        this.mAvatarUrl = avatarAddress
        return this
    }

    /**
     * 设置用户性别
     */
    fun setGender(@GenderType gender: Int): AccountInfo {
        this.mGender = gender
        return this
    }

    fun setGenderInt(gender: Int): AccountInfo {
        if (gender == 1) {
            this.mGender = GENDER_MALE
        } else if (gender == 2) {
            this.mGender = GENDER_FEMALE
        } else {
            this.mGender = GENDER_UNKNOWN
        }
        return this
    }

    /**
     * 根据用户性别字符串设置用户性别
     */
    fun setGenderString(genderString: String): AccountInfo {
        if ("m".equals(genderString, ignoreCase = true)) {
            this.mGender = GENDER_MALE
        } else if ("f".equals(genderString, ignoreCase = true)) {
            this.mGender = GENDER_FEMALE
        } else {
            this.mGender = GENDER_UNKNOWN
        }
        return this
    }

    /**
     * 设置本服务的 ServiceToken
     */
    fun setAccessToken(accessToken: String?): AccountInfo {
        mAccessToken = accessToken
        return this
    }

    /**
     * 获得用户头像地址
     */
    fun getAvatarUrl(): String? {
        return mAvatarUrl
    }

    /**
     * 获得用户的 OpenId（XiaomiId）
     */
    fun getUserId(): String? {
        return mUserId
    }

    /**
     * 获得用户昵称
     */
    fun getNickName(): String? {
        return mNickName
    }

    /**
     * 获得用户性别
     */
    @GenderType
    fun getGender(): Int {
        return mGender
    }

    /**
     * 获得本服务的 ServiceToken
     */
    fun getAccessToken(): String? {
        return mAccessToken
    }

    override fun toString(): String {
        if (BuildConfig.DEBUG)
            return String.format(Locale.getDefault(), "{userId: %s, nickName: %s, " + "avatarUrl: %s, gender: %d, accessToken: %s}",
                    mUserId, mNickName, mAvatarUrl, mGender, mAccessToken)
        else
            return super.toString()
    }

    companion object {
        const val GENDER_UNKNOWN = 0
        const val GENDER_MALE = 1
        const val GENDER_FEMALE = 2
    }
}
