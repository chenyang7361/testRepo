package com.mivideo.mifm.ui.fragment

import android.view.View

interface FragmentRefreshable {
    /**
     * 在完成RefreshLayout相应初始化后进行的操作
     */
    fun postInitRefresh()

    /**
     * 在RefreshLayout初始化之前进行的操作
     */
    fun preInitRefresh()

    /**
     * 初始化RefreshLayout,设置RecyclerView,adapter等
     */
    fun initRefreshView(view: View)

    /**
     * 点击无网络连接或加载失败的重试操作
     */
    fun refresh()

    /**
     * 上划加载更多的数据请求
     */
    fun loadMore()

    /**
     * 下拉刷新刷新数据请求
     */
    fun refreshData()
}
