package com.mivideo.mifm.network.service

import com.mivideo.mifm.data.models.jsondata.SplashInfo
import retrofit2.http.GET
import retrofit2.http.Query
import rx.Observable

/**
 *  Create by lei.tong on 2018/8/22.
 **/
interface SplashService {

    @GET("/mifm/v1/splash")
    fun getSplash(@Query("wxh") wxh:String, @Query("uid") uid:String, @Query("token") token:String): Observable<SplashInfo>
}