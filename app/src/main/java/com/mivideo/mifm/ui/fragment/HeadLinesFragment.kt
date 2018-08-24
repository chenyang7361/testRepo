package com.mivideo.mifm.ui.fragment

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hwangjr.rxbus.annotation.Subscribe
import com.mivideo.mifm.R
import com.mivideo.mifm.data.viewmodel.HeadlineTabViewModel
import com.mivideo.mifm.events.MediaCompleteEvent
import com.mivideo.mifm.events.MediaPreparedEvent
import com.trello.rxlifecycle.FragmentEvent
import kotlinx.android.synthetic.main.headline_category_list_layout.*
import kotlinx.android.synthetic.main.headline_fragment.*
import kotlinx.android.synthetic.main.home_title_layout.*
import timber.log.Timber

/**
 * Created by Jiwei Yuan on 18-7-24.
 */
class HeadLinesFragment : BaseFragment() {

    private lateinit var tabListViewModel: HeadlineTabViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.headline_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabListViewModel = HeadlineTabViewModel(context)
        lifecycle.addObserver(tabListViewModel)

//        headlinePager.addOnPageChangeListener(
//                PauseGlideWhenViewPagerScrollingListener(Glide.with(this@HeadLinesFragment), tabListViewModel))
        headlinePager.adapter = HeadLinePagerAdapter(tabListViewModel, fragmentManager!!)

        // 将分类 Tab 控件和数据绑定起来
        categoryListTab.setupWithViewPager(headlinePager)

        /**
         * 设置tab item的点击事件
         */
        categoryListTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
//                postEvent(RefreshFmListEvent(tab!!.position))
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
            }
        })


        // 监听Tab数据变化
        tabListViewModel.observeUpdate()
                .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe({ range ->
                    Timber.i({ "range: $range" }.invoke())
                    (headlinePager.adapter as HeadLinePagerAdapter).notifyDataSetChanged()
                    categoryListTab.fullScroll(0)
                    categoryListTab.updateTabData(changeTabListData(tabListViewModel))
                }, { error ->
                    Timber.i({ "error: $error" }.invoke())
                })
        initMiniPlayer()
    }

    private fun initMiniPlayer() {
        if (mediaManager.isPlaying()) {
            miniPlayer.switchToPlay()
        }
    }

    private fun changeTabListData(tabListViewModel: HeadlineTabViewModel): ArrayList<String> {
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
        miniPlayer.switchToPlay()
    }
    @Subscribe
    fun onMediaComplete(event: MediaCompleteEvent) {
        miniPlayer?.switchToPause()
    }
}

fun createHeadlinesFragment(): HeadLinesFragment {
    return HeadLinesFragment()
}

class HeadLinePagerAdapter(private val tabListViewModel: HeadlineTabViewModel, fragmentManager: FragmentManager) :
        FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        val tabItem = tabListViewModel.getItem(position)!!
        return createHeadlineListFragment(tabItem.id, "", position)
    }


    override fun getCount(): Int = tabListViewModel.getCount()

    // (!!) 同上
    override fun getPageTitle(position: Int): CharSequence {
        return tabListViewModel.getItem(position)!!.name
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//        super.destroyItem(container, position, `object`)
    }

}