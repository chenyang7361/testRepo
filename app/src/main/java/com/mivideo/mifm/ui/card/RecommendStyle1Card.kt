package com.mivideo.mifm.ui.card

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.jsondata.ChannelItem
import com.mivideo.mifm.data.models.jsondata.RecommendData
import com.mivideo.mifm.events.HeadLineRecommendClickEvent
import com.mivideo.mifm.ui.adapter.KRefreshDelegateAdapter
import com.mivideo.mifm.ui.adapter.homelist.RecommendStyle1ItemDelegate
import com.mivideo.mifm.util.app.postEvent
import org.jetbrains.anko.onClick

/**
 * Created by Jiwei Yuan on 18-7-23.
 */

class RecommendStyle1Card(context: Context) : MCard(context) {
    lateinit var adapter: KRefreshDelegateAdapter<ChannelItem>
    lateinit var container: RecyclerView
    override fun init() {
        rootViews = mInflater?.inflate(R.layout.recommend_card, null)
        container = rootViews!!.findViewById<RecyclerView>(R.id.list)
        val layoutMgr = LinearLayoutManager(mContext)
        layoutMgr.orientation = LinearLayoutManager.VERTICAL
        container.layoutManager = layoutMgr
        adapter = KRefreshDelegateAdapter()
        container.adapter = adapter

    }

    fun addData(data: RecommendData) {
        adapter.mDelegatesManager.addDelegate(RecommendStyle1ItemDelegate(data.atype))
        adapter.dataList.clear()
        adapter.addDefaultDataList(data.list!!)
    }
}