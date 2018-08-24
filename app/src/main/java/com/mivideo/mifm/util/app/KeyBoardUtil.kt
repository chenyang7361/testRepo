package com.mivideo.mifm.util.app

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * Created by aaron on 16/10/9.
 * 软键盘操作工具类
 */
fun hideKeyboard(mContext: Activity) {
    val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (imm.isActive && mContext.currentFocus != null) {
        if (mContext.currentFocus.windowToken != null) {
            imm.hideSoftInputFromWindow(mContext.currentFocus.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}

/**
 * 显示软键盘
 */
fun showKeyboard(mContext: Activity, view: View) {
    val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)
}


/**
 * 判断当前键盘是否在展开状态
 */
fun isShowKeyboard(mContext: Activity): Boolean {
    val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (imm.isActive) {
        return true
    }

    return false
}


/**
 * 实现复制到剪贴板功能
 */
fun copyToClipboard(content: String, context: Context) {
    // 得到剪贴板管理器
    val cmb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cmb.primaryClip = ClipData.newPlainText(null, content)
}
