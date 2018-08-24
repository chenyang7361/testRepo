package com.mivideo.mifm

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.hwangjr.rxbus.RxBus
import com.hwangjr.rxbus.annotation.Subscribe
import com.mivideo.mifm.data.viewmodel.ConfigViewModel
import com.mivideo.mifm.events.CloseMainActivityEvent
import com.mivideo.mifm.events.FragmentCreatedEvent
import com.mivideo.mifm.events.HideStatusBarEvent
import com.mivideo.mifm.manager.UserLocationManager
import com.mivideo.mifm.ui.fragment.*
import com.mivideo.mifm.update.UpdateManager
import com.mivideo.mifm.util.SystemUtil
import com.mivideo.mifm.util.app.DisplayUtil
import com.tbruyelle.rxpermissions.RxPermissions
import org.jetbrains.anko.contentView
import qiu.niorgai.StatusBarCompat
import rx.Subscriber
import timber.log.Timber


class MainActivity : BaseActivity(), KodeinInjected {

    override val injector = KodeinInjector()
    private val configViewModel: ConfigViewModel by instance()

    var mainFragment: MainFragment? = null
    private var isFontColorDark: Boolean = false
    private var delayInitAction: Runnable? = null

    private val mainBackContainer by lazy {
        findViewById<FrameLayout>(R.id.mainBackContainer)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject(appKodein())
//        if (Build.VERSION.SDK_INT < 21 && SystemUtil.isCanChangeStatusBarSystem()) {
//            window.setFlags(
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//        }
        setContentView(R.layout.main_activity)
        DisplayUtil.setStatusBarLightMode(this, true)
        delayInit(savedInstanceState)
    }

    /**
     * 延迟onCreate方法中的初始化方法，防止onCreate方法阻塞
     */
    private fun delayInit(savedInstanceState: Bundle?) {
        delayInitAction = Runnable {
            RxBus.get().register(this)
            if (savedInstanceState == null) {
                mainFragment = makeMainFragment()
                if (mainFragment?.arguments == null) {
                    mainFragment?.arguments = Bundle()
                }
                mainFragment?.arguments?.putParcelable("intent", intent)
                loadRootFragment(R.id.mainActivityContainer, mainFragment!!)
            }
            handleIntent(intent, false)
            configViewModel.initConfig()
        }
        contentView?.post(delayInitAction)
        UpdateManager.getInstance(this).checkUpdate(this)
    }

    private var mHandleOnNewIntent: Boolean = false

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Timber.d("onNewIntent:$intent")
        setIntent(intent)
        if (!handleIntent(intent, true)) {
            mHandleOnNewIntent = true
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        if (mHandleOnNewIntent && isIntentFromRouter(intent)) {
            clearContentChildFragment()
            mainFragment?.onNewIntent(intent)
            mHandleOnNewIntent = false
        }
    }

    private fun isIntentFromRouter(intent: Intent?): Boolean {
        if (intent == null || intent.data == null) return false
        return intent.data.host == RouterConf.HOST_MAIN
    }

    private fun handleIntent(intent: Intent?, restart: Boolean): Boolean {
        Timber.i("handleIntent: restart:$restart")
        var result = false
        //TODO push唤醒操作
        return result
    }

    private fun clearContentChildFragment() {
        val mainFragment = findFragment(MainFragment::class.java) ?: return
        val contentFragment = mainFragment.findChildFragment(ContentFragment::class.java)
        if (contentFragment != null) {
            if (contentFragment.findChildFragment(TabHostFragment::class.java) != null) {
                if (contentFragment.topChildFragment::class.java.simpleName != TabHostFragment::class.java.simpleName) {
                    contentFragment.topChildFragment.popTo(TabHostFragment::class.java, false)
                }
            } else {
                contentFragment.removeBackStackChangeListener()
                while (contentFragment.childFragmentManager.backStackEntryCount > 0) {
                    contentFragment.childFragmentManager.popBackStackImmediate()
                }
                window.decorView.post {
                    contentFragment.loadRootFragment(R.id.contentFragmentContainer, createTabHostFragment())
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (DisplayUtil.statusBarHeight == 0) {
            DisplayUtil.initDisplayOpinion(this)
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        NetworkManager.init(this.applicationContext)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        contentView?.removeCallbacks(delayInitAction)
        NetworkManager.unsubscribe()
        try {
            RxBus.get().unregister(this)
        } catch (e: IllegalArgumentException) {
            //捕获RxBus因为延迟register Activity导致在unregister时产生的异常
            Timber.e(e)
        }
        configViewModel.destroyConfig()
    }

    @Subscribe
    fun onFragmentCreatedEvent(event: FragmentCreatedEvent) {
        //TODO push唤醒操作
    }

    @Subscribe
    fun onCloseMainActivityEvent(event: CloseMainActivityEvent) {
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
