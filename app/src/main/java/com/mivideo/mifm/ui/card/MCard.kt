package com.mivideo.mifm.ui.card

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class MCard {

    var mContext: Context? = null
    var mInflater: LayoutInflater? = null
    var rootViews: View? = null

    constructor(context: Context) {
        initContext(context)
    }

    abstract fun init()


    fun initContext(context: Context) {
        this.mContext = context
        mInflater = LayoutInflater.from(mContext)

        init()
        rootViews!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun getView(): View {
        return rootViews!!
    }
}