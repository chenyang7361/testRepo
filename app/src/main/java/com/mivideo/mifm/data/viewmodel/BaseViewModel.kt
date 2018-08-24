package com.mivideo.mifm.data.viewmodel

import android.arch.lifecycle.LifecycleObserver
import android.content.Context
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein

abstract class BaseViewModel(context: Context) : LifecycleObserver, KodeinInjected {

    override val injector = KodeinInjector()

    init {
        injector.inject(context.appKodein())
    }

    open fun release() {

    }

}
