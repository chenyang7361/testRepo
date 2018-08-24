package com.mivideo.mifm.cache

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.mivideo.mifm.BuildConfig
import com.mivideo.mifm.data.models.jsondata.common.CommonVideoCache
import com.mivideo.mifm.download.VideoDefinition
import com.mivideo.mifm.download.support.DownloadManager
import rx.Observable
import java.io.File

class VideoCacheUtils {
    companion object {

        internal var TAG = "VCT"

        /**
         * sync way
         */
        fun findVideoCachePath(context: Context, head: String?, definition: VideoDefinition?): String? {
            return findFileSeries(DownloadManager.get().getDownloadVideoCacheDir(context), head, definition)
        }

        fun hasVideoCacheExist(context: Context, head: String?): Boolean {
            var path = findVideoCachePath(context, head, VideoDefinition.DEFINITION_LOW)
            return !TextUtils.isEmpty(path)
        }

        /**
         * 取缓存策略：
         * 尽量取输入definition高级或同级缓存
         * 例如：
         * 输入definition为normal，那么优先级是super／high／normal（如果缓存文件存在，但不会取low）
         */
        private fun findFileSeries(dir: String?, head: String?, definition: VideoDefinition?): String? {
            var startTime = System.currentTimeMillis()
            if (dir == null || dir.length == 0) {
                return null
            }
            if (head == null || head.length == 0) {
                return null
            }
            if (definition == null) {
                return null
            }
            val dirFile = File(dir)
            if (!dirFile.exists() || !dirFile.isDirectory) {
                return null
            }
            var file: File? = null
            var filename: String? = null
            var i = 0
            do {
                val d = VideoDefinition.ordinal(i)
                if (d != null) {
                    filename = head + d.code()
                    file = File(dir, filename)
                    if (file.exists() && file.isFile) {
                        var cost = System.currentTimeMillis() - startTime
                        Log.d(TAG, "found cache cost " + cost)
                        return file.absolutePath
                    }
                }
                i++
            } while (i <= definition.ordinal)
            return null
        }

        fun deleteCacheFiles(context: Context?, key: String?) {
            if (context == null || key == null) {
                return
            }
            var videoCacheDirPath = DownloadManager.get().getDownloadVideoCacheDir(context.applicationContext)
            if (!TextUtils.isEmpty(videoCacheDirPath)) {
                var dirFile = File(videoCacheDirPath)
                if (dirFile.exists() && dirFile.isDirectory) {
                    val files = dirFile.listFiles { file, filename ->
                        if (filename.startsWith(key) || filename.startsWith("t_" + key)) {
                            true
                        } else false
                    }
                    for (file in files) {
                        Log.d(TAG, "deleteCacheFiles|" + file.absolutePath)
                        file.delete()
                    }
                }
            }
        }

        fun setVideoCacheClicked(context: Context?, key: String?) {
            if (context == null || key == null || key.length == 0) {
                return
            }
            DownloadManager.get().setVideoCacheClicked(context, key)
        }

        fun tryToReDownload(context: Context?, key: String?): Observable<CommonVideoCache> {
            if (context == null || key == null || key.length == 0) {
                return Observable.create(Observable.OnSubscribe<CommonVideoCache> { subscriber ->
                    subscriber.onNext(CommonVideoCache())
                    subscriber.onCompleted()
                })
            }
            VideoCacheUtils.deleteCacheFiles(context, key)
            DownloadManager.get().cancelTaskByKey(key)
            return DownloadManager.get().tryToReDownloadVideo(context, key)
        }
    }
}