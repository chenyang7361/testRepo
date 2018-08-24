package com.mivideo.mifm.ui.adapter.detail

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates2.AdapterDelegate
import com.mivideo.mifm.data.models.jsondata.ChannelItem
import com.mivideo.mifm.ui.card.DetailRecommendItemCard

/**
 * Created by Jiwei Yuan on 18-7-24.
 */
class DetailRecommendItemDelegate : AdapterDelegate<List<ChannelItem>> {

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return Recommend3ItemHolder(DetailRecommendItemCard(parent.context))
    }

    override fun isForViewType(items: List<ChannelItem>, position: Int): Boolean {
        return true
    }

    override fun onBindViewHolder(items: List<ChannelItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val recomHolder = holder as Recommend3ItemHolder
        recomHolder.item.setData(items[position])
    }

}

class Recommend3ItemHolder(val item: DetailRecommendItemCard) : RecyclerView.ViewHolder(item.getView())