package com.mivideo.mifm.ui.card

import android.content.Context
import android.graphics.Color
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.jsondata.ChannelItem
import com.mivideo.mifm.events.StartDetailFragmentEvent
import com.mivideo.mifm.util.app.postEvent
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

open class RecommendStyle2ItemCard(mContext: Context) : MCard(mContext) {

    lateinit var author: TextView
    lateinit var title: TextView
    lateinit var desc: TextView
    lateinit var icon: ImageView

    override fun init() {
        rootViews = mInflater?.inflate(R.layout.item_recommend_style2, null)
        author = rootViews!!.findViewById(R.id.author)
        title = rootViews!!.findViewById(R.id.title)
        desc = rootViews!!.findViewById(R.id.desc)
        icon = rootViews!!.findViewById(R.id.icon)
    }

    fun setData(item: ChannelItem) {
        title.text = item.title
        author.text = item.author
        desc.text = item.desc
        Glide.with(mContext).load(item.url)
                .placeholder(Color.GRAY)
                .crossFade(600)
                .priority(Priority.HIGH)
                .bitmapTransform(RoundedCornersTransformation(mContext, 5, 0))
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(icon)
        rootViews!!.setOnClickListener {
            postEvent(StartDetailFragmentEvent(item.id.toString()))
        }
    }

}
