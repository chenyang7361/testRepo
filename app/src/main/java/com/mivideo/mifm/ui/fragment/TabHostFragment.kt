package com.mivideo.mifm.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import com.hwangjr.rxbus.annotation.Subscribe
import com.mivideo.mifm.MainConfig
import com.mivideo.mifm.R
import com.mivideo.mifm.RouterConf
import com.mivideo.mifm.data.models.jsondata.BottomTabEntity
import com.mivideo.mifm.events.HomeMediaPageChangeEvent
import com.mivideo.mifm.events.ShowHistoryEvent
import com.mivideo.mifm.events.TabChangeEvent
import com.mivideo.mifm.events.TabHostChangeEvent
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.ui.widget.HomeTabItem
import com.mivideo.mifm.util.app.postEvent
import com.mivideo.mifm.util.app.showToast
import com.mivideo.mifm.viewmodel.TabDataViewModel
import kotlinx.android.synthetic.main.fragment_tab_host.*
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.onClick
import timber.log.Timber

/**
 * 主界面Tab容器Fragment
 *
 * @author LiYan
 */
@Route(path = RouterConf.PATH_TAB_HOST)
class TabHostFragment : BaseFragment() {
    companion object {
        const val ARG_TARGET_TAB = "targetTab"
        const val ARG_TARGET_SUB = "sub"

        /**
         * 首页tab
         */
        const val TAB_HOME = "homeTab"
        /**
         * 订阅tab
         */
        const val TAB_SUBSCRIBE = "subscribeTab"
        /**
         * 我的tab
         */
        const val TAB_MINE = "mineTab"
    }

    private lateinit var homeFragment: HomeFragment
    private lateinit var subscribeFragment: SubscribeFragment
    private lateinit var mineFragment: MineFragment

    private var mineTabItem: HomeTabItem? = null
    private var homeTabItem: HomeTabItem? = null
    private var subscribeItem: HomeTabItem? = null

    private lateinit var tabDataVideoModel: TabDataViewModel

    private var taskTabImgView: ImageView? = null
    private var homeTabInfo: BottomTabEntity.DataEntity? = null
    private lateinit var taskTabInfo: BottomTabEntity.DataEntity
    private var tabInfoList: List<BottomTabEntity.DataEntity>? = null
    private val tabItemList: ArrayList<HomeTabItem> = ArrayList()
    private var keyBackTime: Long = 0

    @Autowired(name = ARG_TARGET_TAB)
    @JvmField
    var targetTab: String? = ""

    @Autowired(name = ARG_TARGET_SUB)
    @JvmField
    var sub: String? = ""

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_host, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState == null) {
            var sub = arguments?.getString(ARG_TARGET_SUB) ?: ""
            homeFragment = createHomeFragment()
            subscribeFragment = createSubscribeFragment()
            mineFragment = createMineFragment(false, sub)
            loadMultipleRootFragment(R.id.tab_host_container, 0,
                    homeFragment, subscribeFragment, mineFragment)
        } else {
            homeFragment = findChildFragment(HomeFragment::class.java)
            subscribeFragment = findChildFragment(SubscribeFragment::class.java)
            mineFragment = findChildFragment(MineFragment::class.java)
        }
        layout_bottom_nav.visibility = View.VISIBLE
        tabDataVideoModel = TabDataViewModel(context)
        tabDataVideoModel.getTabInfo()
                .compose(asyncSchedulers())
                .subscribe({ entity ->
                    renderTab(entity)
                    if (arguments != null) {
                        handleRoute(arguments)
                    }
                }, {})
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun renderTab(entity: BottomTabEntity?) {
        tabInfoList = entity?.data
        tabInfoList?.forEachWithIndex { position, item ->
            val tabItem = HomeTabItem(context)
            if (!homeFragment.isHidden) {
                if (item.action == "view-home") {
                    tabItem.loadImage(item.icon_checked!!)
                    preLoadImgToGlide(item.icon!!)
                    tabItem.setCheckedTextColor()
                } else {
                    tabItem.loadImage(item.icon!!)
                    preLoadImgToGlide(item.icon_checked!!)
                }
                tabItem.setText(item.name!!)
            }
            if (!subscribeFragment.isHidden) {
                if (item.action == "view-subscribe") {
                    tabItem.loadImage(item.icon_checked!!)
                    preLoadImgToGlide(item.icon!!)
                    tabItem.setCheckedTextColor()
                } else {
                    tabItem.loadImage(item.icon!!)
                    preLoadImgToGlide(item.icon_checked!!)
                }
                tabItem.setText(item.name!!)
            }
            if (!mineFragment.isHidden) {
                if (item.action == "view-mine") {
                    tabItem.loadImage(item.icon_checked!!)
                    preLoadImgToGlide(item.icon!!)
                    tabItem.setCheckedTextColor()
                } else {
                    tabItem.loadImage(item.icon!!)
                    preLoadImgToGlide(item.icon_checked!!)
                }
                tabItem.setText(item.name!!)
            }

            tabItemList.add(tabItem)
            ll_nav_item_container.addView(tabItem)
            if (item.action == "view-mine") {
                mineTabItem = tabItem
            } else if (item.action == "view-home") {
                homeTabItem = tabItem
                homeTabInfo = item
            } else if (item.action == "view-subscribe") {
                subscribeItem = tabItem
            }

            tabItem.onClick {
                setAllTabDefault(entity?.data)
                if (item.action == "view-home") {
                    postEvent(TabHostChangeEvent(HomeFragment.HOME_FRAGMENT, HomeFragment.DEFAULT_TAB_ID))
                    if (homeFragment.isHidden) {
                        showHideFragment(homeFragment, subscribeFragment)
                        showHideFragment(homeFragment, mineFragment)
                    } else {
                        homeFragment.refreshList()
                    }
                } else if (item.action == "view-subscribe") {
                    postEvent(TabHostChangeEvent(SubscribeFragment.SUBSCRIBE_FRAGMENT, SubscribeFragment.SUBSCRIBE_TAB))
                    if (!subscribeFragment.isAdded || subscribeFragment.isHidden) {
                        showHideFragment(subscribeFragment, homeFragment)
                        showHideFragment(subscribeFragment, mineFragment)
                    }

                } else if (item.action == "view-mine") {
                    postEvent(TabHostChangeEvent(MineFragment.MINE_FRAGMENT, MineFragment.MINE_TAB))
                    showHideFragment(mineFragment, subscribeFragment)
                    showHideFragment(mineFragment, homeFragment)
                }

                if (item.action == "view-home") {
                    if (item.itemPosition >= MainConfig.RECOMMEND_VISIBLE_ITEM_POSITION
                            && item.tabPosition == 0) {
                        setTabItemReFresh(tabItem)
                    } else {
                        tabItem.loadImage(item.icon_checked!!)
                        tabItem.setCheckedTextColor()
                        tabItem.showRedDot(false)
                    }
                } else {
                    tabItem.loadImage(item.icon_checked!!)
                    tabItem.setCheckedTextColor()
                    tabItem.showRedDot(false)
                }
            }
        }
        requestRedDotNotice()
        updateNewMsgRedDot()
    }

    @Subscribe
    fun onHomeMediaPageChangeEvent(event: HomeMediaPageChangeEvent) {
        if (homeTabItem == null || homeTabInfo == null) {
            return
        }
        if (event.position != 0 || (event.position == 0 && homeTabInfo!!.itemPosition < MainConfig.RECOMMEND_VISIBLE_ITEM_POSITION)) {
            if (homeTabItem!!.getText() != homeTabInfo!!.name!!) {
                homeTabItem!!.updateWithAnim({
                    homeTabInfo!!.tabPosition = event.position
                }, {
                    homeTabItem!!.setText(homeTabInfo!!.name!!)
                    homeTabItem!!.setCheckedTextColor()
                    homeTabItem!!.loadImage(homeTabInfo!!.icon_checked!!)
                })
            }
        } else {
            homeTabItem!!.updateWithAnim({
                homeTabInfo!!.tabPosition = event.position
            }, {
                setTabItemReFresh(homeTabItem!!)
            })
        }
    }

    override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {
        super.onFragmentResult(requestCode, resultCode, data)
    }

    override fun onBackPressedSupport(): Boolean {
        if (System.currentTimeMillis() - keyBackTime > MainConfig.STANDARD_BACK_INTERVAL_TIME
                || keyBackTime == 0.toLong()) {
            keyBackTime = System.currentTimeMillis()
            if (homeFragment.isVisible) {
                homeFragment.refreshList()
            }
            showToast(context, "再按一次退出")
            return true
        }
        return super.onBackPressedSupport()
    }

    private fun setTabItemReFresh(tabItem: HomeTabItem) {
        tabItem.setText("刷新")
        tabItem.loadImage(R.drawable.home_bar_icon_refresh_pressed)
        tabItem.setCheckedTextColor()
    }

    fun showHomeFragment() {
        homeTabItem?.performClick()
    }

    fun showSubscribeFragment() {
        subscribeItem?.performClick()
    }

    private fun preLoadImgToGlide(url: String) {
        Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .dontTransform()
                .preload()
    }

    private fun loadImage(imageView: ImageView, url: String) {
        Glide.with(context)
                .load(url)
                .priority(Priority.HIGH)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .dontTransform()
                .into(imageView)
    }

    private fun showMineFragment() {
        mineTabItem?.performClick()
    }

    private fun updateNewMsgRedDot() {
        /**
         * 监听消息数据变化
         */
//        PollingService.observerPollingUpdate()
//                .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
//                .compose(asyncSchedulers())
//                .subscribe({ newsCountInfo ->
//                    val countInfo = newsCountInfo as NewsCountInfo
//                    if (countInfo.totalCount == 0) {
//                        mineTabItem?.showRedDot(false)
//                    } else {
//                        mineTabItem?.showRedDot(true)
//                    }
//                }, {})
    }

    private fun requestRedDotNotice() {
//        val redDotCheckList = ArrayList<String>()
//        val redDotNeedShowList = ArrayList<String>()
//        tabInfoList?.forEach {
//            if (it.red_point) {
//                redDotCheckList.add(it._id!!)
//            }
//        }
//        tabDataVideoModel.requestRedDotNotice(redDotCheckList)
//                .compose(asyncSchedulers())
//                .subscribe({ jsonObject ->
//                    val data = jsonObject?.optJSONObject("data")
//                    val redPointArray = data?.optJSONArray("red_point")
//                    val length: Int = redPointArray?.length() ?: 0
//                    (0..length).mapNotNullTo(redDotNeedShowList) { redPointArray?.optString(it) }
//                    tabInfoList?.forEachWithIndex { i, dataEntity ->
//                        if (redDotNeedShowList.contains(dataEntity._id)) {
//                            tabItemList[i].showRedDot(true)
//                        }
//                    }
//                }, {})
    }

    private fun setAllTabDefault(data: List<BottomTabEntity.DataEntity>?) {
        data?.forEachWithIndex { i, bottomNavTabEntity ->
            tabItemList[i].loadImage(bottomNavTabEntity.icon!!)
            tabItemList[i].setText(bottomNavTabEntity.name!!)
            tabItemList[i].setDefaultTextColor()
        }
        taskTabImgView?.let {
            loadImage(taskTabImgView!!, taskTabInfo.icon!!)
        }
    }

    override fun onNewBundle(args: Bundle?) {
        super.onNewBundle(args)
        Timber.i("onNewBundle-->$args")
        handleRoute(args)
    }

    private fun handleRoute(args: Bundle?) {
        Timber.i("handleRoute-->$args")
        if (args?.getString(ARG_TARGET_TAB) == TAB_HOME) {
            showHomeFragment()
        } else if (args?.getString(ARG_TARGET_TAB) == TAB_MINE) {
            showMineFragment()
        } else if (args?.getString(ARG_TARGET_TAB) == TAB_SUBSCRIBE) {
            showSubscribeFragment()
        }
    }

    @Subscribe
    fun onChangeTabEvent(event: TabChangeEvent) {
        if (event.tab == TAB_HOME) {
            showHomeFragment()
        } else if (event.tab == TAB_MINE) {
            showMineFragment()
        } else if (event.tab == TAB_SUBSCRIBE) {
            showSubscribeFragment()
        }
    }

    @Subscribe
    fun showHistory(event: ShowHistoryEvent) {
        showSubscribeFragment()
    }
}

fun createTabHostFragment(): TabHostFragment {
    return TabHostFragment()
}
