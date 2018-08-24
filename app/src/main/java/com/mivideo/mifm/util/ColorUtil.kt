package com.mivideo.mifm.util

import android.content.Context
import android.os.Build
import android.support.v4.content.ContextCompat

/**
 * Created by aaron on 2017/12/12.
 */
fun getColor(context: Context, id: Int): Int {

    if (Build.VERSION.SDK_INT >= 23) {
        return ContextCompat.getColor(context, id)
    } else {
        return context.resources.getColor(id)
    }

}
