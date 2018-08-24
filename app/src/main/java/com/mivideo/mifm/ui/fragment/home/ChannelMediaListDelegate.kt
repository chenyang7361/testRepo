package com.mivideo.mifm.ui.fragment.home

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.jsondata.ChannelItem
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.ui.adapter.KRefreshDelegateAdapter
import com.mivideo.mifm.ui.adapter.homelist.RecommendStyle2ItemDelegate
import com.mivideo.mifm.ui.adapter.homelist.RecommendStyle3ItemDelegate
import com.mivideo.mifm.ui.widget.freshrecyclerview.RefreshRecyclerView
import com.mivideo.mifm.viewmodel.ChannelViewModel
import com.trello.rxlifecycle.FragmentEvent
import timber.log.Timber

class ChannelMediaListDelegate(val fragment: MediaListFragment, val tabId: String) : IMediaListDelegate {

    private var channelListAdapter = KRefreshDelegateAdapter<ChannelItem>()
    private var channelViewModel = ChannelViewModel(fragment.context, tabId)

    init {
        val recycler = LayoutInflater.from(fragment.context).inflate(R.layout.home_channel, null) as RefreshRecyclerView
        fragment.refreshLayout?.setRecyclerView(recycler)
    }

    override fun refreshData() {
        channelViewModel.refreshData()
                .compose(asyncSchedulers())
                .subscribe({
                    fragment.refreshLayout?.finishRefresh()
                    setListStyle(it.stype)
                    val data = it.data as ArrayList<ChannelItem>
                    Timber.i("init refresh get data from db size: ${data.size}")
                    channelListAdapter.dataList.clear()
                    if (data != null && !data.isEmpty()) {
                        channelListAdapter.addHeaderItems(data)
                        channelViewModel.saveRefreshDataToDb(tabId, it)
                        fragment.hideTipView()
                    } else {
                        fragment.showEmpty()
                    }
                    checkLoadMoreEnabled(it.has_next)
                }, {
                    fragment.refreshLayout?.finishRefresh()
                    fragment.showLoadFail(channelListAdapter.dataList.isEmpty())
                })
    }

    private fun checkLoadMoreEnabled(next: Boolean) {
        fragment.refreshLayout?.isEnableLoadmore = next
    }

    override fun loadMore() {
        channelViewModel.loadMoreData()
                .compose(asyncSchedulers())
                .subscribe({
                    val data = it.data as ArrayList<ChannelItem>
                    Timber.i("init refresh get data from db size: ${data.size}")
                    channelListAdapter.addFooterItems(data)
                    checkLoadMoreEnabled(it.has_next)
                    fragment.refreshLayout?.finishLoadmore()
                }, {
                    Timber.i("init refresh get data from db error")
                    fragment.refreshLayout?.finishLoadmore()

                })
    }

    override fun loadDataFromCache() {
        fragment.refreshLayout?.showHeader(false)
        Timber.i("init  refresh list from database")
        channelViewModel.loadRefreshDataFromDb()
                .compose(fragment.bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .compose(asyncSchedulers())
                .subscribe(
                        {
                            setListStyle(it.stype)
                            val data = it.data as ArrayList<ChannelItem>
                            Timber.i("init refresh get data from db size: ${data.size}")
                            if (!data.isEmpty()) {
                                channelListAdapter.addHeaderItems(data)
                            } else {
                                fragment.showNetUnconnected()
                            }
                            checkLoadMoreEnabled(it.has_next)
                        },
                        {
                            Timber.i("init refresh get data from db error")
                            fragment.showNetUnconnected()
                        }
                )
    }

    private fun setListStyle(stype: Int) {
        when (stype) {
//            1 -> {
//                val llm = LinearLayoutManager(fragment.context)
//                fragment.refreshLayout?.getRecyclerView()?.layoutManager = llm
//                channelListAdapter.mDelegatesManager
//                        .addDelegate(RecommendStyle1ItemDelegate(DataTypeDesc.NORMAL_ALBUM))
//            }
            2 -> {
                val llm = LinearLayoutManager(fragment.context)
                fragment.refreshLayout?.getRecyclerView()?.layoutManager = llm
                channelListAdapter.mDelegatesManager
                        .addDelegate(RecommendStyle2ItemDelegate())
            }

            3 -> {
                val glm = GridLayoutManager(fragment.context, 3)
                fragment.refreshLayout?.getRecyclerView()?.layoutManager = glm
                channelListAdapter.mDelegatesManager
                        .addDelegate(RecommendStyle3ItemDelegate())
            }
        }

    }

    override fun getAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder> {
        return channelListAdapter
    }

}

