package com.mivideo.mifm.ui.card

import android.content.Context
import com.mivideo.mifm.R
import org.jetbrains.anko.horizontalPadding
import org.jetbrains.anko.verticalPadding

/**
 * Created by Jiwei Yuan on 18-8-9.
 */
class SearchItemCard(context: Context) : RecommendStyle2ItemCard(context) {

    override fun init() {
        super.init()
        setSearchStyle()
    }


    private fun setSearchStyle() {
        author.setTextColor(mContext!!.resources.getColor(R.color.common_black))
        author.setBackgroundResource(R.drawable.shape_bg_search_author)
        author.horizontalPadding = 15
        author.verticalPadding = 8
    }
}

