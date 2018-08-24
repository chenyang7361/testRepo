package com.mivideo.mifm.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.mivideo.mifm.NetworkManager
import com.mivideo.mifm.R
import com.mivideo.mifm.ui.widget.LoadTipView
import com.mivideo.mifm.ui.widget.freshrecyclerview.KRefreshLayout

abstract class BaseRefreshListFragment : BaseFragment(), FragmentRefreshable {
    open var refreshLayout: KRefreshLayout? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preInitRefresh()
        initRefreshView(view)
        initRefreshTip()
        postInitRefresh()
        checkNetwork()
    }

    override fun preInitRefresh() {
    }

    override fun postInitRefresh() {
        refreshLayout?.setOnRefreshListener {
            refreshData()
        }

        refreshLayout?.setOnLoadmoreListener {
            loadMore()
        }
    }

    private fun initRefreshTip() {
        loadTip = refreshLayout?.getEmptyView()
        loadTip?.setRetryListener(object : LoadTipView.OnRetryLoadListener {
            override fun retryLoad() {
                refresh()
            }
        })
    }

    override fun loadMore() {
    }

    override fun refresh() {
    }

    override fun refreshData() {
    }

    fun showLoadFail(noData: Boolean) {
        if (noData) {
            showLoadFail()
        } else {
            Toast.makeText(context, R.string.load_data_error, Toast.LENGTH_SHORT).show()
        }
    }

    open fun checkNetwork() {
        if (NetworkManager.isNetworkUnConnected()) {
            showNetUnconnected()
        } else {
            refreshLayout?.autoRefresh()
        }
    }
}
