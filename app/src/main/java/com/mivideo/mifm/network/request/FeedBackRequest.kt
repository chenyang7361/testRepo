package com.mivideo.mifm.network.request

/**
 * 用户反馈请求
 * @param content 反馈信息
 * @param ftype 反馈类型
 * @param contact 联系方式
 */
data class FeedBackRequest(val content: String,
                           val ftype: String,
                           val contact: String)