package com.mivideo.mifm.ui.widget.freshrecyclerview

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.mivideo.mifm.R
import com.mivideo.mifm.ui.widget.LoadTipView
import com.mivideo.mifm.ui.widget.freshrecyclerview.OnCheckEmptyListener
import com.mivideo.mifm.ui.widget.freshrecyclerview.RefreshRecyclerView
import com.scwang.smartrefresh.layout.SmartRefreshLayout

/**
 * Created by aaron on 17/11/12.
 * 下拉刷新-加载更多控件可以再此做一些自定义操作
 */
class KRefreshLayout : SmartRefreshLayout {

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context,
                attrs: AttributeSet? = null,
                defStyle: Int = 0) : super(context, attrs, defStyle) {
        init()
    }

    override fun autoRefresh(): Boolean {
        return super.autoRefresh(0, 1f)
    }

    override fun finishLoadmore(): SmartRefreshLayout {
        post {
            isEnableAutoLoadmore = false
        }
        postDelayed({
            isEnableAutoLoadmore = true
        }, 100)
        return super.finishLoadmore(5)
    }

    override fun finishRefresh(): SmartRefreshLayout {
        resetNoMoreData()
        refreshRecyclerView.adapter.notifyDataSetChanged()
        return super.finishRefresh()
    }

    fun clearData() {
        footer.clearData()
    }

    private lateinit var header: KRefreshHeader
    private lateinit var footer: KRefreshFooter
    private lateinit var content: FrameLayout
    private lateinit var refreshRecyclerView: RefreshRecyclerView
    private lateinit var emptyView: LoadTipView

    private fun init() {
        header = KRefreshHeader(context)
        footer = KRefreshFooter(context)
        content = LayoutInflater.from(context).inflate(R.layout.refresh_recyclerview, null, false) as FrameLayout
        emptyView = LoadTipView(context)
        refreshRecyclerView = content.findViewById(R.id.refreshRecyclerView)
        this.addView(header)
        this.addView(content)
        this.addView(footer)
        content.addView(emptyView, android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT))

        setHeaderHeight(80f)
        setReboundDuration(300)
        setHeaderMaxDragRate(2f)
        setHeaderTriggerRate(1f)
        setFooterTriggerRate(1f)
        isEnableAutoLoadmore = true
        isEnableOverScrollBounce = false
        isEnableScrollContentWhenLoaded = true
        setEnableHeaderTranslationContent(true)
        setEnableFooterTranslationContent(true)
        setEnableLoadmoreWhenContentNotFull(false)
        setDisableContentWhenRefresh(true)
        mEnableFooterFollowWhenLoadFinished = true
        mEnableScrollContentWhenLoaded = true

        refreshRecyclerView.setCheckEmptyListener(object : OnCheckEmptyListener {
            override fun onCheckEmptyListener(isEmpty: Boolean) {
                if (isEmpty) {
                    emptyView.showEmpty()
                } else {
                    emptyView.hideEmpty()
                }
            }
        })
    }

    fun showCompleteView(visibility: Boolean) {
        footer.showCompleteView(visibility)
    }

    fun setNeedShowAd(show: Boolean) {
        header.setNeedShowAd(show)
    }

    fun setAdapter(refreshAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        this.refreshRecyclerView.adapter = refreshAdapter
    }

    fun setEView(emptyView: View) {
        this.emptyView.addEmpty(emptyView)
    }

    fun showHeader(show: Boolean) {
        if (show) {
            header.visibility = View.VISIBLE
        } else {
            header.visibility = View.GONE
        }
    }

    fun showFooter(show: Boolean) {
        if (show) {
            footer.visibility = View.VISIBLE
        } else {
            footer.visibility = View.GONE
        }
    }

    fun setRecyclerView(recyclerView: RefreshRecyclerView) {
        content.removeAllViews()
        content.addView(recyclerView)
        refreshRecyclerView = recyclerView
        refreshRecyclerView.setCheckEmptyListener(object : OnCheckEmptyListener {
            override fun onCheckEmptyListener(isEmpty: Boolean) {
                if (isEmpty) {
                    emptyView.showEmpty()
                } else {
                    emptyView.hideEmpty()
                }
            }
        })
        content.addView(emptyView, android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT))
    }

    fun getRecyclerView(): RefreshRecyclerView {
        return refreshRecyclerView
    }

    fun showEmpty(show: Boolean) {
        if (show) {
            emptyView.visibility = View.VISIBLE
        } else {
            emptyView.visibility = View.GONE
        }
        invalidate()
    }

    fun getEmptyView(): LoadTipView {
        return emptyView
    }

}
