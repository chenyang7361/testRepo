package com.mivideo.mifm.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Toast
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.hwangjr.rxbus.RxBus
import com.hwangjr.rxbus.annotation.Subscribe
import com.mivideo.mifm.NetworkManager
import com.mivideo.mifm.R
import com.mivideo.mifm.events.FragmentCreatedEvent
import com.mivideo.mifm.events.MediaPreparedEvent
import com.mivideo.mifm.player.manager.MediaManager
import com.mivideo.mifm.ui.widget.LoadTipView
import com.mivideo.mifm.ui.widget.MiniPlayerView
import com.mivideo.mifm.util.app.postEvent
import com.squareup.leakcanary.RefWatcher

open class BaseFragment : RxSupportFragment(), KodeinInjected {

    override val injector = KodeinInjector()

    open var mFragmentName = javaClass.simpleName
    open var mFragmentTitle = ""
    private val refWatcher: RefWatcher by instance()
    private val appContext: Context by instance("appContext")
    protected var mInteractionListener: InteractionListener? = null

    private var mLifecycleListener: FragmentLifecycleListener? = null

    val mediaManager: MediaManager = MediaManager.getInstance()

    public fun setLifecycleListener(listener: FragmentLifecycleListener) {
        mLifecycleListener = listener
    }

    var loadTip: LoadTipView? = null

    override fun getContext(): Context {
        return super.getContext() ?: appContext
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        inject(appKodein())
        RxBus.get().register(this)
//        checkNetwork()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mLifecycleListener?.onViewCreated()
    }

    override fun onDetach() {
        super.onDetach()
        RxBus.get().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        refWatcher.watch(this)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }


    fun postFragmentCreateEvent() {
        postEvent(FragmentCreatedEvent(mFragmentName))
    }

    override fun onBackPressedSupport(): Boolean {
        return super.onBackPressedSupport()
    }

    open fun onNewIntent(intent: Intent?) {

    }

    /**
     * 检测设备网络状况
     */
    private fun checkNetwork() {

        if (SystemClock.elapsedRealtime() - lastFragmentToast < 900) {
            return
        }
        lastFragmentToast = SystemClock.elapsedRealtime()

        if (NetworkManager.isNetworkUnConnected()) {
            Toast.makeText(context, "当前设备没有网络!", Toast.LENGTH_SHORT).show()
        }
    }


    open fun showLoadError() {
        if (context == null) {
            return
        }
        Toast.makeText(context, context.resources.getString(R.string.load_data_error), Toast.LENGTH_SHORT).show()
    }

    open fun showLoadComplete() {
        if (context == null) {
            return
        }
        Toast.makeText(context, context.resources.getString(R.string.load_data_complete), Toast.LENGTH_SHORT).show()
    }


    /**
     * CommunityFragment与其他fragment交互式接口
     */
    fun setInteractionListener(listener: InteractionListener?) {
        mInteractionListener = listener
    }

    abstract class InteractionListener {
        open fun onHandleBack(): Boolean {
            return false
        }
    }

    /**
     * fragment生命周期监听
     */
    abstract class FragmentLifecycleListener {
        open fun onViewCreated() {

        }
    }

    open fun showNetUnconnected() {
        loadTip?.showNetUnconnected()
    }

    open fun showLoadFail() {
        loadTip?.showLoadFail()
    }

    fun showProgress() {
        loadTip?.showProgress()
    }

    fun showEmpty() {
        loadTip?.showEmpty()
    }

    fun hideTipView() {
        loadTip?.hideTipView()
    }

    fun tipShowing(): Boolean {
        return loadTip?.visibility == View.VISIBLE
    }
}

var lastFragmentToast: Long = 0.toLong()
