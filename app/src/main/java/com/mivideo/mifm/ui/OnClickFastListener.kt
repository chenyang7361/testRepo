package com.mivideo.mifm.ui

import android.view.View
import com.mivideo.mifm.util.FastDoubleClickUtil

/**
 * Created by aaron on 2016/6/17.
 * View点击的时候判断屏蔽快速点击事件
 */
open class OnClickFastListener : View.OnClickListener {

    override fun onClick(v: View?) {
        // 判断当前点击事件与前一次点击事件时间间隔是否小于阙值
        if (FastDoubleClickUtil.isFastDoubleClick()) {
            return
        }
        onFastClick(v)
    }

    /**
     * 快速点击事件回调方法
     */
    open fun onFastClick(view: View?) {}
}

