package com.mivideo.mifm.viewmodel

import android.arch.lifecycle.LifecycleObserver
import android.content.Context
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein

/**
 * 基础ViewModel,将通用逻辑可放于此类中
 * Created by aaron on 2017/12/13.
 * @author aaron
 * @author LiYan （modify）
 */
abstract class BaseViewModel(context: Context) : LifecycleObserver, KodeinInjected {

    override val injector = KodeinInjector()

    init {
        injector.inject(context.appKodein())
    }

    open fun release() {

    }

}
