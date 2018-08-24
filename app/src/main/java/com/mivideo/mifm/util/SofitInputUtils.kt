package com.mivideo.mifm.util

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.view.inputmethod.InputMethodManager


object SoftInputUtils {

    /**
     * 显示软键盘，Dialog使用
     *
     * @param activity 当前Activity
     */
    fun showSoftInput(activity: Activity) {
        Handler().post {
            val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    /**
     * 隐藏软键盘
     *
     * @param activity 当前Activity
     */
    fun hideSoftInput(activity: Activity) {
        Handler().post {
            val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                    activity.window.decorView.windowToken, 0)
        }
    }
}
