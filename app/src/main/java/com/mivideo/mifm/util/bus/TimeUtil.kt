package com.mivideo.mifm.util.bus

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by aaron on 2016/11/16.
 * 时间格式工具类
 */
fun changeSecondTime2Str(duration : Int) : String {
    val hour = duration / 60 / 60
    val minute = duration / 60 % 60
    val second = duration % 60

    val sb = StringBuffer("")

    if (hour >= 10) {
        sb.append(hour).append(":")
    } else if (hour > 0 && hour < 10){
        sb.append("0").append(hour).append(":")
    }

    if (minute >= 10) {
        sb.append(minute).append(":")
    } else {
        sb.append("0").append(minute).append(":")
    }

    if (second >= 10) {
        sb.append(second)
    } else {
        sb.append("0").append(second)
    }

    return sb.toString()
}

fun changeMillisecondTime2Str(duration : Int) : String {
    return changeSecondTime2Str(duration / 1000)
}

/**
 * Created by aaron on 2016/11/10.
 * 获取传递的时间与当前时间的时间差
 */
fun diffTime(times : Long) : Long {
    val date1 = Date()

    return date1.time / 1000 - times
}

fun getYMD(times: Long) : String {
    val sdf = SimpleDateFormat("yyyy-MM-dd")
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = times
    return sdf.format(calendar.time)
}

/**
 * 获取输入时间的时间（按s）
 */
fun getTimeStrFromSecondTime(times : Long) : String {

    val sb = StringBuffer()
    // 15分钟之内 --> 刚刚
    if (times / (60 * 15) < 1) {
        return sb.append("刚刚").toString()
    }
    // 60分钟之内 --> x分钟前
    else if (times / (60 * 60) < 1) {
        return sb.append(times / 60).append("分钟前").toString()
    }
    // 24小时之内 --> x小时前
    else if (times / (60 * 60 * 24) < 1) {
        return sb.append(times / (60 * 60)).append("小时前").toString()
    }
    // 240小时之内 --> x天前
    else if (times / (60 * 60 * 240) < 1) {
        return sb.append(times / (60 * 60 * 24)).append("天前").toString()
    }
    // 12个月之内 --> x月x日
    else if (times / (60 * 60 * 24 * 365) < 1) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = (calendar.timeInMillis / 1000 - times) * 1000
        return sb.append(calendar.get(Calendar.MONTH) + 1).append("月").append(calendar.get(Calendar.DAY_OF_MONTH)).append("日").toString()
    }
    // 12个月之外 --> x年x月x日
    else {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = (calendar.timeInMillis / 1000 - times) * 1000
        return sb.append(calendar.get(Calendar.YEAR)).append("年").append(calendar.get(Calendar.MONTH) + 1).append("月").append(calendar.get(Calendar.DAY_OF_MONTH)).append("日").toString()
    }
}
