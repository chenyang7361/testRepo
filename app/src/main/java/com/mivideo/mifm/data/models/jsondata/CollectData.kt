package com.mivideo.mifm.data.models.jsondata

/**
 * Created by Jiwei Yuan on 18-8-2.
 */

class CollectData {
    var code: Int = 0
    var Data: CollectResultData? = null
}

class CollectResultData {
    var albums: ArrayList<CollectItem>? = null
}

class CollectItem {
    val id: Int = 0
    val title: String = ""
    val author: String = ""
    val cover: String = ""
}

class CollectResult {
    var code: Int = 0
    var data: CollectStatus? = null
}

class CollectStatus {
    var is_marked: Boolean = false
    var status: String = ""
}