package com.mivideo.mifm.data.db

import com.sabres.SabresObject
import com.sabres.SabresQuery

/**
 * Created by aaron on 2016/12/7.
 * 主要用于保存主页面的Tab信息
 */
class DbTabList : SabresObject() {
    companion object {
        val TAB_LIST : String = "tab_list"

        fun saveTabList(tabList: String) {
            val dbTabList = DbTabList()
            dbTabList.setTabList(tabList)

            dbTabList.saveInBackground()
        }

        /**
         * 获取查询对象
         */
        fun getQuery() : SabresQuery<DbTabList> {
            return SabresQuery.getQuery(DbTabList::class.java)
        }
    }


    fun setTabList(tabList: String) {
        put(TAB_LIST, tabList)
    }

    fun getTabList() : String {
        return getString(TAB_LIST)
    }
}
