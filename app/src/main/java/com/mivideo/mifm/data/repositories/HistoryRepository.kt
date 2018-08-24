package com.mivideo.mifm.data.repositories

import com.mivideo.mifm.data.db.DbHistory
import com.mivideo.mifm.data.models.jsondata.AlbumInfo
import com.mivideo.mifm.data.models.jsondata.HistoryItem
import com.mivideo.mifm.data.models.jsondata.PassageItem
import com.mivideo.mifm.util.MJson
import rx.Observable
import timber.log.Timber

/**
 * Created by Jiwei Yuan on 18-8-3.
 */
class HistoryRepository {

    fun loadDataFromDb(pageNo: Int, pageSize: Int): Observable<List<HistoryItem>> {
        return Observable.create { subscribe ->
            DbHistory.getQuery()
                    .addDescendingOrder(DbHistory.LAST_UPDATE)
                    .setSkip((pageNo - 1) * pageSize)
                    .setLimit(pageSize)
                    .findInBackground { objects, e ->
                        if (e == null) {
                            val mList = objects.indices.map {
                                val history = HistoryItem()
                                history.album = MJson.getInstance().fromJson<AlbumInfo>(objects[it].getAlbumInfo(), AlbumInfo::class.java)
                                history.item = MJson.getInstance().fromJson<PassageItem>(objects[it].getItemInfo(), PassageItem::class.java)
                                history.lastPosition = objects[it].getLastPosition()
                                history.lastUpdate = objects[it].getLastUpdate()
                                history.id = objects[it].getAlbumId()
                                history.pageNo=objects[it].getPageNo()
                                history
                            }
                            subscribe.onNext(mList)
                            subscribe.onCompleted()
                        } else {
                            Timber.i({ "从db中恢复数据出现错误.........." }.invoke())
                            subscribe.onError(e)
                        }
                    }
        }
    }

    fun loadDataCountFromDb(): Observable<Int> {
        return Observable.create { subscribe ->
            DbHistory.getQuery()
                    .countInBackground { count, e ->
                        if (e == null) {
                            subscribe.onNext(count.toInt())
                            subscribe.onCompleted()
                        } else {
                            subscribe.onError(e)
                        }
                    }
        }
    }

    fun clearData() {

    }

    fun deleteData(key: List<String>): Observable<Boolean> {
        //whereContainedIn调用无效
        return Observable.create { subscribe ->
            DbHistory.getQuery()
                    .findInBackground { objects, e ->
                        if (e == null) {
                            for (obj in objects){
                                if(key.contains(obj.getAlbumId())){
                                    DbHistory.deleteHistory(obj)
                                }
                            }
                            subscribe.onNext(true)
                            subscribe.onCompleted()
                        } else {
                            subscribe.onError(e)
                        }
                    }
        }
    }

    fun saveData(album: AlbumInfo, item: PassageItem, position: Int, pageNo: Int): Observable<Boolean> {
        return Observable.create { subscribe ->
            DbHistory.getQuery().whereEqualTo(DbHistory.ALBUM_ID, album.id)
                    .findInBackground { objects, e ->
                        if (e == null) {
                            if (objects.isEmpty()) {
                                DbHistory.saveHistory(album, item, position,pageNo)
                            } else {
                                DbHistory.updateHistory(item, position, objects[0])
                            }
                            subscribe.onNext(true)
                            subscribe.onCompleted()
                        } else {
                            subscribe.onError(e)
                        }
                    }
        }
    }

}