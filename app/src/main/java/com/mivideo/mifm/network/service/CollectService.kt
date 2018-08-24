package com.mivideo.mifm.network.service

import com.mivideo.mifm.data.models.jsondata.CollectData
import com.mivideo.mifm.data.models.jsondata.CollectResult
import com.mivideo.mifm.data.repositories.CollectRepository
import retrofit2.http.*
import rx.Observable

/**
 * Created by Jiwei Yuan on 18-8-2.
 */

interface CollectService {

    /**
     * 获取收藏列表数据
     */
    @GET("/mifm/v1/users/marks")
    fun getCollectList(): Observable<CollectData>

    /**
     * 查询专辑是否被收藏
     */
    @GET("/mifm/v1/users/mark")
    fun isCollectMedia(@Query("abid") mId: String): Observable<CollectResult>

    /**
     * 收藏专辑
     */
    @POST("/mifm/v1/users/mark")
    fun collectOneMedia(@Body body: CollectRepository.CollectMediaData): Observable<CollectResult>

    /**
     * 取消收藏专辑
     */
    @DELETE("/mifm/v1/users/mark")
    fun unCollectMedia(@Query("abid") mId: String): Observable<CollectResult>
}