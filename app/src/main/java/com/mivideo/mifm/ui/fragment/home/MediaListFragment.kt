package com.mivideo.mifm.ui.fragment.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mivideo.mifm.NetworkManager
import com.mivideo.mifm.R
import com.mivideo.mifm.ui.fragment.BaseRefreshListFragment

class MediaListFragment : BaseRefreshListFragment() {
    companion object {
        const val TAB_ID_RECOMMEND = "recom"
    }

    private var tabPosition: Int = 0
    private lateinit var tabId: String
    private lateinit var tabName: String
    private lateinit var mDelegate: IMediaListDelegate

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fm_list_fragment, container, false)
    }

    override fun preInitRefresh() {
        tabId = arguments?.getString("tabId") ?: ""
        tabName = arguments?.getString("tabName") ?: ""
        tabPosition = arguments?.getInt("position") ?: -1
    }

    override fun initRefreshView(view: View) {
        refreshLayout = view.findViewById(R.id.refreshLayout)
        mDelegate = createMediaListDelegate(tabId)
        refreshLayout?.setAdapter(mDelegate.getAdapter())
        refreshLayout?.getRecyclerView()?.setHasFixedSize(true)
    }

    override fun refresh() {
        mDelegate.refreshData()
    }

    override fun refreshData() {
        mDelegate.refreshData()
    }

    override fun loadMore() {
        mDelegate.loadMore()
    }

    override fun checkNetwork() {
        if (NetworkManager.isNetworkUnConnected()) {
            mDelegate.loadDataFromCache()
        } else {
            refreshLayout?.autoRefresh()
        }
    }

    private fun createMediaListDelegate(tabId: String): IMediaListDelegate {
        if (tabId.equals(MediaListFragment.TAB_ID_RECOMMEND)) {
            return RecommendMediaListDelegate(this@MediaListFragment)
        }
        return ChannelMediaListDelegate(this@MediaListFragment, tabId)
    }
}

fun createMediaListFragment(tabId: String, tabName: String, position: Int): MediaListFragment {
    val fragment = MediaListFragment()
    val bundle = Bundle()
    bundle.putString("tabId", tabId)
    bundle.putString("tabName", tabName)
    bundle.putInt("position", position)
    fragment.arguments = bundle
    return fragment
}

