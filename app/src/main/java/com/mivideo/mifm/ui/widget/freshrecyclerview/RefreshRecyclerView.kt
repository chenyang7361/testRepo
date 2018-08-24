package com.mivideo.mifm.ui.widget.freshrecyclerview

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import com.mivideo.mifm.ui.widget.ExposeRecyclerView

/**
 * Created by aaron on 2017/12/1.
 */
open class RefreshRecyclerView : ExposeRecyclerView {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        layoutManager = LinearLayoutManager(context)
    }

    private var onCheckEmptyListener: OnCheckEmptyListener? = null

    private val observer: RecyclerView.AdapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            checkIfEmpty()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            checkIfEmpty()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            checkIfEmpty()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            checkIfEmpty()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            checkIfEmpty()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            checkIfEmpty()
        }
    }

    override fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        val oldAdapter = getAdapter()
        oldAdapter?.unregisterAdapterDataObserver(observer)
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(observer)
    }

    fun setCheckEmptyListener(onCheckEmptyListener: OnCheckEmptyListener?) {
        this.onCheckEmptyListener = onCheckEmptyListener
    }

    fun checkIfEmpty() {
        if (adapter != null && adapter.itemCount == 0) {
            onCheckEmptyListener?.onCheckEmptyListener(true)
        } else {
            onCheckEmptyListener?.onCheckEmptyListener(false)
        }
    }
}

interface OnCheckEmptyListener {

    fun onCheckEmptyListener(isEmpty: Boolean)
}
