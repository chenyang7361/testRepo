package com.mivideo.mifm.viewmodel

import android.content.Context
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.data.models.jsondata.SplashInfo
import com.mivideo.mifm.data.repositories.SplashRepository
import com.mivideo.mifm.util.app.DisplayUtil
import rx.Observable

/**
 *  Create by lei.tong on 2018/8/22.
 **/
class SplashViewModel(context: Context) : BaseViewModel(context) {
    private val repository: SplashRepository by instance()

    fun getSplash(uid: String, token: String) : Observable<SplashInfo> {

        return repository.getSplash("${DisplayUtil.screenWidthPx}×${DisplayUtil.screenHeightPx}", uid, token)
    }
}