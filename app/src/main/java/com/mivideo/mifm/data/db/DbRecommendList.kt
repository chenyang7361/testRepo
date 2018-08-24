package com.mivideo.mifm.data.db

import com.mivideo.mifm.data.models.jsondata.RecommendData
import com.mivideo.mifm.util.MJson
import com.sabres.SabresObject
import com.sabres.SabresQuery

/**
 * Created by Jiwei Yuan on 18-7-24.
 */

class DbRecommendList : SabresObject() {
    companion object {
        val RECOMMEND_LIST: String = "recommend_list"
        val RECOMMEND_INFO: String = "recom_info"
        /**
         * 获取查询对象
         */
        fun getQuery(): SabresQuery<DbRecommendList> {
            return SabresQuery.getQuery(DbRecommendList::class.java)
        }

        fun saveRecommendData(data: List<RecommendData>) {
            if (!data.isEmpty()) {
                val dbList = ArrayList<DbRecommendList>()
                data.forEach {
                    val dbRecommend = DbRecommendList()
                    dbRecommend.setRecommendData(MJson.getInstance().toGson(it))
                    dbList.add(dbRecommend)
                }
                saveAllInBackground(dbList)
            }
        }

        fun deleteRecommendData(dbRecommendList: DbRecommendList) {
            dbRecommendList.deleteInBackground()
        }
    }

    fun getRecommendData(): String {
        return getString(RECOMMEND_INFO)
    }

    fun setRecommendData(info: String) {
        put(RECOMMEND_INFO, info)
    }

}