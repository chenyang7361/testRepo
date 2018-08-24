package com.mivideo.mifm.di

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.provider
import com.github.salomonbrys.kodein.singleton
import com.mivideo.mifm.data.viewmodel.CacheViewModel
import com.mivideo.mifm.data.viewmodel.ConfigViewModel
import com.mivideo.mifm.viewmodel.MediaListViewModel

val viewModelModule = Kodein.Module {
    val APP_CONTEXT_TAG = "appContext"

    bind<ConfigViewModel>() with singleton {
        ConfigViewModel(instance(APP_CONTEXT_TAG))
    }

    bind<MediaListViewModel>() with provider {
        MediaListViewModel(instance(APP_CONTEXT_TAG))
    }

    bind<CacheViewModel>() with provider {
        CacheViewModel(instance(APP_CONTEXT_TAG))
    }
}
