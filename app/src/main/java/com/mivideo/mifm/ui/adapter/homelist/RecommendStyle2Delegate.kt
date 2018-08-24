package com.mivideo.mifm.ui.adapter.homelist

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.hannesdorfmann.adapterdelegates2.AdapterDelegate
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.jsondata.RecommendData
import com.mivideo.mifm.ui.card.RecommendStyle2Card

/**
 * 推荐页纵向样式
 * Created by Jiwei Yuan on 18-7-23.
 */
class RecommendStyle2Delegate : AdapterDelegate<List<RecommendData>> {

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return Recommend2ViewHolder(RecommendStyle2Card(parent.context))
    }

    override fun isForViewType(items: List<RecommendData>, position: Int): Boolean {
        return items[position].stype == 2
    }

    override fun onBindViewHolder(items: List<RecommendData>, position: Int, holder: RecyclerView.ViewHolder) {
        val recomHolder = holder as Recommend2ViewHolder
        recomHolder.item.addData(items[position])
    }

}

class Recommend2ViewHolder(val item: RecommendStyle2Card) : RecyclerView.ViewHolder(item.getView()) {
    val more: TextView by lazy {
        itemView.findViewById<TextView>(R.id.more)
    }

    val title: TextView by lazy {
        itemView.findViewById<TextView>(R.id.channel_title)
    }

    val recycler: LinearLayout by lazy {
        itemView.findViewById<LinearLayout>(R.id.list)
    }
}