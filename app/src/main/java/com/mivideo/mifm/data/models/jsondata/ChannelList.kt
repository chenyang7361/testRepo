package com.mivideo.mifm.data.models.jsondata

/**
 * Created by Jiwei Yuan on 18-7-25.
 */
class ChannelList {
    var code: Int = 0
    var data: ArrayList<ChannelItem>? = null
    var stype: Int = 0
    var has_next: Boolean =false
}

class ChannelItem {
    var id: Long = 0
    var ctype: Int = 0
    var title: String = ""
    var url: String = ""
    var cover: String = ""
    var img: String = ""
    var from_now = ""
    var author = ""
    var desc = ""
    var clientStyle: Int = 0//客户端添加 默认的展示样式
}
