package com.mivideo.mifm.network.service

import com.mivideo.mifm.data.jsondata.TabList
import com.mivideo.mifm.data.models.jsondata.common.CommonUpdateResult
import com.mivideo.mifm.data.models.jsondata.plugins.PluginResult
import com.mivideo.mifm.network.request.FeedBackRequest
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import rx.Observable

/**
 * 应用首页启动，配置等相关Rest Api接口
 *
 * @author LiYan
 */
interface MainService {

    /**
     * 获取首页不同频道Tab数据(待完成)
     */
    @GET("/mifm/v1/categories")
    fun getTabs(@Query("atype") atype: Int): Observable<TabList>

    /**
     * 获取Cp插件信息
     */
    @GET("/api/cp/1/plugin")
    fun getPluginInfo(): Observable<PluginResult>

    /**
     * 获取Cp插件信息(测试接口)
     */
    @GET("http://45.32.40.65/g/fetch_plugin")
    fun getPluginInfoTestData(): Observable<PluginResult>

    /**
     * app升级接口
     */
    @GET("/mifm/v1/update")
    fun appUpdateInfo(@Query("vc") versionCode: Int, @Query("pkg") packageName: String): Observable<CommonUpdateResult>

    /**
     * 用户反馈接口
     */
    @POST("/mifm/v1/feedback")
    fun feedback(@Body request: FeedBackRequest): Observable<JSONObject>
}
