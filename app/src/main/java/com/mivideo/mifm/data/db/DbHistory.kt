package com.mivideo.mifm.data.db

import com.mivideo.mifm.data.models.jsondata.AlbumInfo
import com.mivideo.mifm.data.models.jsondata.PassageItem
import com.mivideo.mifm.util.MJson
import com.sabres.SabresObject
import com.sabres.SabresQuery

/**
 * Created by xingchang on 16/12/14.
 */
class DbHistory : SabresObject() {
    companion object {

        const val ALBUM_ID = "album_id"
        const val ITEM_INFO = "item_info"
        const val ALBUM_INFO = "album_info"
        val LAST_UPDATE = "last_update"
        val LAST_POSITION = "last_position"
        val PAGE_NO = "page_no"
        fun saveHistory(album: AlbumInfo, item: PassageItem, position: Int, pageNo: Int) {
            if (album != null && item != null) {
                val dbHistory = DbHistory()
                dbHistory.setAlbumId(album.id)
                dbHistory.setLastPosition(position)
                dbHistory.setAlbumInfo(MJson.getInstance().toGson(album))
                dbHistory.setItemInfo(MJson.getInstance().toGson(item))
                dbHistory.setLastUpdate(System.currentTimeMillis())
                dbHistory.setPageNo(pageNo)
                dbHistory.saveInBackground()
            }
        }

        fun updateHistory(item: PassageItem, position: Int, dbHistory: DbHistory) {
            dbHistory.setItemInfo(MJson.getInstance().toGson(item))
            dbHistory.setLastPosition(position)
            dbHistory.setLastUpdate(System.currentTimeMillis())
            dbHistory.saveInBackground()
        }

        fun deleteHistory(dbHistory: DbHistory) {
            dbHistory.deleteInBackground()
        }

        /**
         * 获取查询对象
         */
        fun getQuery(): SabresQuery<DbHistory> {
            return SabresQuery.getQuery(DbHistory::class.java)
        }

        fun deleteAllHistory(data: List<DbHistory>) {
            deleteAllInBackground(data)
        }
    }

    private fun setPageNo(pageNo: Int) {
        put(PAGE_NO, pageNo)
    }

    fun getPageNo(): Int {
        return getInt(PAGE_NO)
    }


    fun getAlbumId(): String {
        return getString(ALBUM_ID)
    }

    fun setAlbumId(albumId: String) {
        put(ALBUM_ID, albumId)
    }

    fun getItemInfo(): String {
        return getString(ITEM_INFO)
    }

    fun setItemInfo(item: String) {
        put(ITEM_INFO, item)
    }

    fun getLastUpdate(): Long {
        return getLong(LAST_UPDATE)
    }

    fun setLastUpdate(lastUpdate: Long) {
        put(LAST_UPDATE, lastUpdate)
    }

    fun getLastPosition(): Int {
        return getInt(LAST_POSITION)
    }

    fun setLastPosition(position: Int) {
        put(LAST_POSITION, position)
    }

    fun getAlbumInfo(): String {
        return getString(ALBUM_INFO)
    }

    fun setAlbumInfo(info: String) {
        put(ALBUM_INFO, info)
    }
}