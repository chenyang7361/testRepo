package com.mivideo.mifm.data.repositories

import android.content.Context
import com.mivideo.mifm.account.authenticators.MiSSOAuthenticator
import com.mivideo.mifm.network.service.UserService
import com.xiaomi.accountsdk.account.XMPassport
import com.xiaomi.accountsdk.account.data.XiaomiUserCoreInfo
import com.xiaomi.passport.data.XMPassportInfo

class UserRepository(val userService: UserService) : UserService by userService {
    fun getXiaomiCoreUserInfo(context: Context): XiaomiUserCoreInfo {
        val passportInfo = XMPassportInfo.build(context.applicationContext, MiSSOAuthenticator.SSO_PASSPORT_SERVICE_ID)
        return XMPassport.getXiaomiUserCoreInfo(passportInfo, MiSSOAuthenticator.SSO_PASSPORT_SERVICE_ID, mutableListOf(XiaomiUserCoreInfo.Flag.BASE_INFO, XiaomiUserCoreInfo.Flag.EXTRA_INFO))
    }
}