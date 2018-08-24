package com.mivideo.mifm.ui.adapter.homelist

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates2.AdapterDelegate
import com.mivideo.mifm.data.models.jsondata.ChannelItem
import com.mivideo.mifm.ui.card.RecommendStyle2ItemCard

/**
 * Created by Jiwei Yuan on 18-7-24.
 */
class RecommendStyle2ItemDelegate : AdapterDelegate<List<ChannelItem>> {

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return Recommend2ItemHolder(RecommendStyle2ItemCard(parent.context))
    }


    override fun isForViewType(items: List<ChannelItem>, position: Int): Boolean {
        return true
    }

    override fun onBindViewHolder(items: List<ChannelItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val recomHolder = holder as Recommend2ItemHolder
        recomHolder.item.setData(items[position])
    }

}

class Recommend2ItemHolder(val item: RecommendStyle2ItemCard) : RecyclerView.ViewHolder(item.getView())