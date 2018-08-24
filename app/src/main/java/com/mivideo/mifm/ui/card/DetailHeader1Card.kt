package com.mivideo.mifm.ui.card

import android.content.Context
import android.graphics.Color
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.jsondata.MediaDetailData
import com.mivideo.mifm.events.CollectDetailEvent
import com.mivideo.mifm.events.ShareDetailEvent
import com.mivideo.mifm.events.UnCollectDetailEvent
import com.mivideo.mifm.util.app.postEvent
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

/**
 * Created by Jiwei Yuan on 18-7-30.
 */

class DetailHeader1Card(context: Context) : DetailHeaderCard(context) {

    private lateinit var title: TextView
    private lateinit var desc: TextView
    private lateinit var share: ImageView
    private lateinit var collect: ImageView
    private var collected: Boolean = false
    private lateinit var cover: ImageView

    override fun init() {
        rootViews = mInflater?.inflate(R.layout.detail_header1, null)
        title = rootViews!!.findViewById(R.id.header_title)
        desc = rootViews!!.findViewById(R.id.desc)
        share = rootViews!!.findViewById(R.id.share)
        collect = rootViews!!.findViewById(R.id.collect)
        cover = rootViews!!.findViewById(R.id.bg_header)
    }

    override fun setData(data: MediaDetailData) {
        title.text = data.title
        desc.text = data.updated_at
        share.setOnClickListener {
            postEvent(ShareDetailEvent(data))
        }

        collect.setOnClickListener {
            if (collected) {
                postEvent(UnCollectDetailEvent(data.id))
            } else {
                postEvent(CollectDetailEvent(data.id))
            }
        }

        Glide.with(mContext).load(data!!.cover)
                .placeholder(Color.GRAY)
                .crossFade(600)
                .priority(Priority.HIGH)
                .bitmapTransform(RoundedCornersTransformation(mContext, 5, 0))
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(cover)
    }

    override fun setCollected(isCollected: Boolean) {
        collected = isCollected
        if (isCollected) {
            collect.setImageResource(R.drawable.icon_collected)
        } else {
            collect.setImageResource(R.drawable.icon_to_collect)
        }
    }
}