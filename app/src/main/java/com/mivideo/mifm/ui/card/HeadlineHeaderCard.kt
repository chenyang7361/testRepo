package com.mivideo.mifm.ui.card

import android.content.Context
import android.view.LayoutInflater
import com.mivideo.mifm.R

/**
 * Created by Jiwei Yuan on 18-8-8.
 */
class HeadlineHeaderCard(context: Context) : MCard(context) {
    override fun init() {
        rootViews = LayoutInflater.from(mContext).inflate(R.layout.headline_header, null)
    }

}