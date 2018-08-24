package com.mivideo.mifm.ui.fragment.home

import android.support.v7.widget.RecyclerView

/**
 * Created by Jiwei Yuan on 18-7-24.
 */
interface IMediaListDelegate {

    fun refreshData()

    fun loadMore()

    fun getAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder>

    fun loadDataFromCache()
}