package com.mivideo.mifm.viewmodel

import android.content.Context
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.data.models.jsondata.MediaDetailResult
import com.mivideo.mifm.data.repositories.DetailRepository
import rx.Observable

/**
 * Created by Jiwei Yuan on 18-8-15.
 */

class MediaListViewModel(context: Context) : ListViewModel<MediaDetailResult>(context) {
    private val repository: DetailRepository by instance()
    lateinit var id: String
    var hasMore = true

    fun loadMoreData(id: String): Observable<MediaDetailResult> {
        this.id = id
        updatePageNoByLoadMore()
        return onLoadMoreData()
                .doOnError { resetPageNo() }
    }

    fun loadDataByPage(id: String, page: Int): Observable<MediaDetailResult> {
        this.id = id
        pageNo = page
        return repository.getMediaDetailList(id, pageNo)
    }

    override fun onRefreshData(): Observable<MediaDetailResult> {
        return Observable.empty()
    }

    override fun onLoadMoreData(): Observable<MediaDetailResult> {
        if (!hasMore) {
            return Observable.error(IllegalStateException())
        }
        return repository.getMediaDetailList(id, pageNo)
    }
}