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
 * Created by Jiwei Yuan on 18-7-31.
 */
class DetailHeader2Card(context: Context) : DetailHeaderCard(context) {
    private lateinit var title: TextView
    private lateinit var time: TextView
    private lateinit var author: TextView
    private lateinit var category: TextView
    private lateinit var cover: ImageView
    private lateinit var share: ImageView
    private lateinit var collect: ImageView
    private var collected: Boolean = false
    override fun init() {
        rootViews = mInflater?.inflate(R.layout.detail_header2, null)
        title = rootViews!!.findViewById(R.id.header_title)
        time = rootViews!!.findViewById(R.id.time)
        author = rootViews!!.findViewById(R.id.author)
        category = rootViews!!.findViewById(R.id.category)
        cover = rootViews!!.findViewById(R.id.bg_header)
        share = rootViews!!.findViewById(R.id.share)
        collect = rootViews!!.findViewById(R.id.collect)
    }

    override fun setData(data: MediaDetailData) {
        title.text = data.title
        time.text = mContext?.resources?.getString(R.string.detail_update_format, data.updated_at)
        author.text = mContext?.resources?.getString(R.string.detail_author_format, data.author)
        category.text = mContext?.resources?.getString(R.string.detail_category_format, data.author)
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