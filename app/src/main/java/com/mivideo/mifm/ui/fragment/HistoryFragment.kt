package com.mivideo.mifm.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hwangjr.rxbus.annotation.Subscribe
import com.mivideo.mifm.R
import com.mivideo.mifm.events.HistoryItemCheckChangeEvent
import com.mivideo.mifm.events.PlayHistoryEvent
import com.mivideo.mifm.events.ShowPlayerEvent
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.ui.adapter.managedelete.*
import com.mivideo.mifm.ui.adapter.subscribe.HistoryDelegate
import com.mivideo.mifm.util.app.postEvent
import com.mivideo.mifm.viewmodel.HistoryViewModel
import com.trello.rxlifecycle.FragmentEvent
import kotlinx.android.synthetic.main.subscribe_item_title.*

/**
 * Created by Jiwei Yuan on 18-7-26.
 */
class HistoryFragment : BaseRefreshListFragment() {

    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var historyAdapter: DeleteManageAdapter
    private lateinit var manageDeleteViewHolder: ManageDeleteViewHolder

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun initRefreshView(view: View) {
        historyViewModel = HistoryViewModel(context)
        lifecycle.addObserver(historyViewModel)
        channelTitle.text = resources.getString(R.string.play_history)

        refreshLayout = view.findViewById(R.id.refreshLayout)
        historyAdapter = DeleteManageAdapter()
        historyAdapter.mDelegatesManager.addDelegate(HistoryDelegate())
        refreshLayout?.setAdapter(historyAdapter)

        manageDeleteViewHolder = ManageDeleteViewHolder()
        manageDeleteViewHolder.attachAdapter(historyAdapter)
        manageDeleteViewHolder.attachDeleteManager(object : DeleteManager {
            override fun delete(context: Context?, key: List<String>) {
                historyViewModel.deleteHistory(key)
                        .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                        .subscribe({
                            historyAdapter.deleteData(key)
                            manageDeleteViewHolder.onFinishDelete()
                        })
            }
        })

        val controller = ManageControllerView()
        controller.fillWithViews(edit, llEdit, delete, cancel, selectAll)
        manageDeleteViewHolder.attachView(controller)
        historyViewModel.loadDataCount().subscribe({
            count.visibility = View.VISIBLE
            count.text = getString(R.string.count_format, it)
        }, {})
    }

    override fun refreshData() {
        historyViewModel.refreshData()
                .compose(asyncSchedulers())
                .compose(bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe({
                    historyAdapter.clearData()
                    historyAdapter.addDefaultDataList(it as ArrayList<Managable>)
                    manageDeleteViewHolder.switchToOriginal()
                    refreshLayout?.finishRefresh()
                }, {
                    refreshLayout?.finishRefresh()
                })

    }

    override fun loadMore() {
        historyViewModel.loadMoreData()
                .compose(asyncSchedulers())
                .compose(bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe({
                    historyAdapter.addFooterItems(it as ArrayList<Managable>)
                    refreshLayout?.finishLoadmore()
                }, {
                    refreshLayout?.finishLoadmore()
                })
    }

    @Subscribe
    fun onItemCheckChanged(event: HistoryItemCheckChangeEvent) {
        if (event.checked) {
            manageDeleteViewHolder.addToDelete(event.id)
        } else {
            manageDeleteViewHolder.removeFromDelete(event.id)
        }
    }

    @Subscribe
    fun onPlayHistory(event: PlayHistoryEvent) {
        mediaManager.playHistory(event.item)
        postEvent(ShowPlayerEvent())
    }

    override fun onSupportVisible() {
        refreshData()
    }

    override fun checkNetwork() {
        //history use local db,work well without net
    }

}


fun createHistoryFragment(): HistoryFragment {
    return HistoryFragment()
}