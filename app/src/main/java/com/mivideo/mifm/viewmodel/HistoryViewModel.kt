package com.mivideo.mifm.viewmodel

import android.content.Context
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.data.models.jsondata.HistoryItem
import com.mivideo.mifm.data.repositories.HistoryRepository
import rx.Observable

/**
 * Created by Jiwei Yuan on 18-8-3.
 */
class HistoryViewModel(context: Context) : ListViewModel<List<HistoryItem>>(context) {
    private val repository: HistoryRepository by instance()

    override fun onRefreshData(): Observable<List<HistoryItem>> {
        return repository.loadDataFromDb(pageNo, pageSize)
    }

    override fun onLoadMoreData(): Observable<List<HistoryItem>> {
        return repository.loadDataFromDb(pageNo, pageSize)
    }

    fun clearData() {
        repository.clearData()
    }

    fun deleteHistory(key: List<String>): Observable<Boolean> {
        return repository.deleteData(key)
    }

    fun loadDataCount(): Observable<Int> {
        return repository.loadDataCountFromDb()
    }

}