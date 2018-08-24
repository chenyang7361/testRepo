package com.mivideo.mifm.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.support.v4.widget.NestedScrollView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.salomonbrys.kodein.instance
import com.hwangjr.rxbus.annotation.Subscribe
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.jsondata.AlbumInfo
import com.mivideo.mifm.data.models.jsondata.MediaDetailData
import com.mivideo.mifm.data.repositories.CollectRepository
import com.mivideo.mifm.events.*
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.share.ShareHelper
import com.mivideo.mifm.ui.card.DetailDescribeCard
import com.mivideo.mifm.ui.card.DetailHeaderContainerCard
import com.mivideo.mifm.ui.card.DetailListCard
import com.mivideo.mifm.ui.card.DetailRecommendCard
import com.mivideo.mifm.ui.dialog.ShareSheetDialog
import com.mivideo.mifm.util.ScreenUtil
import com.mivideo.mifm.util.app.DisplayUtil
import com.mivideo.mifm.util.getColor
import com.mivideo.mifm.viewmodel.MediaDetailViewModel
import com.trello.rxlifecycle.FragmentEvent
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.subscribe_title_layout.*
import org.jetbrains.anko.onClick
import qiu.niorgai.StatusBarCompat

/**
 * Created by Jiwei Yuan on 18-7-30.
 */

fun BaseFragment.showStatusBar(context: Activity) {
    val decorView = context.window.decorView
    val option = View.SYSTEM_UI_FLAG_VISIBLE
    decorView.systemUiVisibility = option
    StatusBarCompat.setStatusBarColor(context, getColor(context,R.color.white))
    DisplayUtil.setStatusBarLightMode(context, true)
}

class MediaDetailFragment : BaseFragment() {
    private lateinit var detailViewModel: MediaDetailViewModel
    private var id: String = ""
    private val repository: CollectRepository by instance()
    private var isCollecting: Boolean = false
    private var collected: Boolean = false

    private val header: DetailHeaderContainerCard by lazy {
        DetailHeaderContainerCard(context)
    }

    private val list: DetailListCard by lazy {
        DetailListCard(context)
    }

    private val desc: DetailDescribeCard by lazy {
        DetailDescribeCard(context)
    }

    private val recommend: DetailRecommendCard by lazy {
        DetailRecommendCard(context)
    }

    private lateinit var shareDialog: ShareSheetDialog

    private var data: MediaDetailData? = null

    override fun onStop() {
        super.onStop()
        val decorView = (context as Activity).window.decorView
        val option = View.SYSTEM_UI_FLAG_VISIBLE
        decorView.systemUiVisibility = option
        StatusBarCompat.setStatusBarColor(context as Activity, resources.getColor(R.color.white))
        DisplayUtil.setStatusBarLightMode(context as Activity, true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        left.onClick {
            pop()
        }
        id = arguments?.getString("id") ?: ""
        detailViewModel = MediaDetailViewModel(context)
        initViews()
    }

    private fun initViews() {
        left.setImageResource(R.drawable.icon_back)
        title.text = ""
        bgBlank.setBackgroundResource(R.color.transparent)
        bgBlank.alpha = 1f
        title.visibility = View.VISIBLE

        val layoutParams = bgBlank.layoutParams
        layoutParams.height += ScreenUtil.getStatusBarHeight(context)
        bgBlank.layoutParams = layoutParams

        llTitle.layoutParams

        scrollView.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener {
            override fun onScrollChange(v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
                if (scrollY >= header.getView().height - llTitle.height) {
                    bgBlank.setBackgroundResource(R.color.white)
                    bgBlank.alpha = 1f
                    data?.let {
                        title.text = data!!.title
                    }
                    scrollView.invalidate()
                } else if (scrollY < header.getView().height) {
                    bgBlank.alpha = scrollY.toFloat() / header.getView().height
                    if (llTitle.alpha == 0f) {
                        bgBlank.setBackgroundResource(R.color.transparent)
                        bgBlank.alpha = 1f
                    } else {
                        bgBlank.setBackgroundResource(R.color.white)
                    }
                    title.text = ""
                    StatusBarCompat.translucentStatusBar(context as Activity, true)
                }
            }
        })
        container.addView(header.getView(), ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        container.addView(list.getView())
        container.addView(desc.getView())
        container.addView(recommend.getView())
        shareDialog = ShareSheetDialog(activity as Activity)

        detailViewModel.loadDetailData(id)
                .compose(asyncSchedulers())
                .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe({
                    data = it.data
                    header.setData(it.data!!)
                    header.setCollected(collected)
                    list.setData(it.data!!)
                    desc.setData(it.data!!.desc)
                }, {

                })
        repository.isCollectMedia(id)
                .compose(asyncSchedulers())
                .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe({
                    collected = it.data?.is_marked!!
                    header.setCollected(collected)
                }, {
                })
        detailViewModel.loadRecommendData(id)
                .compose(asyncSchedulers())
                .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe({
                    if (it.albums != null && !it.albums!!.isEmpty()) {
                        recommend.addData(it.albums!!)
                    }
                }, {
                })
        initMiniPlayer()
    }

    private fun initMiniPlayer() {
        if (mediaManager.isPlaying()) {
            miniPlayer.switchToPlay()
        }
    }

    @Subscribe
    fun onMediaPrepared(event: MediaPreparedEvent) {
        miniPlayer.switchToPlay()
        if (data!!.id.equals(event.albumId)) {
            list.updata()
        }
    }

    @Subscribe
    fun onMediaComplete(event: MediaCompleteEvent) {
        miniPlayer?.switchToPause()
    }

    @Subscribe
    fun onPassageClicked(event: DetailPassageClickedEvent) {
        mediaManager.playAlbum(data!!, event.position, data!!.sections, 1)
    }

    override fun onSupportVisible() {
        super.onSupportVisible()
        StatusBarCompat.translucentStatusBar(context as Activity, true)
    }

    @Subscribe
    fun onShareDetail(event: ShareDetailEvent) {
        val shareInfo = ShareHelper.buildShareInfo(event.data)
        shareDialog.show(shareInfo)
    }

    @Subscribe
    fun onCollectDetail(event: CollectDetailEvent) {
        if (isCollecting) {
            return
        }
        isCollecting = true
        repository.collectMedia(data!!)
                .compose(asyncSchedulers())
                .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe({
                    if (it.code == 1 && it.data?.status == "ok") {
                        header.setCollected(true)
                    }
                    isCollecting = false
                }, {
                    isCollecting = false
                })
    }

    @Subscribe
    fun onUnCollectDetail(event: UnCollectDetailEvent) {
        if (isCollecting) {
            return
        }
        isCollecting = true
        repository.unCollectMedia(event.id)
                .compose(asyncSchedulers())
                .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe({
                    if (it.code == 1 && it.data?.status == "ok") {
                        header.setCollected(false)
                    }
                    isCollecting = false
                }, {
                    isCollecting = false
                })
    }
}

fun createMediaDetailFragment(id: String): MediaDetailFragment {
    val fragment = MediaDetailFragment()
    val bundle = Bundle()
    bundle.putString("id", id)
    fragment.arguments = bundle
    return fragment
}