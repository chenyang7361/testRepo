package com.mivideo.mifm.viewmodel

import android.content.Context
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.data.models.jsondata.MediaDetailResult
import com.mivideo.mifm.data.models.jsondata.RecommendDetailList
import com.mivideo.mifm.data.repositories.DetailRepository
import rx.Observable

class MediaDetailViewModel(context: Context) : BaseViewModel(context), KodeinInjected {

    private val repository: DetailRepository by instance()

    fun loadDetailData(id:String): Observable<MediaDetailResult> {
        return repository.getMediaDetail(id)
    }

    fun loadRecommendData(id:String): Observable<RecommendDetailList> {
        return repository.getMediaRecommendList(id)
    }

}
