package com.mivideo.mifm.util

import android.util.Log
import android.view.View

/**
 * Created by xingchang on 16/12/1.
 */
private var screenHeight = -1
private var screenWidth = -1
private var location = IntArray(2)

private var exportRate = 1f

fun updateExportRate(rate: Float) {
    exportRate = rate
}

fun isRealInVisible(parent: View, view: View): Boolean {
    parent.getLocationOnScreen(location)
    val pl = location[0]
    val pt = location[1]
    val ph = parent.height
    val pw = parent.width
    return isRealInVisible(pt, ph, view)
}

fun isRealInVisible(parentTop: Int, parentHeight: Int, view: View): Boolean {
    if (screenHeight == -1) {
        screenWidth = view.context.resources.displayMetrics.widthPixels
        screenHeight = view.context.resources.displayMetrics.heightPixels
    }

    view.getLocationOnScreen(location)
    val left = location[0]
    val top = location[1]
    val height = view.height
    val width = view.width

//    Log.d("XXXXXXXXXXX", "left: " + left) // 0
//    Log.d("XXXXXXXXXXX", "top: " + top) // 186
//    Log.d("XXXXXXXXXXX", "height: " + height) // 738
//    Log.d("XXXXXXXXXXX", "width: " + width)  // 1080
//    Log.d("XXXXXXXXXXX", "exportRate: " + exportRate)

    // 根据卡片曝光比例计算扩展区域
    val expandArea = (1 - exportRate) * height
//    Log.d("XXXXXXXXXXX", "expandArea: " + expandArea) // 221.40001
//    Log.d("XXXXXXXXXXX", "parentTop: " + parentTop) // 426
//    Log.d("XXXXXXXXXXX", "parentHeight: " + parentHeight) // 1599
//    Log.d("XXXXXXXXXXX", "screenWidth: " + screenWidth) // 1080

//    if (((top > 0) && (top + height) < screenHeight) && ((left + width) <= screenWidth && left >= 0)) {
    if (((top >= (parentTop - expandArea) && (top + height) <= (parentTop + parentHeight) + expandArea))
            && ((left + width) <= screenWidth && left >= 0)) {
        return true
    }
//    if (((top > (-height / 2.0f)) && (top + height / 2.0f) < screenHeight) && (left < (screenWidth / 2) && left > -screenWidth / 2)) {
//        return true
//    }
    return false
}
