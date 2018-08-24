package com.mivideo.mifm.ui.card

import android.content.Context
import com.mivideo.mifm.R

/**
 * Created by aaron on 2016/11/11.
 * 视频列表Item Card
 */
class EmptyCard(context: Context) : MCard(context) {

    override fun init() {
        rootViews = mInflater?.inflate(R.layout.video_item_empty, null)
    }

}