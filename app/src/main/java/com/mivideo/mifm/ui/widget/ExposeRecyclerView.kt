package com.mivideo.mifm.ui.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Created by xingchang on 17/7/26.
 */
open class ExposeRecyclerView : RecyclerView {
    private val EXPOSE_TIME = 1000L
    var mExposeRunnable: Runnable? = null
    var mState = SCROLL_STATE_IDLE
    var mTopView: View? = null
    var mPaddingTop = -1
    var mFixedheight = -1
    var mShowingOnScreen = true
    var mShowingCallback: ShowingOnScreenCallback? = null

    private val mOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (recyclerView == null) return
            mState = newState
            if (newState == SCROLL_STATE_IDLE) {
                delayPost(recyclerView)
            } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                if (mExposeRunnable != null)
                    removeCallbacks(mExposeRunnable)
                mExposeRunnable = null
            }
        }
    }

    constructor(context: Context?) : super(context) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context,
                attrs: AttributeSet? = null,
                defStyle: Int = 0) : super(context, attrs, defStyle) {
    }

    private fun addOnScrollListener() {
        this.addOnScrollListener(mOnScrollListener)
    }

    private fun removeOnScrollListener() {
        this.removeOnScrollListener(mOnScrollListener)
    }

    private fun delayPost(recyclerView: RecyclerView) {
        if (mShowingCallback != null && !mShowingCallback!!.isShowing()) return

        if (mExposeRunnable != null)
            removeCallbacks(mExposeRunnable)

        mExposeRunnable = Runnable {

            if (mPaddingTop == -1 || mFixedheight == -1) {
                var location = IntArray(2)
                recyclerView.getLocationOnScreen(location)
//                val pl = location[0]
                val pt = location[1]
                val ph = recyclerView.height
//                val pw = recyclerView.width
                mPaddingTop = pt
                mFixedheight = ph
                if (mTopView != null) {
                    mTopView!!.getLocationOnScreen(location)
                    val pl = location[0]
                    val tt = location[1]
                    val th = mTopView!!.height
                    val pw = mTopView!!.width
                    mPaddingTop = tt + th
                    mFixedheight = ph - th
                }
            }
            // TODO:
//            recyclerView.forEachChild {
//                if (it.tag != null) {
//                    for (sub in (it.tag as StatsObject).cards) {
//                        if (isRealInVisible(mPaddingTop, mFixedheight, it)) {
//                            logViewExpose(context, (it!!.tag as StatsObject).tabUrl!!, sub, sub.stat_ext, true)
//                            if (sub.embed != null) {
//                                for (embed in sub.embed) {
//                                    logEmbedExpose(context, embed, true)
//                                }
//                            }
//                        }
//                    }
//                }
//            }
        }
        postDelayed(mExposeRunnable, EXPOSE_TIME)
    }

    fun changeVisible(visible: Boolean) {
        if (mShowingOnScreen != visible) {
            mShowingOnScreen = visible
            if (mShowingOnScreen) {
                handleExpose()
            } else {
                if (mExposeRunnable != null)
                    removeCallbacks(mExposeRunnable)
            }
        }
    }

    fun handleExpose() {
        if (mState == SCROLL_STATE_IDLE)
            delayPost(this)
    }

    fun setShowingOnScreenCallback(callback: ShowingOnScreenCallback) {
        mShowingCallback = callback
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        this.adapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                handleExpose()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                handleExpose()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                handleExpose()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                handleExpose()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                handleExpose()
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                handleExpose()
            }
        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addOnScrollListener()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeOnScrollListener()
    }

    fun setTopView(view: View) {
        mTopView = view
    }

    interface ShowingOnScreenCallback {
        fun isShowing(): Boolean
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
//        if (changed) {
        mFixedheight = -1
        mPaddingTop = -1
//        }
    }
}
