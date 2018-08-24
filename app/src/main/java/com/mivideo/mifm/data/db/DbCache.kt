package com.mivideo.mifm.data.db

import android.os.SystemClock
import com.mivideo.mifm.data.models.jsondata.common.CommonVideoCache
import com.mivideo.mifm.download.support.DownloadState
import com.mivideo.mifm.util.MJson
import com.sabres.SabresObject
import com.sabres.SabresQuery
import java.util.ArrayList

/**
 * 专辑
 */
class DbCache : SabresObject() {
    companion object {
        val ALBUM_ID = "album_id"
        val ALBUM_INFO = "album_info"

        fun saveCache(audio: CommonVideoCache) {
            if (audio != null) {
                val albumInfo = audio.getAudioInfo()!!.albumInfo
                val dbCache = DbCache()
                dbCache.setAlbumId(albumInfo.id)
                dbCache.setAlbumInfo(MJson.getInstance().toGson(albumInfo))
                dbCache.saveInBackground()
            }
        }

        fun deleteCache(dbCache: DbCache) {
            dbCache.deleteInBackground()
        }

        fun getQuery(): SabresQuery<DbCache> {
            return SabresQuery.getQuery(DbCache::class.java)
        }
    }

    fun deleteCache(dbCache: DbCache) {
        dbCache.deleteInBackground()
    }

    fun getAlbumId(): String {
        return getString(ALBUM_ID)
    }

    fun setAlbumId(albumId: String) {
        put(ALBUM_ID, albumId)
    }

    fun getAlbumInfo(): String {
        return getString(ALBUM_INFO)
    }

    fun setAlbumInfo(albumInfo: String) {
        put(ALBUM_INFO, albumInfo)
    }
}

/**
 * 音频缓存对象
 */
class DbAudioCache : SabresObject() {
    companion object {
        val AUDIO_ID = "audio_id"
        val ALBUM_ID = "album_id"

        val KEY = "key"
        val URL = "url"
        val PATH = "path"
        val PROGRESS = "progress"
        val COMPLETE_SIZE = "csize"
        val TOTAL_SIZE = "tsize"
        val STATE = "state"

        val LAST_UPDATE = "last_update"
        val AUDIO_INFO = "audio_info"
        val AUTO_START = "auto"
        val CLICKED = "clk"
        val FAIL_REASON = "fail_reason"
        var ERROR_CODE = "error_code"

        fun saveCacheList(audioList: ArrayList<CommonVideoCache>) {
            if (audioList != null && audioList.size > 0) {
                val dbList = ArrayList<DbAudioCache>()
                for (audio in audioList) {
                    val audioInfo = audio.getAudioInfo()!!
                    val dbAudioList = DbAudioCache()
                    dbAudioList.setAudioId(audioInfo.passageItem.id)
                    dbAudioList.setAlbumId(audioInfo.albumInfo.id)
                    dbAudioList.setKey(audio.getKey())
                    dbAudioList.setUrl(audio.getUrl())
                    dbAudioList.setPath(audio.getPath())
                    dbAudioList.setProgress(audio.getProgress())
                    dbAudioList.setCompleteSize(audio.getCompleteSize())
                    dbAudioList.setTotalSize(audio.getTotalSize())
                    dbAudioList.setState(audio.getState())
                    dbAudioList.setAudioInfo(MJson.getInstance().toGson(audioInfo))
                    dbAudioList.setAutoStart(audio.getAutoStart())
                    dbAudioList.setClicked(audio.getClicked())
                    dbAudioList.setFailReason(audio.getFailReason())
                    dbAudioList.setErrorCode(audio.getErrorCode())
                    dbAudioList.setLastUpdate(SystemClock.elapsedRealtime())
                    dbList.add(dbAudioList)
                }
                saveAllInBackground(dbList)
            }
        }

        fun saveCache(audio: CommonVideoCache) {
            if (audio != null) {
                val audioInfo = audio.getAudioInfo()!!
                val dbAudioList = DbAudioCache()

                dbAudioList.setKey(audio.getKey())
                dbAudioList.setAudioId(audioInfo.passageItem.id)
                dbAudioList.setAlbumId(audioInfo.albumInfo.id)
                dbAudioList.setUrl(audio.getUrl())
                dbAudioList.setPath(audio.getPath())
                dbAudioList.setProgress(audio.getProgress())
                dbAudioList.setCompleteSize(audio.getCompleteSize())
                dbAudioList.setTotalSize(audio.getTotalSize())
                dbAudioList.setState(audio.getState())
                dbAudioList.setAudioInfo(MJson.getInstance().toGson(audioInfo))
                dbAudioList.setAutoStart(audio.getAutoStart())
                dbAudioList.setClicked(audio.getClicked())
                dbAudioList.setFailReason(audio.getFailReason())
                dbAudioList.setErrorCode(audio.getErrorCode())
                dbAudioList.setLastUpdate(System.currentTimeMillis())
                dbAudioList.saveInBackground()
            }
        }

        fun updateCache(video: CommonVideoCache, dbAudioCache: DbAudioCache) {
            updateCache(video, dbAudioCache, false)
        }

        fun updateCache(video: CommonVideoCache, dbAudioCache: DbAudioCache, updateTime: Boolean) {
            dbAudioCache.setUrl(video.getUrl())
            dbAudioCache.setPath(video.getPath())
            dbAudioCache.setProgress(video.getProgress())
            dbAudioCache.setCompleteSize(video.getCompleteSize())
            dbAudioCache.setTotalSize(video.getTotalSize())
            dbAudioCache.setState(video.getState())
            if (updateTime) {
                dbAudioCache.setLastUpdate(System.currentTimeMillis())
            }
            dbAudioCache.setAutoStart(video.getAutoStart())
            dbAudioCache.setClicked(video.getClicked())
            dbAudioCache.setFailReason(video.getFailReason())
            dbAudioCache.setErrorCode(video.getErrorCode())
            dbAudioCache.saveInBackground()
        }

        fun deleteCache(dbAudioCache: DbAudioCache) {
            dbAudioCache.deleteInBackground()
        }

        /**
         * 获取查询对象
         */
        fun getQuery(): SabresQuery<DbAudioCache> {
            return SabresQuery.getQuery(DbAudioCache::class.java)
        }
    }

    fun getAudioId(): String {
        return getString(AUDIO_ID)
    }

    fun setAudioId(videoId: String) {
        put(AUDIO_ID, videoId)
    }

    fun getAlbumId(): String {
        return getString(DbCache.ALBUM_ID)
    }

    fun setAlbumId(albumId: String) {
        put(DbCache.ALBUM_ID, albumId)
    }

    fun getLastUpdate(): Long {
        return getLong(LAST_UPDATE)
    }

    fun setLastUpdate(lastUpdate: Long) {
        put(LAST_UPDATE, lastUpdate)
    }

    fun getKey(): String {
        return getString(KEY)
    }

    fun setKey(key: String) {
        put(KEY, key)
    }

    fun getUrl(): String? {
        return getString(URL)
    }

    fun setUrl(url: String?) {
        put(URL, url)
    }

    fun getPath(): String? {
        return getString(PATH)
    }

    fun setPath(path: String?) {
        put(PATH, path)
    }

    fun getProgress(): Int {
        return getInt(PROGRESS)
    }

    fun setProgress(position: Int) {
        put(PROGRESS, position)
    }

    fun getComleteSize(): Long {
        return getLong(COMPLETE_SIZE)
    }

    fun setCompleteSize(size: Long) {
        put(COMPLETE_SIZE, size)
    }

    fun getTotalSize(): Long {
        return getLong(TOTAL_SIZE)
    }

    fun setTotalSize(size: Long) {
        put(TOTAL_SIZE, size)
    }

    fun getState(): Int {
        return getInt(STATE)
    }

    fun setState(state: Int) {
        put(STATE, state)
    }

    fun getAudioInfo(): String {
        return getString(AUDIO_INFO)
    }

    fun setAudioInfo(audioInfo: String) {
        put(AUDIO_INFO, audioInfo)
    }

    fun getAutoStart(): Boolean {
        return "t" == getString(AUTO_START)
    }

    fun setAutoStart(auto: Boolean) {
        if (auto) {
            put(AUTO_START, "t")
        } else {
            put(AUTO_START, "f")
        }
    }

    fun getClicked(): Boolean {
        return "t" == getString(CLICKED)
    }

    fun setClicked(click: Boolean) {
        if (click) {
            put(CLICKED, "t")
        } else {
            put(CLICKED, "f")
        }
    }

    fun getFailReason(): String {
        return getString(FAIL_REASON)
    }

    fun setFailReason(reason: String) {
        put(FAIL_REASON, reason)
    }

    fun getErrorCode(): Int {
        return getInt(ERROR_CODE)
    }

    fun setErrorCode(code: Int) {
        put(ERROR_CODE, code)
    }
}