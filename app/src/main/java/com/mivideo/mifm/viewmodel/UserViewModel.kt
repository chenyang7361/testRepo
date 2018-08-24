package com.mivideo.mifm.viewmodel

import android.content.Context
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.data.repositories.UserRepository
import com.xiaomi.accountsdk.account.data.XiaomiUserCoreInfo
import rx.Observable

/**
 * Created by Jiwei Yuan on 18-8-23.
 */
class UserViewModel(val context: Context) : BaseViewModel(context) {
    private val repository: UserRepository by instance()

    fun getXiaomiCoreUserInfo(): Observable<XiaomiUserCoreInfo> {
        return Observable.create({ subscribe ->
            try {
                val coreInfo = repository.getXiaomiCoreUserInfo(context)
                subscribe.onNext(coreInfo)
                subscribe.onCompleted()
            } catch (e: Throwable) {
                subscribe.onError(e)
            }
        })
    }
}