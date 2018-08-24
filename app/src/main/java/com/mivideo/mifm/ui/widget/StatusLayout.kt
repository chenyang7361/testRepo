package com.mivideo.mifm.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.mivideo.mifm.R
import org.jetbrains.anko.onClick

/**
 * Created by aaron on 2018/6/7.
 */

class StatusLayout : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private lateinit var loadingView: LoadingView
    private lateinit var errorView: FrameLayout
    private lateinit var llRetry: LinearLayout
    private var statusLayoutListener: StatusLayoutListener?= null

    override fun onFinishInflate() {
        super.onFinishInflate()
        val rootViews = LayoutInflater.from(context).inflate(R.layout.status_layout, null, false)
        loadingView = rootViews.findViewById(R.id.loadingView)
        errorView = rootViews.findViewById(R.id.errorView)
        llRetry = errorView.findViewById(R.id.ll_retry)
        llRetry.onClick {
            statusLayoutListener?.onRetry()
        }

        this.addView(rootViews)
    }

    fun showLoadingView() {
        this.visibility = View.VISIBLE
        loadingView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
    }

    fun showErrorView() {
        this.visibility = View.VISIBLE
        loadingView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
    }

    fun showContentView() {
        this.visibility = View.GONE
    }

    fun setStatusLayoutListener(statusLayoutListener: StatusLayoutListener) {
        this.statusLayoutListener = statusLayoutListener
    }
}

interface StatusLayoutListener {

    fun onRetry()
}