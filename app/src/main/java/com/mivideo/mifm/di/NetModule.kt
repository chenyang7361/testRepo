package com.mivideo.mifm.di

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
import com.mivideo.mifm.network.interceptor.CommonParamsAddInterceptor
import com.mivideo.mifm.network.interceptor.CustomHttpLoggingInterceptor
import com.mivideo.mifm.network.interceptor.ErrorHandleInterceptor
import com.mivideo.mifm.data.api.APIUrl
import com.mivideo.mifm.network.converter.JSONConverterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val DI_RETROFIT_DUOSHOU = "duoshou"

val netModule = Kodein.Module {

    /**
     * 构建Retrofit实例
     */
    bind<Retrofit>() with singleton {
        val retrofit = Retrofit.Builder()
                .client(instance())
                .baseUrl(APIUrl.BASE_URL)
                .addConverterFactory(JSONConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
        retrofit
    }

    /**
     * 构建账号Retrofit实例
     */
    bind<Retrofit>(DI_RETROFIT_DUOSHOU) with singleton {
        val retrofit = Retrofit.Builder()
                .client(instance())
                .baseUrl(APIUrl.DOMAIN_ACCOUNT)
                .addConverterFactory(JSONConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
        retrofit
    }


    /**
     * 构建OkHttpClient实例
     */
    bind<OkHttpClient>() with singleton {
        val clientBuilder = OkHttpClient.Builder()
        clientBuilder.addInterceptor(CommonParamsAddInterceptor(instance("appContext")))
        clientBuilder.addInterceptor(ErrorHandleInterceptor(instance("appContext")))

        val httpLogInterceptor = CustomHttpLoggingInterceptor()
        httpLogInterceptor.setLevel(CustomHttpLoggingInterceptor.Level.BODY)
        clientBuilder.addInterceptor(httpLogInterceptor)

        val okHttpClient = clientBuilder.connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()
        okHttpClient
    }
}
