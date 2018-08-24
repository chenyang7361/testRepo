package com.mivideo.mifm.di

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.provider
import com.mivideo.mifm.data.repositories.*
import com.mivideo.mifm.network.service.*
import retrofit2.Retrofit

val APP_CONTEXT_TAG = "appContext"

val repositoryModule = Kodein.Module {

    bind<MainRepository>() with provider {
        val retrofit = instance<Retrofit>()
        val service = retrofit.create(MainService::class.java)
        MainRepository(service)
    }

    bind<HomeRepository>() with provider {
        val retrofit = instance<Retrofit>()
        val service = retrofit.create(HomeService::class.java)
        HomeRepository(service)
    }

    bind<UserRepository>() with provider {
        val retrofit = instance<Retrofit>()
        val service = retrofit.create(UserService::class.java)
        UserRepository(service)
    }

    bind<DuoShouRepository>() with provider {
        val retrofit = instance<Retrofit>(DI_RETROFIT_DUOSHOU)
        val service = retrofit.create(DuoShouService::class.java)
        DuoShouRepository(service)
    }

    bind<CacheRepository>() with provider {
        CacheRepository(instance(APP_CONTEXT_TAG))
    }

    bind<DetailRepository>() with provider {
        val retrofit = instance<Retrofit>()
        val service = retrofit.create(DetailService::class.java)
        DetailRepository(service)
    }

//    bind<VideoRepository>() with provider {
//        val retrofit = instance<Retrofit>()
//        val videoService = retrofit.create(VideoService::class.java)
//        VideoRepository(videoService, instance(APP_CONTEXT_TAG))
//    }

    bind<CollectRepository>() with provider {
        val retrofit = instance<Retrofit>()
        val service = retrofit.create(CollectService::class.java)
        CollectRepository(service)
    }

    bind<HistoryRepository>() with provider {
        HistoryRepository()
    }

    bind<SearchRepository>() with provider {
        val retrofit = instance<Retrofit>()
        val service = retrofit.create(SearchService::class.java)
        SearchRepository(service)
    }

    bind<SplashRepository>() with provider {
        val retrofit = instance<Retrofit>()
        val service = retrofit.create(SplashService::class.java)
        SplashRepository(service)
    }
}
