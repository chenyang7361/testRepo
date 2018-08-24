package com.mivideo.mifm.viewmodel

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.data.models.jsondata.RecommendData
import com.mivideo.mifm.data.models.jsondata.RecommendList
import com.mivideo.mifm.data.repositories.HomeRepository
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.util.addTo
import rx.Observable
import rx.subscriptions.CompositeSubscription
import timber.log.Timber

/**
 * Created by Jiwei Yuan on 18-7-24.
 */
class RecommendViewModel(val context: Context) : BaseViewModel(context), KodeinInjected {

    private val homeRepository: HomeRepository by instance()

    private val compositeSubscription = CompositeSubscription()

    override fun release() {
        compositeSubscription.clear()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        release()
    }

    fun loadRecommendData(): Observable<RecommendList> {
        return homeRepository.getRecommendData()
    }

    fun saveRefreshDataToDb(data: List<RecommendData>) {
        homeRepository.deleteDbRecommend()
                .flatMap {
                    homeRepository.saveRecommendToDb(data)
                }
                .compose(asyncSchedulers())
                .subscribe(
                        { Timber.i("save refresh data to db success") },
                        { Timber.i("save refresh data to db error") }
                )
                .addTo(compositeSubscription)
    }

    fun loadRefreshDataFromDb(): Observable<List<RecommendData>> {
        return homeRepository.loadRecommendListFromDb()
    }
}