package com.mivideo.mifm.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Jiwei Yuan on 18-8-20.
 */
object TimeUtil {
    fun getDateString(time: Long): String {
        try {
            val date = Date(time)
            val formatter = SimpleDateFormat("yyyy-MM-dd")
            return formatter.format(date)
        } catch (e: Exception) {
            return ""
        }

    }
}