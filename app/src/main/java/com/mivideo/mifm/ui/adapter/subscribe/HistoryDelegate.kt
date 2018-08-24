package com.mivideo.mifm.ui.adapter.subscribe

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates2.AdapterDelegate
import com.mivideo.mifm.data.models.jsondata.HistoryItem
import com.mivideo.mifm.ui.adapter.managedelete.Managable
import com.mivideo.mifm.ui.card.HistoryItemCard

/**
 * Created by Jiwei Yuan on 18-8-3.
 */
class HistoryDelegate : AdapterDelegate<List<Managable>> {

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return HistoryViewHolder(HistoryItemCard(parent!!.context))
    }

    override fun isForViewType(items: List<Managable>, position: Int): Boolean {
        return true
    }

    override fun onBindViewHolder(items: List<Managable>, position: Int, holder: RecyclerView.ViewHolder) {
        val historyHolder = holder as HistoryViewHolder
        historyHolder.item.setData(items[position] as HistoryItem)
    }

}

class HistoryViewHolder(val item: HistoryItemCard) : RecyclerView.ViewHolder(item.getView()) {

}