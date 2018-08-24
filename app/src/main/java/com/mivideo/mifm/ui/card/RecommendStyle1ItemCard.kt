package com.mivideo.mifm.ui.card

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.jsondata.ChannelItem
import com.mivideo.mifm.events.HeadLineRecommendClickEvent
import com.mivideo.mifm.util.ScreenUtil
import com.mivideo.mifm.util.app.postEvent
import org.jetbrains.anko.onClick
import org.jetbrains.anko.textColor

/**
 * Created by Jiwei Yuan on 18-7-23.
 */
class RecommendStyle1ItemCard(context: Context) : MCard(context) {

    lateinit var time: TextView
    lateinit var title: TextView
    lateinit var icon: ImageView

    override fun init() {
        rootViews = mInflater?.inflate(R.layout.item_recomm_style1, null)
        time = rootViews!!.findViewById(R.id.time)
        title = rootViews!!.findViewById(R.id.title)
        icon = rootViews!!.findViewById(R.id.icon)
    }

    fun setData(it: ChannelItem) {
        time.text = it.from_now
        title.text = it.title
    }

    fun setFirstStyle() {
        val color = mContext!!.resources.getColor(R.color.text_dark_black)
        time.textColor = color
        title.textColor = color
        time.textSize = ScreenUtil.px2dip(mContext, 30f).toFloat()
        title.textSize = ScreenUtil.px2dip(mContext, 48f).toFloat()
        icon.visibility = View.GONE
        title.paint.isFakeBoldText = true
    }

    fun setEntrance() {
        rootViews?.onClick {
            postEvent(HeadLineRecommendClickEvent())
        }
    }
}