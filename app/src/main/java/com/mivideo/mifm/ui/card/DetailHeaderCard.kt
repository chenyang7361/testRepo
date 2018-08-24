package com.mivideo.mifm.ui.card

import android.content.Context
import com.mivideo.mifm.data.models.jsondata.MediaDetailData

/**
 * Created by Jiwei Yuan on 18-7-31.
 */
abstract class DetailHeaderCard(context: Context) : MCard(context) {
    abstract fun setData(data: MediaDetailData)
    abstract fun setCollected(isCollected: Boolean)
}