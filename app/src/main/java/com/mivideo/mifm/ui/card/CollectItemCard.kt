package com.mivideo.mifm.ui.card

import android.content.Context
import android.graphics.Color
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.jsondata.CollectItem
import com.mivideo.mifm.events.StartDetailFragmentEvent
import com.mivideo.mifm.util.app.postEvent
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

/**
 * Created by Jiwei Yuan on 18-8-2.
 */
class CollectItemCard(context: Context) : MCard(context) {

    lateinit var title: TextView
    lateinit var author: TextView
    lateinit var icon: ImageView

    override fun init() {
        rootViews = mInflater?.inflate(R.layout.item_collect_layout, null)
        title = rootViews!!.findViewById(R.id.tv_collect_item_title)
        author = rootViews!!.findViewById(R.id.tv_collect_item_author)
        icon = rootViews!!.findViewById(R.id.iv_collect_item_icon)
    }

    fun setData(data: CollectItem) {
        title.text = data.title
        author.text = data.author
        Glide.with(mContext).load(data.cover)
                .placeholder(Color.GRAY)
                .crossFade(600)
                .priority(Priority.HIGH)
                .bitmapTransform(RoundedCornersTransformation(mContext, 5, 0))
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(icon)
        rootViews!!.setOnClickListener {
            postEvent(StartDetailFragmentEvent(data.id.toString()))
        }
    }
}