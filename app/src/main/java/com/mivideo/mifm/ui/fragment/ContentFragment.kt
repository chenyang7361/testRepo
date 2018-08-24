package com.mivideo.mifm.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alibaba.android.arouter.launcher.ARouter
import com.hwangjr.rxbus.annotation.Subscribe
import com.mivideo.mifm.R
import com.mivideo.mifm.events.ShowHomeFragmentEvent
import timber.log.Timber

/**
 * 内容层
 */
class ContentFragment : BaseFragment() {

    var acceptInput: Boolean = true

    private var routerFragmentExitToHome = false

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.content_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments?.get("intent") != null) {
            val intent = arguments!!.get("intent") as Intent

            if (!handledScheme(intent, savedInstanceState)) {
                if (savedInstanceState == null) {
                    loadRootFragment(R.id.contentFragmentContainer, createTabHostFragment())
                }
            }
        } else {
            if (savedInstanceState == null) {
                loadRootFragment(R.id.contentFragmentContainer, createTabHostFragment())
            }
        }
    }


    /**
     * 处理MainActivity拦截的Scheme跳转
     */
    private fun handledScheme(intent: Intent?, savedInstanceState: Bundle?): Boolean {
        if (!isAdded) return false
        childFragmentManager.removeOnBackStackChangedListener(backStackChangeListener)
        if (intent == null || intent.data == null) return false
        val uri = intent.data
        Timber.i("intent data:$uri")

        routerFragmentExitToHome = uri?.getQueryParameter("exit_to_home") == "true"
        Timber.i("exitToHome:$routerFragmentExitToHome")

        val fragment = ARouter.getInstance()
                .build(uri)
                .navigation()
        if (fragment == null || fragment !is BaseFragment) {
            return false
        }

        /*如果当前fragment栈中已经存在路由跳转的fragment就直接跳转到对应fragment,
        并回调此存在的路由fragment的onNewBundle方法，使其接收新参数*/
        if (topChildFragment != null && findChildFragment(fragment.javaClass) != null) {
            if (topChildFragment::class.java.simpleName != fragment::class.java.simpleName) {
                topChildFragment.popTo(fragment.javaClass, false)
            }
            findChildFragment(fragment.javaClass).onNewBundle(fragment.arguments)
        } else if (topChildFragment != null && topChildFragment is TabHostFragment) {
            /*如果历史栈中不存在当前路由fragment，但是Tab首页存在，保留Tab首页
             通过Tab页直接start路由fragment*/
            topChildFragment.start(fragment)
        } else {
            /* 历史栈中不存在当前路由fragment且Tab首页也不存在，则直接清除fragment栈，将路由fragment
            加载到contentFragment的根布局上*/
            while (childFragmentManager.popBackStackImmediate()) {
                Timber.i("new scheme enter,pop child")
            }
            /*如果路由参数中携带了exit_to_home参数，返回是需要返回到首页，所以现价在hostFragment,
             HostFragment加载成功了后在跳转到路由fragment
             */
            if (routerFragmentExitToHome) {
                val tabHostFragment = createTabHostFragment()
                loadRootFragment(R.id.contentFragmentContainer, tabHostFragment)
                tabHostFragment.setLifecycleListener(object : FragmentLifecycleListener() {
                    override fun onViewCreated() {
                        super.onViewCreated()
                        topChildFragment?.start(fragment)
                        routerFragmentExitToHome = false
                    }
                })
            } else {
                loadRootFragment(R.id.contentFragmentContainer, fragment)
            }
        }
        childFragmentManager.addOnBackStackChangedListener(backStackChangeListener)
        return true
    }

    private val backStackChangeListener = FragmentManager.OnBackStackChangedListener {
        if (childFragmentManager.backStackEntryCount < 1) {
            activity?.finish()
        }
    }

    fun removeBackStackChangeListener() {
        childFragmentManager.removeOnBackStackChangedListener(backStackChangeListener)
    }


    @Subscribe
    fun onShowHomeFragmentEvent(event: ShowHomeFragmentEvent) {
        if (findChildFragment(TabHostFragment::class.java) != null) {
            if (topChildFragment::class.java.simpleName != TabHostFragment::class.java.simpleName) {
                topChildFragment.popTo(TabHostFragment::class.java, false)
            }
        } else {
            while (childFragmentManager.backStackEntryCount > 1) {
                childFragmentManager.popBackStackImmediate()
            }
            view?.post {
                loadRootFragment(R.id.contentFragmentContainer, createTabHostFragment(), false, false)
            }
        }
    }

    override fun onBackPressedSupport(): Boolean {
        if (childFragmentManager.backStackEntryCount > 1) {
            popChild()
            return true
        }

        return false
    }

    override fun getUserVisibleHint(): Boolean {
        return acceptInput
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handledScheme(intent, null)
    }

}

fun createContentFragment(): ContentFragment {
    return ContentFragment()
}
