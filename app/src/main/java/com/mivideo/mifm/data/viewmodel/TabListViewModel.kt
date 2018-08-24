package com.mivideo.mifm.data.viewmodel

import android.content.Context
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.data.jsondata.TabList
import com.mivideo.mifm.data.jsondata.Tabs
import com.mivideo.mifm.data.models.TabItemModel
import com.mivideo.mifm.data.repositories.MainRepository
import com.mivideo.mifm.rx.asyncSchedulers
import rx.Observable
import rx.lang.kotlin.BehaviorSubject
import rx.lang.kotlin.addTo
import rx.subscriptions.CompositeSubscription
import timber.log.Timber

/**
 * 首页视频分类集合
 */
class TabListViewModel(val context: Context) : BaseViewModel(context), KodeinInjected {
    private val mainRepository: MainRepository by instance()

    var tabs: List<Tabs> = emptyList()
    var subscription = CompositeSubscription()
    var subject = BehaviorSubject<Int>()

    companion object {
        val DEFAULT_TAB_DATA = "top_tabs.json"
        val DEFAULT_HOME_TAB_ATYPE = 1
    }

    fun getCount(): Int {
        return tabs.size
    }

    fun getItem(position: Int): TabItemModel? {
        return if (tabs.isEmpty()) {
            triggerLoad()
            null
        } else {
            TabItemModel().let {
                it.id = tabs[position].id
                it.name = tabs[position].name
                it.icon = tabs[position].icon
                it
            }
        }
    }

    private fun triggerLoad() {
        if (!subscription.hasSubscriptions()) {
            Observable.create<Boolean> { subscribe ->
                subscribe.onNext(true)
                subscribe.onCompleted()
            }
                    .flatMap {
                        mainRepository.getTabs(DEFAULT_HOME_TAB_ATYPE)
                    }
                    .compose(asyncSchedulers())
                    .onErrorResumeNext { e ->
                        mainRepository.getObservableTabList(TabList())
                    }
                    .flatMap { tabList ->
                        if (tabList?.data != null) {
                            mainRepository.updateDbTabList(tabList)
                            mainRepository.getObservableTabList(tabList)
                        } else {
                            mainRepository.loadTabListFromDb()
                        }
                    }
                    .compose(asyncSchedulers())
                    .onErrorResumeNext { e ->
                        mainRepository.getObservableTabList(TabList())
                    }
                    .flatMap { tabList ->
                        if (tabList?.data != null) {
                            mainRepository.getObservableTabList(tabList)
                        } else {
                            mainRepository.loadTabListFromFile(DEFAULT_TAB_DATA, context)
                        }
                    }
                    .compose(asyncSchedulers())
                    .subscribe({ tabList ->
                        if (tabList?.data != null) {
                            this@TabListViewModel.tabs = tabList.data
                            subject.onNext(tabList.data.size)
                        }
                    }, { err ->
                        Timber.i({ "ERROR: $err" }.invoke())
                    }).addTo(subscription)
        }
    }

    /**
     * 监听数据更新
     */
    fun observeUpdate(): Observable<Int> {
        triggerLoad()
        return subject
    }

    override fun release() {
    }
}
