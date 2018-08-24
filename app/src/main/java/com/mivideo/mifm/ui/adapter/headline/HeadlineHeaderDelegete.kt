package com.mivideo.mifm.ui.adapter.headline

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates2.AdapterDelegate
import com.mivideo.mifm.data.models.jsondata.ChannelItem
import com.mivideo.mifm.data.models.jsondata.PassageItem
import com.mivideo.mifm.ui.card.HeadlineHeaderCard

/**
 * Created by Jiwei Yuan on 18-8-8.
 */

class HeadlineHeaderDelegete : AdapterDelegate<List<PassageItem>> {

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return HeadlineHeaderViewholder(HeadlineHeaderCard(parent!!.context))
    }


    override fun isForViewType(items: List<PassageItem>, position: Int): Boolean {
        return items[position].headline == 1
    }

    override fun onBindViewHolder(items: List<PassageItem>, position: Int, holder: RecyclerView.ViewHolder) {

    }
}

class HeadlineHeaderViewholder(val item: HeadlineHeaderCard) : RecyclerView.ViewHolder (item.getView())
