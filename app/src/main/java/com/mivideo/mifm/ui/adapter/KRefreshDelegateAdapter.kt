package com.mivideo.mifm.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates2.AdapterDelegatesManager

open class KRefreshDelegateAdapter<E> : KRefreshListAdapter<E>() {

    val mDelegatesManager: AdapterDelegatesManager<List<E>> = AdapterDelegatesManager()

    fun getDelegateManager(): AdapterDelegatesManager<List<E>> {
        return mDelegatesManager
    }

    /**
     * 添加默认数据
     */
    fun addDefaultDataList(dataList: ArrayList<E>) {
        if (isClear()) {
            addHeaderItems(dataList)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return mDelegatesManager.onCreateViewHolder(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        return mDelegatesManager.getItemViewType(dataList, position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        mDelegatesManager.onBindViewHolder(dataList, position, holder)
    }
}
