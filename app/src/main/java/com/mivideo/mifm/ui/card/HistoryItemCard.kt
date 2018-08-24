package com.mivideo.mifm.ui.card

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.jsondata.HistoryItem
import com.mivideo.mifm.events.HistoryItemCheckChangeEvent
import com.mivideo.mifm.events.PlayHistoryEvent
import com.mivideo.mifm.util.TimeUtil
import com.mivideo.mifm.util.app.postEvent
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

/**
 * Created by Jiwei Yuan on 18-8-3.
 */

class HistoryItemCard(context: Context) : MCard(context) {
    lateinit var update: TextView
    lateinit var title: TextView
    lateinit var time: TextView
    lateinit var icon: ImageView
    lateinit var checkBox: CheckBox

    override fun init() {
        rootViews = mInflater?.inflate(R.layout.history_item, null)
        update = rootViews!!.findViewById(R.id.author)
        title = rootViews!!.findViewById(R.id.title)
        time = rootViews!!.findViewById(R.id.desc)
        icon = rootViews!!.findViewById(R.id.icon)
        checkBox = rootViews!!.findViewById(R.id.check)
    }

    fun setData(item: HistoryItem) {
        title.text = item.album?.title
        update.text = item.item?.name
        time.text = TimeUtil.getDateString(item.lastUpdate)//.toString()
        Glide.with(mContext).load(item.album?.cover)
                .placeholder(Color.GRAY)
                .crossFade(600)
                .priority(Priority.HIGH)
                .bitmapTransform(RoundedCornersTransformation(mContext, 5, 0))
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(icon)
        rootViews!!.setOnClickListener {
            postEvent(PlayHistoryEvent(item))
        }
        if (item.manage) {
            checkBox.visibility = View.VISIBLE
        } else {
            checkBox.visibility = View.GONE
        }
        checkBox.isChecked = item.delete
        checkBox.setOnCheckedChangeListener { _, checked ->
            item.delete = checked
            postEvent(HistoryItemCheckChangeEvent(checked, if (item.album == null) "" else {
                item.album!!.id
            }))
        }
    }


}