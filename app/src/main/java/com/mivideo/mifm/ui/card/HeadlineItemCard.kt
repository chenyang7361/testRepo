package com.mivideo.mifm.ui.card

import android.content.Context
import android.widget.TextView
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.jsondata.PassageItem

/**
 * Created by Jiwei Yuan on 18-8-8.
 */
class HeadlineItemCard(context: Context) : MCard(context) {
    private lateinit var title: TextView
    private lateinit var time: TextView

    override fun init() {
        rootViews = mInflater?.inflate(R.layout.item_headline, null)
        time = rootViews!!.findViewById(R.id.time)
        title = rootViews!!.findViewById(R.id.title)
    }

    fun setData(items: PassageItem) {
        time.text = items.duration
        title.text = items.name
    }
}