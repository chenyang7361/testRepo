package com.mivideo.mifm.util.app

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.support.design.widget.Snackbar
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.mivideo.mifm.R
import org.jetbrains.anko.onClick

var lastShowToast: Long = 0

/**
 * 显示系统Toast,屏蔽多次连续显示
 */
fun showToast(mContext: Context, message: String) {
    Handler(Looper.getMainLooper()).post {
        if (SystemClock.elapsedRealtime() - lastShowToast < 900) {
            return@post
        }
        lastShowToast = SystemClock.elapsedRealtime()
        val makeText = Toast.makeText(mContext, message, Toast.LENGTH_SHORT)
        val tv = makeText.view.findViewById<TextView>(android.R.id.message)
        tv.gravity = Gravity.CENTER
        makeText.show()
    }
}

fun showSnake(view: View, title: String, message: String) {
    val snakeBar = Snackbar.make(view as View, message, 10 * 1000)
    val rootView = snakeBar.view as ViewGroup
    val contentVew = LayoutInflater.from(view.context).inflate(R.layout.custom_snakebar_layout, null, false)
    (contentVew.findViewById<TextView>(R.id.snakeTitle)).text = title
    (contentVew.findViewById<TextView>(R.id.snakeContent)).text = message
    (contentVew.findViewById<ImageView>(R.id.snakeClose)).onClick {
        snakeBar.dismiss()
    }
    rootView.addView(contentVew)
    rootView.setBackgroundColor(view.context.resources.getColor(R.color.themeColor))
    snakeBar.show()
}

fun showSnake(view: ViewGroup, title: String, message: String) {
    showSnake(view as View, title, message)
}
