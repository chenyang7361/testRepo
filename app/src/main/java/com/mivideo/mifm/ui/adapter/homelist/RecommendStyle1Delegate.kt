package com.mivideo.mifm.ui.adapter.homelist

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.hannesdorfmann.adapterdelegates2.AdapterDelegate
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.jsondata.RecommendData
import com.mivideo.mifm.ui.card.RecommendStyle1Card

/**
 * 推薦頁听头条样式
 * Created by Jiwei Yuan on 18-7-23.
 */
class RecommendStyle1Delegate : AdapterDelegate<List<RecommendData>> {

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return Recommend1ViewHolder(RecommendStyle1Card(parent.context))
    }

    override fun isForViewType(items: List<RecommendData>, position: Int): Boolean {
        return items[position].stype == 1
    }

    override fun onBindViewHolder(items: List<RecommendData>, position: Int, holder: RecyclerView.ViewHolder) {
        val recomHolder = holder as Recommend1ViewHolder
        recomHolder.item.addData(items[position])
    }
}

class Recommend1ViewHolder(val item: RecommendStyle1Card) : RecyclerView.ViewHolder(item.getView()) {
    val more: TextView by lazy {
        itemView.findViewById<TextView>(R.id.more)
    }

    val title: TextView by lazy {
        itemView.findViewById<TextView>(R.id.channel_title)
    }

    val recycler: RecyclerView by lazy {
        itemView.findViewById<RecyclerView>(R.id.list)
    }
}