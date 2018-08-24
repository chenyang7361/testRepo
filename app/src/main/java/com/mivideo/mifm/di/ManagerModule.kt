package com.mivideo.mifm.di

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
import com.mivideo.mifm.SpManager
import com.mivideo.mifm.account.UserAccountManager
import com.mivideo.mifm.cache.AudioCacheManager
import com.mivideo.mifm.cpplugin.PluginManager
import com.mivideo.mifm.manager.OrientationManager
import com.mivideo.mifm.player.VideoProxySource

/**
 * Manager模块
 */
val managerModule = Kodein.Module {

    val APP_CONTEXT_TAG = "appContext"

    bind<OrientationManager>() with singleton {
        OrientationManager(instance(APP_CONTEXT_TAG))
    }

    bind<SpManager>() with singleton {
        SpManager(instance(APP_CONTEXT_TAG))
    }

    /**
     * 账户管理器
     */
    bind<UserAccountManager>() with singleton {
        UserAccountManager(instance(APP_CONTEXT_TAG))
    }

    bind<PluginManager>() with singleton {
        PluginManager(instance(APP_CONTEXT_TAG))
    }

    bind<AudioCacheManager>() with singleton {
        AudioCacheManager(instance(APP_CONTEXT_TAG))
    }

    bind<VideoProxySource>() with singleton {
        VideoProxySource(instance(APP_CONTEXT_TAG))
    }
}
