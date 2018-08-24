package com.mivideo.mifm.ui.card

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.jsondata.ChannelItem
import com.mivideo.mifm.data.models.jsondata.RecommendData
import com.mivideo.mifm.events.RecommendMoreEvent
import com.mivideo.mifm.ui.adapter.KRefreshDelegateAdapter
import com.mivideo.mifm.ui.adapter.homelist.RecommendStyle2ItemDelegate
import com.mivideo.mifm.util.app.postEvent
import org.jetbrains.anko.onClick

/**
 * Created by Jiwei Yuan on 18-7-23.
 */
class RecommendStyle2Card(context: Context) : MCard(context) {
    lateinit var adapter: KRefreshDelegateAdapter<ChannelItem>
    lateinit var container: RecyclerView
    lateinit var more: TextView
    lateinit var channelTitle: TextView
    override fun init() {
        rootViews = mInflater?.inflate(R.layout.recommend_card2, null)
        container = rootViews!!.findViewById<RecyclerView>(R.id.list)
        more = rootViews!!.findViewById(R.id.more)
        channelTitle = rootViews!!.findViewById(R.id.channel_title)
        val layoutMgr = LinearLayoutManager(mContext)
        layoutMgr.orientation = LinearLayoutManager.VERTICAL
        container.layoutManager = layoutMgr
        adapter = KRefreshDelegateAdapter()
        container.adapter = adapter
        adapter.mDelegatesManager.addDelegate(RecommendStyle2ItemDelegate())
    }

    fun addData(data: RecommendData) {
//        more.visibility = if (data.has_more == 0) View.GONE else View.VISIBLE
        channelTitle.text = data.name
        adapter.dataList.clear()
        adapter.addDefaultDataList(data.list!!)
        more.onClick {
//            postEvent(RecommendMoreEvent(data.has_more.toString()))
        }
    }
}