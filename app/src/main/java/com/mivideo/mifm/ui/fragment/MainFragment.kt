package com.mivideo.mifm.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hwangjr.rxbus.annotation.Subscribe
import com.mivideo.mifm.R
import com.mivideo.mifm.events.ShowPlayerEvent
import com.mivideo.mifm.util.app.DisplayUtil
import com.mivideo.mifm.util.getColor
import qiu.niorgai.StatusBarCompat

/**
 * MainFragment 主容器
 *   |
 *   +-- ContentFragment
 *   |     |
 *   |     +--TabHostFragment
 *   |     |        +-- HomeFragment
 *   |     |        +-- SubscribeFragment
 *   |     |        +-- MineFragment
 *   |     |
 *   |     +--CommunityFragment
 *   |     +--SettingsFragment
 *   |
 *   +-- DraggableFragment 拖拽容器
 *         |
 *         +-- WriteCommentFragment 写评论
 */
class MainFragment : BaseFragment() {

    private var contentFragment: ContentFragment? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            contentFragment = createContentFragment()
            if (arguments != null) {
                val intent = arguments!!.get("intent") as Intent
                val arg = Bundle()
                arg.putParcelable("intent", intent)
                contentFragment?.arguments = arg
            }
            loadRootFragment(R.id.mainContainer, contentFragment)
        } else {
            contentFragment = findChildFragment(ContentFragment::class.java)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (isAdded) {
            contentFragment?.onNewIntent(intent)
        }
    }

    @Subscribe
    fun onStartPlayerPage(event:ShowPlayerEvent){
       showStatusBar(activity as Activity)
        DisplayUtil.setColor(activity as Activity, getColor(context, R.color.alpha_0_5_black), 0, true)
        start(createAudioPlayFragment())

    }
}

fun makeMainFragment(): MainFragment {
    return MainFragment()
}
