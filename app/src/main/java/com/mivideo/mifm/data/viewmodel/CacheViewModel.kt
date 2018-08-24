package com.mivideo.mifm.data.viewmodel

import android.content.Context
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.data.models.jsondata.common.CommonVideoCache
import com.mivideo.mifm.data.repositories.CacheRepository
import rx.Observable

class CacheViewModel(context: Context): ListViewModel<List<CommonVideoCache>>(context) {

    private val cacheRepository: CacheRepository by instance()

    override fun onRefreshData(): Observable<List<CommonVideoCache>> {
        return cacheRepository.loadCacheData(pageNo, pageSize)
    }

    override fun onLoadMoreData(): Observable<List<CommonVideoCache>> {
        return cacheRepository.loadCacheData(pageNo, pageSize)
    }

    fun clearData() {
        cacheRepository.clearCache()
    }

    fun deleteData(keysToBeDelete: List<String>): Observable<List<String>> {
        return cacheRepository.deleteCache(keysToBeDelete)
    }

    fun loadAllCacheData(): Observable<List<CommonVideoCache>> {
        return cacheRepository.loadAllCacheData()
    }

    fun saveCache(video: CommonVideoCache, updateTime: Boolean): Observable<CommonVideoCache> {
        return cacheRepository.saveCache(video, updateTime)
    }

    fun saveCache(video: CommonVideoCache): Observable<CommonVideoCache> {
        return cacheRepository.saveCache(video)
    }

    fun getDataByKey(key: String): Observable<CommonVideoCache> {
        return cacheRepository.getDataByKey(key)
    }
}