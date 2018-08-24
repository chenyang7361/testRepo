package com.mivideo.mifm.ui.adapter.homelist

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates2.AdapterDelegate
import com.mivideo.mifm.data.models.jsondata.ChannelItem
import com.mivideo.mifm.ui.card.RecommendStyle3ItemCard

/**
 * Created by Jiwei Yuan on 18-7-24.
 */
class RecommendStyle3ItemDelegate : AdapterDelegate<List<ChannelItem>> {

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return Recommend3ItemHolder(RecommendStyle3ItemCard(parent.context))
    }

    override fun isForViewType(items: List<ChannelItem>, position: Int): Boolean {
        return true
    }

    override fun onBindViewHolder(items: List<ChannelItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val recomHolder = holder as Recommend3ItemHolder
        recomHolder.item.setData(items[position])
    }

}

class Recommend3ItemHolder(val item: RecommendStyle3ItemCard) : RecyclerView.ViewHolder(item.getView())