package com.mivideo.mifm.ui.card

import android.content.Context
import android.widget.TextView
import com.mivideo.mifm.R

/**
 * Created by Jiwei Yuan on 18-7-30.
 */

class DetailDescribeCard(context: Context) : MCard(context) {

    private lateinit var tvDesc: TextView
    private lateinit var channelTitle: TextView

    override fun init() {
        rootViews = mInflater?.inflate(R.layout.detail_describe, null)
        tvDesc = rootViews!!.findViewById(R.id.tvDesc)
        channelTitle = rootViews!!.findViewById(R.id.channel_title)
    }

    fun setData(desc: String) {
        tvDesc.text = desc
        channelTitle.text = mContext?.resources?.getString(R.string.describe)
    }
}