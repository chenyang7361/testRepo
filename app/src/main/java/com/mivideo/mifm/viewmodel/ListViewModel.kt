package com.mivideo.mifm.viewmodel

import android.arch.lifecycle.*
import android.content.Context
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import rx.Observable
import rx.subscriptions.CompositeSubscription

/**
 * Created by aaron on 2017/12/13.
 */
abstract class ListViewModel<E>(context: Context) : BaseViewModel(context) {

    var pageNo: Int = 1
    open var pageSize: Int = 10
    private var lastPageNo: Int = 1
    val compositeSubscription = CompositeSubscription()


    open fun updatePageNoByRefresh() {
        lastPageNo = pageNo
        pageNo = 1
    }

    open fun updatePageNoByLoadMore() {
        lastPageNo = pageNo
        pageNo++
    }

    open fun resetPageNo() {
        pageNo = lastPageNo
    }

    fun refreshData(): Observable<E> {
        updatePageNoByRefresh()
        return onRefreshData()
                .doOnError { resetPageNo() }
    }

    fun loadMoreData(): Observable<E> {
        updatePageNoByLoadMore()
        return onLoadMoreData()
                .doOnError { resetPageNo() }
    }

    protected abstract fun onRefreshData(): Observable<E>

    protected abstract fun onLoadMoreData(): Observable<E>

    override fun release() {
        compositeSubscription.clear()
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        release()
    }
}