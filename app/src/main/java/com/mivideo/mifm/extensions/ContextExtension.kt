package com.mivideo.mifm.extensions

import android.content.Context
import org.json.JSONObject

/**
 * 小米数据平台，内部BI数据统计打点方法
 */
fun Context.statistics(action: String, category: String, ext: JSONObject? = null) {
//    Statistics.logTrack(this, action, category, ext)
}

/**
 * 小米开放平台数据统计
 */
fun Context.miStatistics(action: String, category: String, params: Map<String, String> = mapOf()) {
//    recordCalculateEvent(category, action, 1, params)
}
