package com.mivideo.mifm.network.service

import org.json.JSONObject
import retrofit2.http.GET
import retrofit2.http.Query
import rx.Observable

/**
 * 用户信息相关Rest Api接口
 */
interface UserService {

    @GET("/api/login/3")
    fun tokenUpdate(@Query("uid") uid: String,
                    @Query("token") token: String,
                    @Query("regid") regId: String?): Observable<JSONObject>

}