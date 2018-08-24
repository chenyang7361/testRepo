package com.mivideo.mifm.ui.widget.freshrecyclerview

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.hwangjr.rxbus.RxBus
import com.mivideo.mifm.R
import com.mivideo.mifm.data.viewmodel.ConfigViewModel
import com.mivideo.mifm.rx.asyncSchedulers
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.api.RefreshKernel
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.constant.SpinnerStyle
import rx.subscriptions.CompositeSubscription

/**
 * Created by aaron on 2017/12/14.
 */
class KRefreshHeader : LinearLayout, RefreshHeader, KodeinInjected {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        RxBus.get().register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        RxBus.get().unregister(this)
        compositeSubscription.clear()
    }

    override val injector = KodeinInjector()
    private lateinit var operationImage: ImageView
    private lateinit var operationLayout: LinearLayout
    private lateinit var loadingLayout: LinearLayout
    private val configViewModel: ConfigViewModel by instance()
    private var isNeedShowAd: Boolean = false
    private val compositeSubscription = CompositeSubscription()

    private fun init() {
        inject(appKodein())
        val view = LayoutInflater.from(context).inflate(R.layout.refresh_default_header, this, true)
        operationImage = view.findViewById(R.id.operationImage)
        operationLayout = view.findViewById(R.id.operationLayout)
        loadingLayout = view.findViewById(R.id.loadingLayout)
        operationLayout.visibility = View.GONE
        loadingLayout.visibility = View.VISIBLE

        showAd()
    }

    fun setNeedShowAd(show: Boolean) {
        isNeedShowAd = show
        showAd()
    }

    private fun showAd() {
        if (isNeedShowAd) {
            val imageUrl = configViewModel.getTopPullImage()
            if (!TextUtils.isEmpty(imageUrl)) {
                Glide.with(context)
                        .load(imageUrl)
                        .priority(Priority.NORMAL)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(object : GlideDrawableImageViewTarget(operationImage) {
                            override fun onResourceReady(resource: GlideDrawable?, animation: GlideAnimation<in GlideDrawable>?) {
                                super.onResourceReady(resource, animation)
                                if (resource != null) {
                                    operationImage.setImageDrawable(resource)
                                    operationLayout.visibility = View.VISIBLE
                                    loadingLayout.visibility = View.GONE
                                } else {
                                    operationLayout.visibility = View.GONE
                                    loadingLayout.visibility = View.VISIBLE
                                }
                            }
                        })
            } else {
                operationLayout.visibility = View.GONE
                loadingLayout.visibility = View.VISIBLE
            }

//            configViewModel.observePullImageUpdate()
//                    .compose(asyncSchedulers())
//                    .subscribe({ imageUrl ->
//                        if (!TextUtils.isEmpty(imageUrl)) {
//                            Glide.with(context)
//                                    .load(imageUrl)
//                                    .priority(Priority.NORMAL)
//                                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
//                                    .into(object : GlideDrawableImageViewTarget(operationImage) {
//                                        override fun onResourceReady(resource: GlideDrawable?, animation: GlideAnimation<in GlideDrawable>?) {
//                                            super.onResourceReady(resource, animation)
//                                            if (resource != null) {
//                                                operationImage.setImageDrawable(resource)
//                                                operationLayout.visibility = View.VISIBLE
//                                                loadingLayout.visibility = View.GONE
//                                            } else {
//                                                operationLayout.visibility = View.GONE
//                                                loadingLayout.visibility = View.VISIBLE
//                                            }
//                                        }
//                                    })
//                        } else {
//                            operationLayout.visibility = View.GONE
//                            loadingLayout.visibility = View.VISIBLE
//                        }
//                    }, {}).addTo(compositeSubscription)
        }
    }

    /**
     * 下拉状态回调
     */
    override fun onPullingDown(v: Float, i: Int, i1: Int, i2: Int) {
    }

    /**
     * Loading状态回调
     */
    override fun onReleasing(v: Float, i: Int, i1: Int, i2: Int) {
    }

    /**
     * 释放状态回调
     */
    override fun onRefreshReleased(refreshLayout: RefreshLayout, i: Int, i1: Int) {
    }

    /**
     * 显示布局
     */
    override fun getView(): View {
        return this
    }

    override fun getSpinnerStyle(): SpinnerStyle? {
        return SpinnerStyle.Translate
    }

    override fun setPrimaryColors(vararg ints: Int) {
    }

    override fun onInitialized(refreshKernel: RefreshKernel, i: Int, i1: Int) {
    }

    override fun onHorizontalDrag(v: Float, i: Int, i1: Int) {
    }

    override fun onStartAnimator(refreshLayout: RefreshLayout, i: Int, i1: Int) {
    }

    override fun onFinish(refreshLayout: RefreshLayout, b: Boolean): Int {
        return 0
    }

    override fun isSupportHorizontalDrag(): Boolean {
        return false
    }

    override fun onStateChanged(refreshLayout: RefreshLayout, refreshState: RefreshState, refreshState1: RefreshState) {}
}
