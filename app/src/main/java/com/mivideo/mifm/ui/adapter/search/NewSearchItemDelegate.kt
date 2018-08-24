package com.kuaiest.video.ui.adapter.searchlist.result

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates2.AdapterDelegate
import com.mivideo.mifm.data.models.jsondata.ChannelItem
import com.mivideo.mifm.ui.card.SearchItemCard

/**
 * 搜索结果列表视频内容item显示
 * @author LiYan
 */
class NewSearchItemDelegate : AdapterDelegate<ChannelItem> {
    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return SearchItemHolder(SearchItemCard(parent.context))
    }

    override fun onBindViewHolder(items: ChannelItem, position: Int, holder: RecyclerView.ViewHolder) {
        (holder as SearchItemHolder).item.setData(items)
    }

    override fun isForViewType(items: ChannelItem, position: Int): Boolean {
        return items.clientStyle == 0
    }
}

class SearchItemHolder(val item: SearchItemCard) : RecyclerView.ViewHolder(item.getView())