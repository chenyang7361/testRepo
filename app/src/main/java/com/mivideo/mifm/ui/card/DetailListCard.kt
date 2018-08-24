package com.mivideo.mifm.ui.card

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.jsondata.MediaDetailData
import com.mivideo.mifm.data.models.jsondata.PassageItem
import com.mivideo.mifm.events.showMoreAlbumContentEvent
import com.mivideo.mifm.ui.adapter.KRefreshDelegateAdapter
import com.mivideo.mifm.ui.adapter.detail.PassageItemDelegate
import com.mivideo.mifm.util.app.postEvent
import org.jetbrains.anko.onClick

/**
 * Created by Jiwei Yuan on 18-7-30.
 */

class DetailListCard(context: Context) : MCard(context) {
    private lateinit var container: RecyclerView
    private lateinit var channelTitle: TextView
    private lateinit var more: RelativeLayout
    lateinit var adapter: KRefreshDelegateAdapter<PassageItem>
    private lateinit var itemDelegate: PassageItemDelegate
    override fun init() {
        rootViews = mInflater?.inflate(R.layout.detail_list, null)
        container = rootViews!!.findViewById<RecyclerView>(R.id.list)
        container.setHasFixedSize(true)
        container.isNestedScrollingEnabled = false
        channelTitle = rootViews!!.findViewById(R.id.channel_title)
        more = rootViews!!.findViewById(R.id.more_content)
        channelTitle.text = mContext?.resources?.getString(R.string.catalog)
        val layoutMgr = LinearLayoutManager(mContext)
        layoutMgr.orientation = LinearLayoutManager.VERTICAL
        layoutMgr.isAutoMeasureEnabled = true
        container.layoutManager = layoutMgr
        adapter = KRefreshDelegateAdapter()
        container.adapter = adapter
        itemDelegate = PassageItemDelegate()
        adapter.mDelegatesManager.addDelegate(itemDelegate)
    }

    fun setData(data: MediaDetailData) {
        adapter.dataList.clear()
        itemDelegate.setCurrentAlbumInfo(data)
        adapter.addDefaultDataList(data.sections)
        if (data.has_next) {
            more.visibility = View.VISIBLE
            more.onClick {
                postEvent(showMoreAlbumContentEvent(data.id))
            }
        } else {
            more.visibility = View.GONE
        }
    }

    fun updata() {
        adapter.notifyDataSetChanged()
    }
}