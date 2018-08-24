package com.mivideo.mifm.ui.card

import android.content.Context
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.mivideo.mifm.data.models.jsondata.MediaDetailData

/**
 * Created by Jiwei Yuan on 18-7-31.
 */
class DetailHeaderContainerCard(context: Context) : MCard(context) {

    private var card: DetailHeaderCard? = null
    override fun init() {
        rootViews = RelativeLayout(mContext)
    }

    fun setData(data: MediaDetailData) {
        val container = rootViews as ViewGroup
        card = DetailHeader2Card(mContext!!)//createHeaderCard(mContext!!, data.cid)
        container.removeAllViews()
        container.addView(card!!.getView())
        card!!.setData(data)
    }

    fun setCollected(isCollected: Boolean) {
        card?.setCollected(isCollected)
    }
}

fun createHeaderCard(context: Context, type: String): DetailHeaderCard {
    return when (type) {
        "1" ->
            DetailHeader1Card(context)

        "2" ->
            DetailHeader2Card(context)

        else ->
            DetailHeader1Card(context)//默认
    }
}