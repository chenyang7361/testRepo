package com.mivideo.mifm.viewmodel

import android.content.Context
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.MainConfig
import com.mivideo.mifm.data.models.jsondata.ChannelList
import com.mivideo.mifm.data.repositories.HomeRepository
import com.mivideo.mifm.network.service.ServerParameterType
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.util.addTo
import rx.Observable
import timber.log.Timber

/**
 * Created by Jiwei Yuan on 18-7-24.
 */
class ChannelViewModel(val context: Context, val tabId: String) : ListViewModel<ChannelList>(context) {

    private val homeRepository: HomeRepository by instance()

    override fun onRefreshData(): Observable<ChannelList> {
        return homeRepository.getChannelList(tabId,ServerParameterType.TAB_TYPE_NEW,ServerParameterType.ALBUM_TYPE_NORMAL, MainConfig.DEFAULT_FIRST_PAGE_INDEX)
    }

    override fun onLoadMoreData(): Observable<ChannelList> {
        return homeRepository.getChannelList(tabId,ServerParameterType.TAB_TYPE_NEW,ServerParameterType.ALBUM_TYPE_NORMAL, pageNo)
    }

    fun loadRefreshDataFromDb(): Observable<ChannelList> {
        return homeRepository.loadChannelListFromDb(tabId, 1, pageSize)
    }

    fun saveRefreshDataToDb(tabId: String, list: ChannelList) {
        homeRepository.deleteChannelData(tabId)
                .flatMap {
                    homeRepository.saveChannelListToDb(tabId, list)
                }
                .compose(asyncSchedulers())
                .subscribe(
                        { Timber.i("save refresh data to db success") },
                        {
                            it.printStackTrace()
                            Timber.i("save refresh data to db error")
                        }
                )
                .addTo(compositeSubscription)
    }

}