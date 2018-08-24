package com.mivideo.mifm.data.repositories

import com.mivideo.mifm.data.db.DbChannelList
import com.mivideo.mifm.data.db.DbRecommendList
import com.mivideo.mifm.data.models.jsondata.ChannelList
import com.mivideo.mifm.data.models.jsondata.RecommendData
import com.mivideo.mifm.network.service.HomeService
import com.mivideo.mifm.util.MJson
import com.sabres.SabresObject
import rx.Observable
import timber.log.Timber

/**
 * Created by Jiwei Yuan on 18-7-25.
 */
class HomeRepository(service: HomeService) : HomeService by service {


    fun deleteDbRecommend(): Observable<Boolean> {
        return Observable.create<Boolean> { subscribe ->
            DbRecommendList.getQuery().findInBackground { objects, e ->
                if (e == null) {
                    if (objects != null && objects.size > 0) {
                        SabresObject.deleteAllInBackground(objects) {
                            subscribe.onNext(true)
                            subscribe.onCompleted()
                        }
                    } else {
                        Timber.i("db中的数据为空，取消.......")
                        subscribe.onNext(true)
                        subscribe.onCompleted()
                    }
                } else {
                    Timber.i("获取db缓存数据出现错误.........")
                    subscribe.onError(e)
                }
            }
        }
    }

    fun saveRecommendToDb(data: List<RecommendData>): Observable<Boolean> {
        return Observable.create<Boolean> { subscribe ->
            try {
                DbRecommendList.saveRecommendData(data)
                subscribe.onNext(true)
                subscribe.onCompleted()
            } catch (e: Exception) {
                subscribe.onError(e)
            }
        }
    }

    fun loadRecommendListFromDb(): Observable<List<RecommendData>> {
        return Observable.create({ subscribe ->
            DbRecommendList.getQuery()
                    .findInBackground { objects, e ->
                        if (e == null && objects != null) {
                            val mList = objects.indices.map { MJson.getInstance().fromJson(objects[it].getRecommendData(), RecommendData::class.java) }
                            Timber.i({ " recom 从db中恢复${mList.size}条数据........." }.invoke())
                            subscribe.onNext(mList as List<RecommendData>)
                            subscribe.onCompleted()
                        } else {
                            Timber.i({ "从db中恢复数据出现错误.........." }.invoke())
                            subscribe.onError(e)
                        }
                    }
        })
    }


    /**
     * 从db中恢复数据
     */
    fun loadChannelListFromDb(tabId: String, pageNo: Int, pageSize: Int): Observable<ChannelList> {
        return Observable.create({ subscribe ->
            DbChannelList.getQuery()
                    .whereEqualTo(DbChannelList.TAB_ID, tabId)
                    .setSkip((pageNo - 1) * pageSize)
                    .setLimit(pageSize)
                    .findInBackground { objects, e ->
                        if (e == null && objects != null) {
                            if (objects.isEmpty()) {
                                subscribe.onError(IllegalStateException("no data"))
                            } else {
                                val mList = objects.indices.map { MJson.getInstance().fromJson(objects[it].getChannelData(), ChannelList::class.java) }
                                Timber.i({ "$tabId   从db中恢复${mList.size}条数据........." }.invoke())
                                subscribe.onNext(mList[0])
                                subscribe.onCompleted()
                            }
                        } else {
                            Timber.i({ "从db中恢复数据出现错误.........." }.invoke())
                            subscribe.onError(e)
                        }
                    }
        })
    }


    fun saveChannelListToDb(tabId: String, data: ChannelList): Observable<Boolean> {
        return Observable.create<Boolean> { subscribe ->
            try {
                DbChannelList.saveChannelData(tabId, data)
                subscribe.onNext(true)
                subscribe.onCompleted()
            } catch (e: Exception) {
                subscribe.onError(e)
            }
        }

    }


    /**
     * 删除db中的缓存数据
     */
    fun deleteChannelData(tabId: String): Observable<Boolean> {
        return Observable.create<Boolean> { subscribe ->
            DbChannelList.getQuery().whereEqualTo(DbChannelList.TAB_ID, tabId).findInBackground { objects, e ->
                if (e == null) {
                    if (objects != null && objects.size > 0) {
                        SabresObject.deleteAllInBackground(objects) {
                            subscribe.onNext(true)
                            subscribe.onCompleted()
                        }
                    } else {
                        Timber.i("db中的数据为空，取消.......")
                        subscribe.onNext(true)
                        subscribe.onCompleted()
                    }
                } else {
                    Timber.i("获取db缓存数据出现错误.........")
                    subscribe.onError(e)
                }
            }
        }
    }
}