package com.mivideo.mifm

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.viewmodel.SplashViewModel
import org.jetbrains.anko.find
import timber.log.Timber

class SplashActivity : BaseActivity(), KodeinInjected {
    private companion object {
        val DEFAULT_SPLASH_LOAD_TIME: Long = 2000
    }

    private lateinit var mMineContainer: FrameLayout
    private lateinit var mSplashImageView: ImageView
    private lateinit var splashBgImageView: ImageView
    private var timeOut: Boolean = false

    override val injector = KodeinInjector()
    private lateinit var mHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)
        initView()
        initData()
        inject(appKodein())
        mHandler = Handler()
        mHandler.postDelayed({ startMainActivity() }, DEFAULT_SPLASH_LOAD_TIME)
    }

    private fun initData() {
        val splashViewModel = SplashViewModel(this)
        //TODO 切换真实用户信息
        splashViewModel.getSplash("UID", "TOKEN")
                .compose(asyncSchedulers())
                .subscribe({
                    Timber.i("url=${it?.data?.url}")
                    it?.data?.url.let {
                        if (!timeOut) {
                            Timber.i("load splash img")
                            Glide.with(this)
                                    .load(it)
                                    .priority(Priority.HIGH)
                                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                    .dontTransform()
                                    .into(splashBgImageView)
                        }
                    }
                }, {
                    Timber.i(it)
                })
    }

    private fun initView() {
        mMineContainer = find<FrameLayout>(R.id.mineContainer)
        mSplashImageView = find<ImageView>(R.id.splashImageView)
        splashBgImageView = find<ImageView>(R.id.splashBgImageView)
    }

    private fun startMainActivity() {
        timeOut = true
        window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        val intent = Intent()
        intent.setClass(this, MainActivity::class.java)
        startActivityAndFinish(intent)
    }

    private fun startActivityAndFinish(intent: Intent) {
        mHandler.removeCallbacksAndMessages(null)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
