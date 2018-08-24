package com.mivideo.mifm.viewmodel

import android.content.Context
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.data.models.jsondata.CollectData
import com.mivideo.mifm.data.repositories.CollectRepository
import rx.Observable

class CollectListViewModel(context: Context) : ListViewModel<CollectData>(context) {

    private val collectRepository: CollectRepository by instance()

    override fun onRefreshData(): Observable<CollectData> {
        return collectRepository.getCollectList()
    }

    override fun onLoadMoreData(): Observable<CollectData> {
        return collectRepository.getCollectList()
    }
}