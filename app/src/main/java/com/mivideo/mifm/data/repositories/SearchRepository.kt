package com.mivideo.mifm.data.repositories

import com.mivideo.mifm.network.service.SearchService

/**
 * Created by Jiwei Yuan on 18-8-9.
 */

class SearchRepository(service: SearchService) : SearchService by service {
    /**
     * 获取搜索提示词wrapper方法
     */
//    fun getSearchHintKeyStr(): Observable<String> {
//        return getSearchHintKey().map { hotWordInfo ->
//            if (hotWordInfo!!.data != null && hotWordInfo!!.data!!.hotword != null &&
//                    hotWordInfo.data!!.hotword.size > 0) {
//                hotWordInfo.data!!.hotword[0].keyword
//            } else {
//                ""
//            }
//        }
//    }
}