package com.mivideo.mifm.ui.widget

import android.content.Context
import android.support.v4.widget.NestedScrollView
import android.util.AttributeSet
import android.view.View

/**
 * 兼容SDK23之前监听ScrollView滑动
 * Created by Jiwei Yuan on 18-7-30.
 */

class BelowV23ScrollView : NestedScrollView {

    lateinit var listener: OnScrollChangeListener

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
    }

    interface OnScrollChangeListener {
        fun onScrollChange(v: View, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int)
    }
}