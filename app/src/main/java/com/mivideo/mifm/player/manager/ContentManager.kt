package com.mivideo.mifm.player.manager

import android.content.Context
import android.util.Log
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.data.models.jsondata.AlbumInfo
import com.mivideo.mifm.data.models.jsondata.HistoryItem
import com.mivideo.mifm.data.models.jsondata.PassageItem
import com.mivideo.mifm.data.repositories.HistoryRepository
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.viewmodel.MediaListViewModel

/**
 * Created by Jiwei Yuan on 18-8-16.
 */
class ContentManager(val mContext: Context) : KodeinInjected {
    /**
     * A Kodein Injected class must be within reach of a Kodein Injector object.
     */
    override val injector = KodeinInjector()

    companion object {
        const val INIT_PAGE: Int = 1
        const val PAGE_LOAD_LIMIT: Int = 2
        const val DEFAULT_PAGE_SIZE: Int = 10
    }

    init {
        injector.inject(mContext.appKodein())
    }

    private lateinit var listener: LoadDataListener

    fun attachLoadDataListener(listener: LoadDataListener) {
        this.listener = listener
    }

    private val historyRepository: HistoryRepository by instance()
    private val mediaListViewModel: MediaListViewModel by instance()
    private var isLoading: Boolean = false


    fun isCurrentItem(albumInfo: AlbumInfo, position: Int, list: List<PassageItem>): Boolean {
        if (position >= list.size) {
            return false
        }
        if (DataContainer.album != null && DataContainer.album!!.id.equals(albumInfo.id)
                && list[position].id.equals(DataContainer.items[DataContainer.index].id)) {
            return true
        }
        return false
    }

    private fun needLoadMore(): Boolean {
        return DataContainer.index >= DataContainer.items.size - PAGE_LOAD_LIMIT
    }

    private var startPage: Int = -1

    fun playAlbum(albumInfo: AlbumInfo, position: Int, list: List<PassageItem>, page: Int): Boolean {
        DataContainer.saveLast()
        DataContainer.album = albumInfo
        DataContainer.index = position
        DataContainer.items.clear()
        DataContainer.items.addAll(list)
        DataContainer.item = DataContainer.items[position]
        startPage = INIT_PAGE
        mediaListViewModel.pageNo = page
        mediaListViewModel.hasMore = albumInfo.hasMore
        if (needLoadMore() && !isLoading) {
            loadMore()
        }
        return true
    }

    fun playNextContent(): PassageItem? {
        if (needLoadMore() && !isLoading) {
            loadMore()
        }
        return DataContainer.getOrderedNext(ListOrder.NORMAL)
    }

    fun playLastContent(): PassageItem? {
        if (needLoadMoreBefore() && !isLoading) {
            loadMoreBefore()
        }
        return DataContainer.getOrderedLast(ListOrder.NORMAL)
    }

    private fun needLoadMoreBefore(): Boolean {
        if (startPage <= INIT_PAGE) {
            return false
        }
        return DataContainer.index <= 1
    }

    private fun loadMoreBefore() {
        if (DataContainer.hasData()) {
            isLoading = true
            mediaListViewModel.loadDataByPage(DataContainer.album!!.id, startPage - 1).compose(asyncSchedulers())
                    .subscribe({
                        if (it.data != null && DataContainer.hasData() && it.data!!.id.equals(DataContainer.album!!.id)) {
                            DataContainer.addHeaderContent(DataContainer.album!!.id, it.data!!.sections)
                            startPage--
                            listener.onLastMoreLoaded()
                            isLoading = false
                            mediaListViewModel.hasMore = it.data!!.has_next
                        }
                    }, {
                        isLoading = false
                        Log.e("AAAA", it.message)
                    })
        }

    }

    fun saveToHistory(lastPosition: Int) {
        if (DataContainer.hasHistory()) {
            historyRepository.saveData(DataContainer.lastAbulm!!, DataContainer.lastItem!!, lastPosition, getPageNo())
                    .subscribe()
        }
    }

    private fun getPageNo(): Int {
        return DataContainer.lastIndex / DEFAULT_PAGE_SIZE + startPage
    }

    private fun loadMore() {
        if (DataContainer.hasData()) {
            isLoading = true
            mediaListViewModel.loadMoreData(DataContainer.album!!.id).compose(asyncSchedulers())
                    .subscribe({
                        if (it.data != null && DataContainer.hasData() && it.data!!.id.equals(DataContainer.album!!.id)) {
                            DataContainer.addFooterContent(DataContainer.album!!.id, it.data!!.sections!!)
                            listener.onNextMoreLoaded()
                            isLoading = false
                            mediaListViewModel.hasMore = it.data!!.has_next
                        }
                    }, {
                        isLoading = false
                    })
        }
    }

    fun playHistory(item: HistoryItem) {
        DataContainer.saveLast()
        DataContainer.album = item.album
        DataContainer.index = 0
        DataContainer.items.clear()
        DataContainer.item = item.item
        startPage = item.pageNo
        isLoading = true
        listener.historyFirstPageLoading()
        mediaListViewModel.hasMore = true
        mediaListViewModel.loadDataByPage(item.album!!.id, item.pageNo).compose(asyncSchedulers())
                .subscribe({
                    DataContainer.addFooterContent(DataContainer.album!!.id, it.data!!.sections!!)
                    listener.historyFirstPageLoaded()
                    for ((index, d) in it.data!!.sections.withIndex()) {
                        if (d.id.equals(item.item?.id)) {
                            DataContainer.index = index
                            DataContainer.item = d
                            break
                        }
                    }
                    mediaListViewModel.hasMore = it.data!!.has_next
                    isLoading = false
                    if (needLoadMore() && !isLoading) {
                        loadMore()
                    }
                    if (needLoadMoreBefore() && !isLoading) {
                        loadMoreBefore()
                    }
                }, {
                    isLoading = false
                })
    }

    interface LoadDataListener {
        fun historyFirstPageLoading()
        fun historyFirstPageLoaded()
        fun onNextMoreLoaded()
        fun onLastMoreLoaded()
    }
}