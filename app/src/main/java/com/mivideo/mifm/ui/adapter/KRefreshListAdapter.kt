package com.mivideo.mifm.ui.adapter

import android.support.v7.widget.RecyclerView
import java.util.*

abstract class KRefreshListAdapter<E> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val dataList: ArrayList<E> = ArrayList()

    override fun getItemCount(): Int {
        return dataList.size
    }

    open fun addHeaderItems(mList: ArrayList<E>) {
        dataList.clear()
        dataList.addAll(mList)
        notifyDataSetChanged()
    }

    open fun addHeaderItem(data: E) {
        val mList = ArrayList<E>()
        mList.add(data)
        mList.addAll(dataList)
        dataList.clear()
        dataList.addAll(mList)
        notifyItemInserted(0)
    }

    open fun addFooterItems(mList: ArrayList<E>) {
        val startPosition = dataList.size
        dataList.addAll(mList)
//        notifyItemRangeChanged(startPosition, mList.size)
      notifyItemRangeInserted(startPosition, mList.size)
    }

    open fun addFooterItem(data: E) {
        dataList.add(data)
        notifyItemInserted(dataList.size - 1)
    }

    open fun addItemAt(index: Int, data: E) {
        dataList.add(index, data)
        notifyItemInserted(index)
    }

    open fun removeItem(data: E) {
        if (dataList.remove(data)) {
            notifyDataSetChanged()
        }
    }

    open fun removeAt(index: Int) {
        if (index < dataList.size) {
            dataList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    open fun getAt(index: Int): E? {
        if (index < dataList.size) {
            return dataList[index]
        } else {
            return null
        }
    }

    open fun clearData() {
        dataList.clear()
        notifyDataSetChanged()
    }

    open fun isClear(): Boolean {
        return dataList.isEmpty()
    }

    open fun size(): Int {
        return dataList.size
    }
}