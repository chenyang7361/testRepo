package com.mivideo.mifm.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.mivideo.mifm.R

/**
 * Created by Jiwei Yuan on 18-6-14.
 * 网络异常，加载失败，正在加载，空白数据
 */

class LoadTipView : FrameLayout {

    val clickable: View by lazy {
        findViewById<View>(R.id.tipView)
    }

    val icon: ImageView by lazy {
        findViewById<ImageView>(R.id.ic_no_net)
    }

    val tip: TextView by lazy {
        findViewById<TextView>(R.id.tip_retry)
    }

    val blankHolder: View by lazy {
        findViewById<View>(R.id.blank)
    }

    val fail: View by lazy {
        findViewById<View>(R.id.rl_fail)
    }

    val emptyView: FrameLayout by lazy {
        findViewById<FrameLayout>(R.id.emptyView)
    }

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }


    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.view_load_fail, this)
        fail.visibility = View.GONE
        emptyView.visibility = View.GONE
        visibility = View.GONE
    }

    fun addTopPadding(paddingTop: Int) {
        val layoutParams = blankHolder.layoutParams
        layoutParams.height = paddingTop
        blankHolder.layoutParams = layoutParams
        blankHolder.visibility = View.VISIBLE
        invalidate()
    }

    fun setRetryListener(l: OnRetryLoadListener) {
        clickable.setOnClickListener {
            l.retryLoad()
        }
    }

    fun showNetUnconnected() {
        if (fail.visibility == View.VISIBLE) {
            Toast.makeText(context, R.string.load_data_error, Toast.LENGTH_SHORT).show()
            return
        }

        icon.setImageResource(R.drawable.ic_no_net)
        tip.setText(R.string.tip_no_net)
        visibility = View.VISIBLE
        fail.visibility = View.VISIBLE
        invalidate()
    }

    fun showLoadFail() {
        if (fail.visibility == View.VISIBLE) {
            Toast.makeText(context, R.string.load_data_error, Toast.LENGTH_SHORT).show()
            return
        }
        icon.setImageResource(R.drawable.icon_load_error)
        tip.setText(R.string.load_data_error)
        visibility = View.VISIBLE
        fail.visibility = View.VISIBLE
        invalidate()
    }

    fun showProgress() {
        //TODO
    }

    fun hideTipView() {
        visibility = View.GONE
        invalidate()
    }

    fun showEmpty() {
        emptyView.visibility = View.VISIBLE
        visibility = View.VISIBLE
        invalidate()
    }

    fun hideEmpty() {
        emptyView.visibility = View.GONE
        if (fail.visibility == View.GONE) {
            visibility = View.GONE
        }
        invalidate()
    }


    fun addEmpty(empty: View) {
        if (empty.parent != null) {
            (empty.parent as ViewGroup).removeView(empty)
        }
        this.emptyView.addView(empty)
    }


    /**
     * 点击重试回调
     */
    interface OnRetryLoadListener {
        /**
         * 重试加载数据
         */
        fun retryLoad()
    }

}
