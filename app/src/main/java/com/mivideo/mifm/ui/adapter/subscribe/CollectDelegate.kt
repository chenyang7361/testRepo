package com.mivideo.mifm.ui.adapter.subscribe

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates2.AdapterDelegate
import com.mivideo.mifm.data.models.jsondata.CollectItem
import com.mivideo.mifm.ui.card.CollectItemCard

/**
 * Created by Jiwei Yuan on 18-8-2.
 */
class CollectDelegate : AdapterDelegate<List<CollectItem>> {

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return CollectViewHolder(CollectItemCard((parent!!.context)))
    }

    override fun isForViewType(items: List<CollectItem>, position: Int): Boolean {
        return true
    }

    override fun onBindViewHolder(items: List<CollectItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val collectHolder = holder as CollectViewHolder
        collectHolder.item.setData(items[position])
    }


}

class CollectViewHolder(val item: CollectItemCard) : RecyclerView.ViewHolder(item.getView()) {

}