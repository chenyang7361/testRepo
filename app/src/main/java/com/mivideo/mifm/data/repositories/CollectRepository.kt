package com.mivideo.mifm.data.repositories

import com.mivideo.mifm.data.models.jsondata.CollectResult
import com.mivideo.mifm.data.models.jsondata.MediaDetailData
import com.mivideo.mifm.network.service.CollectService
import rx.Observable

/**
 * Created by Jiwei Yuan on 18-8-2.
 */
class CollectRepository(service: CollectService) : CollectService by service {

    fun collectMedia(data: MediaDetailData): Observable<CollectResult> {
        val collectData = CollectMediaData()
        collectData.id = data.id.toInt()
        collectData.title = data.title
        collectData.cover = data.cover
        collectData.author = data.author
        return collectOneMedia(collectData)
    }

    class CollectMediaData {
        var id: Int = -1
        var title: String = ""
        var cover: String = ""
        var author: String = ""
    }

}