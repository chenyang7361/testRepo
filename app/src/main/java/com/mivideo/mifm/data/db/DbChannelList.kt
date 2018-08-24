package com.mivideo.mifm.data.db

import com.mivideo.mifm.data.models.jsondata.ChannelList
import com.mivideo.mifm.util.MJson
import com.sabres.SabresObject
import com.sabres.SabresQuery

/**
 * Created by Jiwei Yuan on 18-7-24.
 */
class DbChannelList : SabresObject() {
    companion object {
        val CHANNEL_LIST: String = "channel_list"
        val CHANNEL_INFO: String = "channel_info"
        val TAB_ID: String = "tab_id"
        /**
         * 获取查询对象
         */
        fun getQuery(): SabresQuery<DbChannelList> {
            return SabresQuery.getQuery(DbChannelList::class.java)
        }

        fun saveChannelData(tabId: String, data: ChannelList) {
            val dbChannel = DbChannelList()
            dbChannel.setChannelData(MJson.getInstance().toGson(data))
            dbChannel.setTabId(tabId)
            dbChannel.saveInBackground()
        }

        fun deleteChannelData(dbChannel: DbChannelList) {
            dbChannel.deleteInBackground()
        }
    }

    fun getChannelData(): String {
        return getString(CHANNEL_INFO)
    }

    fun setChannelData(info: String) {
        put(CHANNEL_INFO, info)
    }

    fun getTabId(): String {
        return getString(TAB_ID)
    }

    fun setTabId(tabId: String) {
        put(TAB_ID, tabId)
    }
}