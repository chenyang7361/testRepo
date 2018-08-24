package com.mivideo.mifm.ui.card

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.TextView
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.jsondata.ChannelItem
import com.mivideo.mifm.ui.adapter.KRefreshDelegateAdapter
import com.mivideo.mifm.ui.adapter.detail.DetailRecommendItemDelegate

/**
 * Created by Jiwei Yuan on 18-7-30.
 */
class DetailRecommendCard(context: Context) : MCard(context) {
    lateinit var adapter: KRefreshDelegateAdapter<ChannelItem>
    lateinit var container: RecyclerView
    lateinit var channelTitle: TextView
    override fun init() {
        rootViews = mInflater?.inflate(R.layout.detail_recommend, null)
        container = rootViews!!.findViewById<RecyclerView>(R.id.list)
        container.setHasFixedSize(true);
        container.isNestedScrollingEnabled = false
        channelTitle = rootViews!!.findViewById(R.id.channel_title)
        channelTitle.text = "相关推荐"
        val layoutMgr = GridLayoutManager(mContext, 3)
        layoutMgr.isAutoMeasureEnabled = true
        container.layoutManager = layoutMgr
        adapter = KRefreshDelegateAdapter()
        container.adapter = adapter
        adapter.mDelegatesManager.addDelegate(DetailRecommendItemDelegate())
    }

    fun addData(data: ArrayList<ChannelItem>) {
        adapter.dataList.clear()
        adapter.addDefaultDataList(data)
    }
}