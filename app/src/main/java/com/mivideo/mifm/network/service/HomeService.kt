package com.mivideo.mifm.network.service

import com.mivideo.mifm.data.models.jsondata.ChannelList
import com.mivideo.mifm.data.models.jsondata.RecommendList
import retrofit2.http.GET
import retrofit2.http.Query
import rx.Observable

/**
 * Created by Jiwei Yuan on 18-7-25.
 */

interface HomeService {

    /**
     * 獲取首頁推薦列表
     */
    @GET("/mifm/v1/home")
    fun getRecommendData(): Observable<RecommendList>

    @GET("/mifm/v1/categories/list")
    fun getChannelList(@Query("cid") tabId: String, @Query("tab") tabType: String, @Query("atype") atype: String, @Query("next") page: Int): Observable<ChannelList>


}