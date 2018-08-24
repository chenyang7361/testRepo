package com.mivideo.mifm.data.models.jsondata

/**
 * Created by Jiwei Yuan on 18-7-23.
 */

class RecommendList {
    var code: Int = 0
    var data: ArrayList<RecommendData>? = null

}

class RecommendDetailList {
    var code: Int = 0
    var albums: ArrayList<ChannelItem>? = null
}

class RecommendData {

    var atype: Int = DataTypeDesc.NORMAL_ALBUM
//    var has_more: Int = 0
    var name: String = ""
    var list: ArrayList<ChannelItem>? = null
    var stype: Int = 0
}

class DataTypeDesc {
    companion object {
        val NORMAL_ALBUM = 1
        val HEADLINE_ALBUM = 2

        val STYPE_STYLE1 = 1
        val STYPE_STYLE_LIST = 2
        val STYPE_STYLE_GRID = 3
    }
}

