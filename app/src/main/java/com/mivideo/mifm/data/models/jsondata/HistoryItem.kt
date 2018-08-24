package com.mivideo.mifm.data.models.jsondata

import com.mivideo.mifm.ui.adapter.managedelete.Managable

/**
 * Created by Jiwei Yuan on 18-8-3.
 */

class HistoryItem : Managable() {

    var album: AlbumInfo? = null
    var item: PassageItem? = null
    var lastUpdate: Long = 0
    var lastPosition: Int = 0
    var pageNo: Int = 0
}
