package com.mivideo.mifm.ui.fragment

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.hwangjr.rxbus.annotation.Subscribe
import com.mivideo.mifm.R
import com.mivideo.mifm.data.viewmodel.TabListViewModel
import com.mivideo.mifm.events.*
import com.mivideo.mifm.ui.fragment.home.createMediaListFragment
import com.mivideo.mifm.ui.widget.TabLayout2
import com.mivideo.mifm.util.app.postEvent
import com.trello.rxlifecycle.FragmentEvent
import kotlinx.android.synthetic.main.home_title_layout.*
import org.jetbrains.anko.find
import org.jetbrains.anko.onClick
import timber.log.Timber

class HomeFragment : TabFragment() {

    private lateinit var standardHomeLayout: FrameLayout
    private lateinit var categoryTab: TabLayout2
    private lateinit var viewPager: ViewPager
    private lateinit var homeHeaderContainer: View
    private lateinit var searchLayout: LinearLayout
    private lateinit var historyLayout: LinearLayout
    private lateinit var tabListViewModel: TabListViewModel

    companion object {
        val HOME_FRAGMENT = "homeFragment"
        val DEFAULT_TAB_ID = "recom"
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        standardHomeLayout = view.find(R.id.standardHomeLayout)
        standardHomeLayout.visibility = View.VISIBLE
        // 分类列表页
        categoryTab = view.find(R.id.categoryListTab)
        // 工具栏
        homeHeaderContainer = view.find(R.id.homeHeaderContainer)
        viewPager = view.find(R.id.home_view_pager)
        searchLayout = view.find(R.id.searchLayout)
        searchLayout.onClick {
            start(createSearchFragment())
        }
        history.onClick {
            postEvent(ShowHistoryEvent())
        }

        tabListViewModel = TabListViewModel(context)
        lifecycle.addObserver(tabListViewModel)

        viewPager.addOnPageChangeListener(
                PauseGlideWhenViewPagerScrollingListener(Glide.with(this@HomeFragment), tabListViewModel))

        // 将分类 Tab 控件和数据绑定起来
        categoryTab.setupWithViewPager(viewPager)

        /**
         * 设置tab item的点击事件
         */
        categoryTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                postEvent(RefreshMediaListEvent(tab!!.position))
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
            }
        })

        viewPager.adapter = VideoListPageAdapter2(tabListViewModel, childFragmentManager)

        /**
         * 监听Tab数据变化
         */
        tabListViewModel.observeUpdate()
                .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe({ range ->
                    Timber.i({ "range: $range" }.invoke())
                    (viewPager.adapter as VideoListPageAdapter2).notifyDataSetChanged()
                    categoryTab.fullScroll(0)
                    categoryTab.updateTabData(changeTabListData(tabListViewModel))
                }, { error ->
                    Timber.i({ "error: $error" }.invoke())
                })

        postFragmentCreateEvent()
        initMiniPlayer()
    }

    private fun initMiniPlayer() {
        if (mediaManager.isPlaying()) {
            miniPlayer.switchToPlay()
        }
    }

    fun refreshList() {
        postEvent(RefreshMediaListEvent(viewPager.currentItem))
    }

    private fun changeTabListData(tabListViewModel: TabListViewModel): ArrayList<String> {
        val titleList = ArrayList<String>()
        for (i in 0 until tabListViewModel.getCount()) {
            if (tabListViewModel.getItem(i) != null) {
                titleList.add(tabListViewModel.getItem(i)!!.name)
            }
        }
        return titleList
    }

    @Subscribe
    fun onMediaPrepared(event: MediaPreparedEvent) {
        miniPlayer?.switchToPlay()
    }

    @Subscribe
    fun onMediaComplete(event: MediaCompleteEvent) {
        miniPlayer?.switchToPause()
    }

    @Subscribe
    fun onStartDetailFragment(event: StartDetailFragmentEvent) {
        postEvent(HideStatusBarEvent())
        start(createMediaDetailFragment(event.id))
    }

    @Subscribe
    fun onRecommendMoreClick(event: RecommendMoreEvent) {
        viewPager.setCurrentItem(getTabIndexById(event.tabId))
    }

    private fun getTabIndexById(tabId: String): Int {
        for (i in 0 until tabListViewModel.getCount()) {
            if (tabListViewModel.getItem(i) != null && tabListViewModel.getItem(i)!!.id.equals(tabId)) {
                return i
            }
        }
        return viewPager.currentItem
    }

    @Subscribe
    fun onRecommendHeadLineClick(event: HeadLineRecommendClickEvent) {
        start(createHeadlinesFragment())
    }
}

fun createHomeFragment(): HomeFragment {
    return HomeFragment()
}


class VideoListPageAdapter2(private val tabListViewModel: TabListViewModel, fragmentManager: FragmentManager) :
        FragmentStatePagerAdapter(fragmentManager) {

    override fun getCount(): Int = tabListViewModel.getCount()

    override fun getItem(position: Int): Fragment {
        // tabList 必须已经全部载入才可以显示，否则应该由外层容器提供 Loading 提示
        // 这里并不考虑尚未载入时的边界条件，如果出现问题 (!!) 会提供显式断言
        val tabItem = tabListViewModel.getItem(position)!!
        return createMediaListFragment(tabItem.id, tabItem.name, position)
    }

    // (!!) 同上
    override fun getPageTitle(position: Int): CharSequence {
        return tabListViewModel.getItem(position)!!.name
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//        super.destroyItem(container, position, `object`)
    }
}

class PauseGlideWhenViewPagerScrollingListener(private val requestManager: RequestManager, private val tabListViewModel: TabListViewModel) : ViewPager.OnPageChangeListener {

    override fun onPageScrollStateChanged(state: Int) {
        if (state != ViewPager.SCROLL_STATE_IDLE) {
            requestManager.pauseRequests()
        } else {
            requestManager.resumeRequests()
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        if (tabListViewModel.tabs.isNotEmpty()) {
            postEvent(HomeMediaPageChangeEvent(position, tabListViewModel.tabs[position].id))
        }
    }
}
