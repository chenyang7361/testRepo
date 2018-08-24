package com.mivideo.mifm.socialize

import com.mivideo.mifm.socialize.share.NewShareInfo

/**
 * Created by yamlee on 19/05/2017.
 */
data class AuthResult(
        val uid: String,
        val accessToken: String,
        val refreshToken: String,
        val expiredTime: Long,
        val authCode: String)

data class ShareResult(val shareInfo: NewShareInfo?, val target: String)
