package com.mivideo.mifm.network.service

import com.mivideo.mifm.data.models.jsondata.MediaDetailResult
import com.mivideo.mifm.data.models.jsondata.RecommendDetailList
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import rx.Observable

/**
 * Created by Jiwei Yuan on 18-7-31.
 */
interface DetailService {
    /**
     *获取详情页接口数据
     */
    @GET("/mifm/v1/play/detail")
    fun getMediaDetail(@Query("abid") id: String): Observable<MediaDetailResult>


    /**
     *获取详情页接口数据
     */
    @GET("/mifm/v1/play/list")
    fun getMediaDetailList(@Query("abid") id: String, @Query("next") page: Int): Observable<MediaDetailResult>

    /**
     * 获取详情页推荐数据
     */
    @GET("/mifm/v1/recomm/list")
    fun getMediaRecommendList(@Query("abid") id: String, @Query("next") page: Int = 1): Observable<RecommendDetailList>

}