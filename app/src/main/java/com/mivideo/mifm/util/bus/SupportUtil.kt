package com.mivideo.mifm.util.bus

import android.text.TextUtils
import android.view.View
import com.mivideo.mifm.util.app.DisplayUtil.statusBarHeight

/**
 * Created by aaron on 2016/12/27.
 * 评论次数，点赞次数，播放次数转化，当次数大于10000时，以万为单位显示
 */
fun getCountStrFromCount(count : Int) : String {
    if (count >= 10000) {
        return StringBuffer().append(Math.round(count / 1000f) / 10f).append("万").toString()
    } else {
        return count.toString()
    }
}

/**
 * cp来源
 */
fun getCpFromContent(cpName: String) : String {
    if (TextUtils.isEmpty(cpName)) {
        return "未知"
    }
    return cpName
}


/**
 * 主要用于获取某个View组件的位置
 */
fun getPositionY(view : View) : Int {
    val location = IntArray(2)
    view.getLocationOnScreen(location)
    val x = location[0]
    val y = location[1] - statusBarHeight

    return y
}
