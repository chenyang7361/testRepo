package com.kuaiest.video.ui.adapter.searchlist.result

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates2.AdapterDelegate
import com.mivideo.mifm.data.models.jsondata.ChannelItem
import com.mivideo.mifm.ui.card.EmptyCard

/**
 * 搜索结果为空时搜索结果列表item显示
 * @author LiYan
 */
class NewSearchEmptyDelegate : AdapterDelegate<ChannelItem> {
    override fun onBindViewHolder(items: ChannelItem, position: Int, holder: RecyclerView.ViewHolder) {

    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return EmptyItemHolder(EmptyCard(parent.context))
    }

    override fun isForViewType(items: ChannelItem, position: Int): Boolean {
        return items.clientStyle == 1
    }
}

class EmptyItemHolder(val item: EmptyCard) : RecyclerView.ViewHolder(item.getView())