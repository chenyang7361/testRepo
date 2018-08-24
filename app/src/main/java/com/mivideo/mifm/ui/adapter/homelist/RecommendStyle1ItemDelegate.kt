package com.mivideo.mifm.ui.adapter.homelist

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates2.AdapterDelegate
import com.mivideo.mifm.data.models.jsondata.ChannelItem
import com.mivideo.mifm.data.models.jsondata.DataTypeDesc
import com.mivideo.mifm.ui.card.RecommendStyle1ItemCard

/**
 * Created by Jiwei Yuan on 18-7-24.
 */
class RecommendStyle1ItemDelegate(val atype: Int) : AdapterDelegate<List<ChannelItem>> {

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return Recommend1ItemHolder(RecommendStyle1ItemCard(parent.context))
    }


    override fun isForViewType(items: List<ChannelItem>, position: Int): Boolean {
        return true
    }

    override fun onBindViewHolder(items: List<ChannelItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val recomHolder = holder as Recommend1ItemHolder
        recomHolder.item.setData(items[position])
        if (atype == DataTypeDesc.HEADLINE_ALBUM) {
            if (position == 0) {
                recomHolder.item.setFirstStyle()
            }
            recomHolder.item.setEntrance()
        }
    }
}

class Recommend1ItemHolder(val item: RecommendStyle1ItemCard) : RecyclerView.ViewHolder(item.getView())