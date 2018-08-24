package com.mivideo.mifm.network.service

import org.json.JSONObject
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import rx.Observable

/**
 * DuoShou服务（小米sso登录，oAuth登录相关）
 */
interface DuoShouService {
    /**
     * oauth登录：通过authCode登录（如微信登录）
     */
    @POST("/user/login")
    @FormUrlEncoded
    fun authCodeLogin(@Field("channel") channel: String,
                      @Field("code") authCode: String,
                      @Field("kind") accountKind: String): Observable<JSONObject>

    /**
     * oAuth登录：通过accessToken登录（如微博，QQ登录）
     */
    @POST("/user/login2")
    @FormUrlEncoded
    fun accessTokenLogin(@Field("channel") channel: String,
                         @Field("uid") uid: String,
                         @Field("sender") sender: String,
                         @Field("token") accessToken: String,
                         @Field("kind") accountKind: String): Observable<JSONObject>
}