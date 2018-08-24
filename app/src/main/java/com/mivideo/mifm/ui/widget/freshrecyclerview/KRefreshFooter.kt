package com.mivideo.mifm.ui.widget.freshrecyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.mivideo.mifm.R
import com.scwang.smartrefresh.layout.api.RefreshFooter
import com.scwang.smartrefresh.layout.api.RefreshKernel
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.constant.SpinnerStyle

/**
 * Created by aaron on 2017/12/25.
 */
class KRefreshFooter : LinearLayout, RefreshFooter {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private var completeViewVisibility: Boolean = true
    private lateinit var rootViews: View
    private lateinit var loadingView: LinearLayout
    private lateinit var completeView: LinearLayout

    private fun init() {
        rootViews = LayoutInflater.from(context).inflate(R.layout.refresh_default_footer, this, true)
        loadingView = rootViews.findViewById(R.id.loadingLayout)
        completeView = rootViews.findViewById(R.id.completeLayout)
        loadingView.visibility = View.VISIBLE
        completeView.visibility = View.GONE
    }

    fun showCompleteView(visibility: Boolean) {
        completeViewVisibility = visibility
    }

    override fun getView(): View {
        return this
    }

    override fun onLoadmoreReleased(layout: RefreshLayout?, footerHeight: Int, extendHeight: Int) {
    }

    override fun onPullReleasing(percent: Float, offset: Int, footerHeight: Int, extendHeight: Int) {
    }

    override fun onStateChanged(refreshLayout: RefreshLayout?, oldState: RefreshState?, newState: RefreshState?) {
    }

    override fun onPullingUp(percent: Float, offset: Int, footerHeight: Int, extendHeight: Int) {
    }

    override fun setLoadmoreFinished(finished: Boolean): Boolean {
        if (finished) {
            loadingView.visibility = View.GONE
            if (completeViewVisibility) {
                completeView.visibility = View.VISIBLE
            }
        } else {
            loadingView.visibility = View.VISIBLE
            if (completeViewVisibility) {
                completeView.visibility = View.GONE
            }
        }
        return true
    }

    fun clearData() {
        loadingView.visibility = View.GONE
        completeView.visibility = View.GONE
    }

    override fun getSpinnerStyle(): SpinnerStyle {
        return SpinnerStyle.Translate
    }

    override fun onFinish(layout: RefreshLayout?, success: Boolean): Int {
        return 0
    }

    override fun isSupportHorizontalDrag(): Boolean {
        return false
    }

    override fun setPrimaryColors(vararg colors: Int) {
    }

    override fun onHorizontalDrag(percentX: Float, offsetX: Int, offsetMax: Int) {
    }

    override fun onStartAnimator(layout: RefreshLayout?, height: Int, extendHeight: Int) {
    }

    override fun onInitialized(kernel: RefreshKernel?, height: Int, extendHeight: Int) {
    }
}
