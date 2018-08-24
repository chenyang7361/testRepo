package com.mivideo.mifm.data.repositories

import android.content.Context
import android.text.TextUtils
import com.mivideo.mifm.data.db.DbAudioCache
import com.mivideo.mifm.data.db.DbCache
import com.mivideo.mifm.data.models.AudioInfo
import com.mivideo.mifm.data.models.jsondata.common.CommonVideoCache
import com.mivideo.mifm.util.MJson
import com.sabres.SabresObject
import rx.Observable

class CacheRepository(val appContext: Context) {

    fun loadAllCacheData(): Observable<List<CommonVideoCache>> {
        return Observable.create { subscriber ->
            DbAudioCache.getQuery()
                    .addDescendingOrder(DbAudioCache.LAST_UPDATE)
                    .findInBackground { objects, e ->
                        if (e == null) {
                            if (objects != null && objects.size > 0) {
                                val mList = objects.indices
                                        .map {
                                            var key = objects[it].getKey()
                                            var url = objects[it].getUrl()
                                            var path = objects[it].getPath()
                                            var progress = objects[it].getProgress()
                                            var completeSize = objects[it].getComleteSize()
                                            var totalSize = objects[it].getTotalSize()
                                            var state = objects[it].getState()
                                            var audioInfo = MJson.getInstance().fromJson(objects[it].getAudioInfo(), AudioInfo::class.java)
                                            var audio = CommonVideoCache(audioInfo)
                                            var autoStart = objects[it].getAutoStart()
                                            var clicked = objects[it].getClicked()
                                            var failReason = objects[it].getFailReason()
                                            var errorCode = objects[it].getErrorCode()
                                            audio.setKey(key)
                                            audio.setUrl(url)
                                            audio.setPath(path)
                                            audio.setProgress(progress)
                                            audio.setCompleteSize(completeSize)
                                            audio.setTotalSize(totalSize)
                                            audio.setState(state)
                                            audio.setAutoStart(autoStart)
                                            audio.setClicked(clicked)
                                            audio.setFailReason(failReason)
                                            audio.setErrorCode(errorCode)
                                            audio
                                        }
                                subscriber.onNext(mList)
                            } else {
                                subscriber.onNext(ArrayList())
                            }
                        } else {
                            subscriber.onNext(ArrayList())
                        }
                    }
        }
    }

    fun loadCacheData(pageNo: Int, pageSize: Int): Observable<List<CommonVideoCache>> {
        return Observable.create { subscriber ->
            DbAudioCache.getQuery()
                    .addDescendingOrder(DbAudioCache.LAST_UPDATE)
                    .setSkip((pageNo - 1) * pageSize)
                    .setLimit(pageSize)
                    .findInBackground { objects, e ->
                        if (e == null) {
                            if (objects != null && objects.size > 0) {
                                val mList = objects.indices
                                        .map {
                                            var key = objects[it].getKey()
                                            var url = objects[it].getUrl()
                                            var path = objects[it].getPath()
                                            var progress = objects[it].getProgress()
                                            var completeSize = objects[it].getComleteSize()
                                            var totalSize = objects[it].getTotalSize()
                                            var state = objects[it].getState()
                                            var audioInfo = MJson.getInstance().fromJson(objects[it].getAudioInfo(), AudioInfo::class.java)
                                            var audio = CommonVideoCache(audioInfo)
                                            var autoStart = objects[it].getAutoStart()
                                            var clicked = objects[it].getClicked()
                                            var failReason = objects[it].getFailReason()
                                            var errorCode = objects[it].getErrorCode()
                                            audio.setKey(key)
                                            audio.setUrl(url)
                                            audio.setPath(path)
                                            audio.setProgress(progress)
                                            audio.setCompleteSize(completeSize)
                                            audio.setTotalSize(totalSize)
                                            audio.setState(state)
                                            audio.setAutoStart(autoStart)
                                            audio.setClicked(clicked)
                                            audio.setFailReason(failReason)
                                            audio.setErrorCode(errorCode)
                                            audio
                                        }
                                subscriber.onNext(mList)
                            } else {
                                subscriber.onNext(ArrayList())
                            }
                        } else {
                            subscriber.onNext(ArrayList())
                        }
                    }
        }
    }

    fun clearCache() {
        DbCache.getQuery()
                .findInBackground { objects, e ->
                    if (e == null) {
                        if (objects != null && objects.size > 0) {
                            SabresObject.deleteAllInBackground(objects)
                        }
                    }
                }
        DbAudioCache.getQuery()
                .findInBackground { objects, e ->
                    if (e == null) {
                        if (objects != null && objects.size > 0) {
                            SabresObject.deleteAllInBackground(objects)
                        }
                    }
                }
    }

    fun deleteCache(keysToBeDelete: List<String>): Observable<List<String>> {
        if (keysToBeDelete.isEmpty()) {
            return Observable.create { subscriber ->
                subscriber.onNext(ArrayList<String>())
                subscriber.onCompleted()
            }
        }
        return Observable.create { subscriber ->
            var deletedList = ArrayList<String>()
            for (i in 0 until keysToBeDelete.size) {
                var deleteSuccess = false
                try {
                    var key = keysToBeDelete[i]
                    var objs = DbAudioCache.getQuery().whereEqualTo(DbAudioCache.KEY, key).find()
                    SabresObject.deleteAll(objs)
                    // 删除音频缓存时，判断是否同一专辑下的音频都已经删除，如果删除，将专辑信息删除
                    objs.forEach {
                        DbCache.getQuery().whereEqualTo(DbCache.ALBUM_ID, it.getAlbumId())
                                .findInBackground { objects, e ->
                                    if (e == null) {
                                        if (objects != null && objects.size > 0) {
                                            DbCache.deleteCache(objects[0])
                                        }
                                    }
                                }
                    }
                    deleteSuccess = true
                } catch (t: Throwable) {
                    t.printStackTrace()
                    deleteSuccess = false
                }
                if (deleteSuccess) {
                    deletedList.add(keysToBeDelete[i])
                }
            }
            subscriber.onNext(deletedList)
            subscriber.onCompleted()
        }
    }

    fun saveCache(video: CommonVideoCache): Observable<CommonVideoCache> {
        return saveCache(video, false)
    }

    fun saveCache(video: CommonVideoCache, updateTime: Boolean): Observable<CommonVideoCache> {
        if (video == null || TextUtils.isEmpty(video.getKey())) {
            return Observable.just(CommonVideoCache())
        }
        return Observable.create { subscriber ->
            DbAudioCache.getQuery()
                    .whereEqualTo(DbAudioCache.KEY, video.getKey())
                    .findInBackground { objects, e ->
                        if (e == null) {
                            if (objects == null || objects.size == 0) {
                                // TODO: 2018/8/22 本地最大缓存音频逻辑
                                /*DbAudioCache.getQuery().countInBackground { count, sabresException ->
                                    Log.d("CacheCollection", "count = " + count)
                                    if (count > 99) {
                                        DbCache.getQuery().addDescendingOrder(DbCache.LAST_UPDATE).findInBackground { obj, e ->
                                            val c = count - 99
                                            var i = 0
                                            while (i < c) {
                                                DbCache.deleteCache(obj[count.toInt() - ++i])
                                            }
                                            DbCache.saveCache(video)
                                        }
                                    } else {
                                        DbAudioCache.saveCache(video)
                                    }
                                }*/
                                DbAudioCache.saveCache(video)
                                DbCache.getQuery().whereEqualTo(DbCache.ALBUM_ID, video.getAudioInfo()?.albumInfo?.id)
                                        .findInBackground { objects, e ->
                                            if (e == null) {
                                                if (objects == null || objects.size == 0) { // 专辑表信息未缓存
                                                    DbCache.saveCache(video)
                                                }
                                            }
                                        }
                            } else {
                                DbAudioCache.updateCache(video, objects[0], updateTime)
                            }
                        }
                    }
            subscriber.onNext(video)
            subscriber.onCompleted()
        }

    }

    fun getDataByKey(key: String): Observable<CommonVideoCache> {
        return Observable.create { subscriber ->
            DbAudioCache.getQuery()
                    .whereEqualTo(DbAudioCache.KEY, key)
                    .findInBackground { objects, e ->
                        if (e == null) {
                            if (objects != null && objects.size > 0) {
                                var key = objects[0].getKey()
                                var url = objects[0].getUrl()
                                var path = objects[0].getPath()
                                var progress = objects[0].getProgress()
                                var completeSize = objects[0].getComleteSize()
                                var totalSize = objects[0].getTotalSize()
                                var state = objects[0].getState()
                                var audioInfo = MJson.getInstance().fromJson(objects[0].getAudioInfo(), AudioInfo::class.java)
                                var audio = CommonVideoCache(audioInfo)
                                var autoStart = objects[0].getAutoStart()
                                var clicked = objects[0].getClicked()
                                var failReason = objects[0].getFailReason()
                                var errorCode = objects[0].getErrorCode()
                                audio.setKey(key)
                                audio.setUrl(url)
                                audio.setPath(path)
                                audio.setProgress(progress)
                                audio.setCompleteSize(completeSize)
                                audio.setTotalSize(totalSize)
                                audio.setState(state)
                                audio.setAutoStart(autoStart)
                                audio.setClicked(clicked)
                                audio.setFailReason(failReason)
                                audio.setErrorCode(errorCode)
                                subscriber.onNext(audio)
                                subscriber.onCompleted()
                            } else {
                                subscriber.onNext(CommonVideoCache())
                                subscriber.onCompleted()
                            }
                        } else {
                            subscriber.onNext(CommonVideoCache())
                            subscriber.onCompleted()
                        }
                    }
        }
    }
}