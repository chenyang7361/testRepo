package com.mivideo.mifm.ui.card

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import com.mivideo.mifm.R

/**
 * Created by Jiwei Yuan on 18-7-26.
 */
class SubscribeTabCard(context: Context) : MCard(context) {
    lateinit var icon: ImageView
    lateinit var title: TextView
    lateinit var count: TextView
    override fun init() {
        rootViews = mInflater?.inflate(R.layout.subscribe_tab_card, null)
        icon = rootViews!!.findViewById(R.id.icon)
        title = rootViews!!.findViewById(R.id.title)
        count = rootViews!!.findViewById(R.id.count)
    }

    fun setInfo(tabId: Int) {
        icon.setImageResource(getIconById(tabId))
        title.setText(getTitleById(tabId))
    }

    private fun getTitleById(tabId: Int): Int {
        var res: Int = R.string.history
        when (tabId) {
            0 -> res = R.string.history
            1 -> res = R.string.favor
            2 -> res = R.string.download
            3 -> res = R.string.player
        }
        return res
    }

    private fun getIconById(tabId: Int): Int {
        var res = R.drawable.icon_tab_history
        when (tabId) {
            0 -> res = R.drawable.icon_tab_history
            1 -> res = R.drawable.icon_tab_favor
            2 -> res = R.drawable.icon_tab_download
            3 -> res = R.drawable.icon_tab_player
        }
        return res
    }

    fun setCount(count: Int) {
        this.count.text = count.toString()
    }

}