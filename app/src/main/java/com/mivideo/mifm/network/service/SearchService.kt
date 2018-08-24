package com.mivideo.mifm.network.service

import com.mivideo.mifm.data.models.jsondata.SearchResult
import retrofit2.http.GET
import retrofit2.http.Query
import rx.Observable

/**
 * Created by Jiwei Yuan on 18-8-9.
 */
interface SearchService {
    /**
     * 搜索所有内容
     *
     * @param searchKey 搜索的内容
     * @param pageNum 搜索结果分页的第几页
     */
    @GET("/mifm/v1/search")
    fun search(@Query("q") searchKey: String,
               @Query("page_no") pageNum: Int): Observable<SearchResult>

//    /**
//     * 搜索作者
//     *
//     * @param searchKey 作者相关的关键词
//     * @param pageNum 搜索结果分页的第几页
//     * @param pageSize 搜索结果分页的页数大小
//     */
//    @GET("/api/search_author/2")
//    fun searchAuthor(@Query("q") authorKey: String,
//                     @Query("page_no") pageNum: Int,
//                     @Query("page_size") pageSize: Int): Observable<SearchAuthor>
//
//
//    /**
//     * 搜索页获取热搜标签
//     */
//    @GET("/api/search_hotword/2")
//    fun getHotSearch(): Observable<HotWordInfo>
//
//
//    /**
//     * 获取搜索提示词,搜索输入框中默认的提示词会填写其中，这个词就是
//     * 通过此接口获取
//     */
//    @GET("/api/search_keyword/1")
//    fun getSearchHintKey(): Observable<HotWordInfo>
}