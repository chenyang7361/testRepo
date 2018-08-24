package com.mivideo.mifm.di

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.eagerSingleton
import com.github.salomonbrys.kodein.instance
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher

val tuningModule = Kodein.Module {

    bind<RefWatcher>() with eagerSingleton {
        LeakCanary.install(instance())
    }
}
