package com.mivideo.mifm.ui.adapter.headline

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates2.AdapterDelegate
import com.mivideo.mifm.data.models.jsondata.ChannelItem
import com.mivideo.mifm.data.models.jsondata.PassageItem
import com.mivideo.mifm.ui.card.HeadlineItemCard

/**
 * Created by Jiwei Yuan on 18-8-8.
 */

class HeadlineItemDelegate : AdapterDelegate<List<PassageItem>> {

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return HeadLineViewHolder(HeadlineItemCard(parent!!.context))
    }


    override fun isForViewType(items: List<PassageItem>, position: Int): Boolean {
        return items[position].headline == 0
    }


    override fun onBindViewHolder(items: List<PassageItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val headlineHolder = holder as HeadLineViewHolder
        headlineHolder.item.setData(items[position])
    }

}

class HeadLineViewHolder(val item: HeadlineItemCard) : RecyclerView.ViewHolder(item.getView())