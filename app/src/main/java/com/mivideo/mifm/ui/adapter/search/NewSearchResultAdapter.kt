package com.mivideo.mifm.ui.adapter.search

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates2.AdapterDelegatesManager
import com.kuaiest.video.ui.adapter.searchlist.result.NewSearchEmptyDelegate
import com.kuaiest.video.ui.adapter.searchlist.result.NewSearchItemDelegate
import com.mivideo.mifm.data.models.jsondata.ChannelItem
import com.mivideo.mifm.ui.adapter.KRefreshListAdapter

/**
 * 搜索结果页列表适配器
 * （移除dataCollection使用新版KRefreshAdapter）
 * @author LiYan
 */
class NewSearchResultAdapter(val context: Context, val tabUrl: String)
    : KRefreshListAdapter<ChannelItem>() {
    private val mDelegatesManager: AdapterDelegatesManager<ChannelItem> = AdapterDelegatesManager()

    init {
        mDelegatesManager.addDelegate(141, NewSearchItemDelegate())
        mDelegatesManager.addDelegate(143, NewSearchEmptyDelegate())
    }

    fun setData(newData: List<ChannelItem>) {
        dataList.clear()
        dataList.addAll(newData)
        notifyDataSetChanged()
    }


    override fun getItemViewType(position: Int): Int {
        return mDelegatesManager.getItemViewType(dataList[position], position)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        return mDelegatesManager.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        mDelegatesManager.onBindViewHolder(dataList[position], position, holder)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}