package com.mivideo.mifm.data.repositories

import android.content.Context
import android.util.Log
import com.mivideo.mifm.data.db.DbHistory
import com.mivideo.mifm.data.models.CommonVideoHistory
import com.mivideo.mifm.data.models.jsondata.common.CommonVideo
import com.mivideo.mifm.network.service.VideoService
import rx.Observable

class VideoRepository(videoService: VideoService,
                      val mContext: Context) : VideoService by videoService {
//    /**
//     * 拉取视频详情内容,详情页接口wrapper方法
//     */
//    fun loadVideoDetailNetwork(videoId: String): Observable<CommonVideo?> {
//        return getVideoDetailInfo(videoId, 0, 0)
//                .map { videoInfo ->
//                    if (videoInfo.data != null && videoInfo.data!!.video != null) {
//                        videoInfo.data!!.video
//                    } else {
//                        null
//                    }
//                }
//    }
//
//    fun getLastPosition(videoId: String): Observable<Int> {
//        return Observable.create { subscriber ->
//            DbHistory.getQuery()
//                    .whereEqualTo(DbHistory.VIDEO_ID, videoId)
//                    .findInBackground { objects, e ->
//                        if (e == null && objects != null && objects.size > 0) {
//                            subscriber.onNext(objects[0].getLastPosition())
//                        } else {
//                            subscriber.onNext(0)
//                        }
//                        subscriber.onCompleted()
//                    }
//        }
//    }
//
//    fun saveHistory(video: CommonVideoHistory) {
//        DbHistory.getQuery()
//                .whereEqualTo(DbHistory.VIDEO_ID, video.getCommonVideo()!!.video_id)
//                .findInBackground { objects, e ->
//                    if (e == null) {
//                        if (objects == null || objects.size == 0) {
//                            DbHistory.getQuery().countInBackground { count, sabresException ->
//                                Log.d("HistoryCollection", "count = " + count)
//                                if (count > 99) {
//                                    DbHistory.getQuery().addDescendingOrder(DbHistory.LAST_UPDATE).findInBackground { obj, e ->
//                                        val c = count - 99
//                                        var i = 0
//                                        while (i < c) {
//                                            DbHistory.deleteHistory(obj[count.toInt() - ++i])
//                                        }
//                                        DbHistory.saveHistory(video)
//                                    }
//                                } else {
//                                    DbHistory.saveHistory(video)
//                                }
//                            }
//                        } else {
//                            DbHistory.updateHistory(video, objects[0])
//                        }
//                    }
//                }
//    }
}