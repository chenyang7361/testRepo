package com.mivideo.mifm.network.service

import com.mivideo.mifm.data.models.jsondata.VideoInfo
import retrofit2.http.GET
import retrofit2.http.Query
import rx.Observable

/**
 * 视频列表相关Rest api接口
 */
interface VideoService {

    /**
     * 获取视频详情页信息
     *
     * @param videoId 视频Id
     * @param pageNum 分页加载的第几页
     * @param pageSize 分页加载每页的个数
     * @param flag 当flag为 1 时，只拉取相关视频，屏蔽运营推荐数据，默认为null
     */
    @GET("/api/video/2")
    fun getVideoDetailInfo(@Query("vid") videoId: String,
                           @Query("page_no") pageNum: Int,
                           @Query("page_size") pageSize: Int,
                           @Query("noop") flag: String? = null): Observable<VideoInfo>


}