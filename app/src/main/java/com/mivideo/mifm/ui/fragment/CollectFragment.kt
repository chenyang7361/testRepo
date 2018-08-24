package com.mivideo.mifm.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.jsondata.CollectItem
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.ui.adapter.KRefreshDelegateAdapter
import com.mivideo.mifm.ui.adapter.subscribe.CollectDelegate
import com.mivideo.mifm.viewmodel.CollectListViewModel
import com.trello.rxlifecycle.FragmentEvent
import kotlinx.android.synthetic.main.subscribe_item_title.*

/**
 * Created by Jiwei Yuan on 18-7-26.
 */

class CollectFragment : BaseRefreshListFragment() {

    private lateinit var collectListViewModel: CollectListViewModel
    private lateinit var collectAdapter: KRefreshDelegateAdapter<CollectItem>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_collect, container, false)
    }

    override fun initRefreshView(view: View) {
        collectListViewModel = CollectListViewModel(context)
        lifecycle.addObserver(collectListViewModel)
        refreshLayout = view.findViewById(R.id.collect_refreshLayout)

        channelTitle.text = resources.getString(R.string.my_collect)
        manageLayout.visibility = View.GONE
        collectAdapter = KRefreshDelegateAdapter()
        collectAdapter.mDelegatesManager.addDelegate(CollectDelegate())
        refreshLayout?.setAdapter(collectAdapter)
        refreshLayout?.isEnableLoadmore = false
    }

    override fun refreshData() {
        collectListViewModel.refreshData()
                .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .compose(asyncSchedulers())
                .subscribe({
                    if (!isAdded) return@subscribe
                    refreshLayout?.finishRefresh()
                    if (it.Data != null && it.Data?.albums != null && !it.Data?.albums?.isEmpty()!!) {
                        collectAdapter.addHeaderItems(it.Data?.albums!!)
                        count.visibility = View.VISIBLE
                        count.text = getString(R.string.count_format, it.Data?.albums!!.size)
                        hideTipView()
                    } else {
                        collectAdapter.clearData()
                        showEmpty()
                    }
                }, {
                    if (!isAdded) return@subscribe
                    showLoadFail(collectAdapter.dataList.isEmpty())
                    refreshLayout?.finishRefresh()
                })
    }

    override fun refresh() {
        if (!isAdded) return
        refreshLayout?.autoRefresh()
    }
}


fun createCollectFragment(): CollectFragment {
    return CollectFragment()
}