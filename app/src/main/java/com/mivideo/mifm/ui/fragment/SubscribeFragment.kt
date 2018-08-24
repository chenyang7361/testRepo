package com.mivideo.mifm.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.salomonbrys.kodein.instance
import com.hwangjr.rxbus.annotation.Subscribe
import com.mivideo.mifm.R
import com.mivideo.mifm.account.UserAccountManager
import com.mivideo.mifm.events.MediaCompleteEvent
import com.mivideo.mifm.events.MediaPreparedEvent
import com.mivideo.mifm.events.ShowHistoryEvent
import com.mivideo.mifm.ui.widget.SubscribeTab
import kotlinx.android.synthetic.main.subscribe_fragment.*
import kotlinx.android.synthetic.main.subscribe_title_layout.*
import org.jetbrains.anko.onClick

/**
 * 听单页
 */
class SubscribeFragment : TabFragment() {

    companion object {
        const val SUBSCRIBE_FRAGMENT = "subscribeFragment"
        const val SUBSCRIBE_TAB = "subscribe"
    }

    private val userAccountManager: UserAccountManager by instance()
    private val historyFragment: HistoryFragment = createHistoryFragment()
    private val collectFragment: CollectFragment = createCollectFragment()
    private val fragments = ArrayList<BaseFragment>()
    private lateinit var currentFragment: BaseFragment

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.subscribe_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        left.onClick {
            start(createSearchFragment())
        }
        fragments.add(historyFragment)
        fragments.add(collectFragment)
        currentFragment = historyFragment
        loadMultipleRootFragment(R.id.container, 0, historyFragment, collectFragment)
        tabs.updateTabData(listOf(resources.getString(R.string.history), resources.getString(R.string.favor)))
        title.visibility = View.VISIBLE
        tabs.addOnTabClickListener(object : SubscribeTab.OnTabClickListener {

            override fun onTabClick(position: Int, tab: SubscribeTab.TabItemView): Boolean {
//                if (position == 1) {
//                    return if (!userAccountManager.userLoggedIn()) {
//                        userAccountManager.startLogin(activity!!, null)
//                        false
//                    } else {
//                        showHideFragment(currentFragment, fragments[position])
//                        currentFragment = fragments[position]
//                        true
//                    }
//                }
                showHideFragment(fragments[position], currentFragment)
                currentFragment = fragments[position]
                return true
            }
        })
    }

    override fun onSupportVisible() {
        if (mediaManager.isPlaying()) {
            miniPlayer.switchToPlay()
        }
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
    fun showHistory(event: ShowHistoryEvent) {
        showHideFragment(currentFragment, fragments[0])
        currentFragment = fragments[0]
    }
}

fun createSubscribeFragment(): SubscribeFragment {
    return SubscribeFragment()
}
