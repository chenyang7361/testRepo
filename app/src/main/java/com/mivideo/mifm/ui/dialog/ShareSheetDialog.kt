package com.mivideo.mifm.ui.dialog

import android.app.Activity
import android.support.design.widget.BottomSheetDialog
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.MainApp
import com.mivideo.mifm.R
import com.mivideo.mifm.data.viewmodel.ConfigViewModel
import com.mivideo.mifm.share.ShareHelper
import com.mivideo.mifm.socialize.SocializeManager
import com.mivideo.mifm.socialize.share.NewShareInfo
import com.mivideo.mifm.util.app.showToast
import org.jetbrains.anko.onClick
import rx.Observable
import rx.lang.kotlin.BehaviorSubject

/**
 * 分享底部功能弹出框
 * @author LiYan
 */
class ShareSheetDialog(val activity: Activity) {
    var dialog: BottomSheetDialog = BottomSheetDialog(activity, R.style.BottomSheetDialog)
    private val context = activity.applicationContext
    private var shareInfo: NewShareInfo = NewShareInfo()
    private var socialManager: SocializeManager? = SocializeManager.get(activity)
    private val subject = BehaviorSubject<Boolean>()
    //    private val reportSheetDialog = ReportSheetDialog(activity)
//    private val dislikeSheetDialog = DislikeSheetDialog(activity)
    private val shareHelper = ShareHelper(socialManager!!, "ShareDialog")
    private val configViewModel: ConfigViewModel = (context as MainApp).kodein.instance()

    private var rootView: View = activity.layoutInflater.inflate(R.layout.share_dialog_layout, null)
    private var wbImage: ImageView? = null
    private var qqFriendImage: ImageView? = null
    private var qqSpaceImage: ImageView? = null
    private var wxFriendImage: ImageView? = null
    private var wxGroupImage: ImageView? = null
    //    private var downloadView: ViewGroup? = null
//    private var downloadBtn: DownloadView? = null
//    private var bannerImg: ImageView? = null
    private var listener: ShareSheetClickListener? = null
//    private var videoInfo: CommonVideo? = null
//    private var authorInfo: CommonAuthor? = null

    var isShow = dialog.isShowing

    init {
        dialog.setContentView(rootView)
        dialog.setOnDismissListener {
            subject.onNext(false)
        }
        dialog.setOnShowListener {
            subject.onNext(true)
        }
        if (rootView.parent is View) {
            var parent = rootView.parent as View
            parent.setBackgroundResource(R.color.palette_transparent)
        }
        wbImage = rootView.findViewById(R.id.sinaWeiBoImage)
        qqFriendImage = rootView.findViewById(R.id.qqFriendImage)
        qqSpaceImage = rootView.findViewById(R.id.qqSpaceImage)
        wxFriendImage = rootView.findViewById(R.id.wechatFriendImage)
        wxGroupImage = rootView.findViewById(R.id.wechatGroupImage)
//        bannerImg = rootView.findViewById(R.id.banner_img)

//        rootView.findViewById<View>(R.id.copyLinkLayout).onClick {
//            val handledClick = listener?.onClickCopyUrl() ?: false
//            if (handledClick) return@onClick
//            if (TextUtils.isEmpty(shareInfo.targetUrl)) {
//                showToast(context, context.resources.getString(R.string.share_not_supported))
//            } else {
//                shareHelper.copyUrl(shareInfo)
//            }
//            dismiss()
//        }
        rootView.findViewById<View>(R.id.shareCancelButton).onClick {
            dismiss()
        }
        rootView.findViewById<View>(R.id.qqFriendLayout).onClick {
            val handledClick = listener?.onClickQQ() ?: false
            if (handledClick) return@onClick
            shareHelper.shareQQ(shareInfo, object : ShareHelper.ShareHelperListener() {
                override fun onShareStart(): Boolean {
                    super.onShareStart()
                    dialog.dismiss()
                    return false
                }
            })
        }
        rootView.findViewById<View>(R.id.qqSpaceLayout).onClick {
            val handledClick = listener?.onClickQQZone() ?: false
            if (handledClick) return@onClick
            shareHelper.shareQQZone(shareInfo, object : ShareHelper.ShareHelperListener() {
                override fun onShareStart(): Boolean {
                    super.onShareStart()
                    dismiss()
                    return false
                }
            })
        }
        rootView.findViewById<View>(R.id.wechatFriendLayout).onClick {
            val handledClick = listener?.onClickWx() ?: false
            if (handledClick) return@onClick
            shareHelper.shareWx(shareInfo, object : ShareHelper.ShareHelperListener() {
                override fun onShareStart(): Boolean {
                    super.onShareStart()
                    dismiss()
                    return false
                }
            })
        }
        rootView.findViewById<View>(R.id.wechatGroupLayout).onClick {
            val handledClick = listener?.onClickWxMoments() ?: false
            if (handledClick) return@onClick
            shareHelper.shareWxMoments(shareInfo, object : ShareHelper.ShareHelperListener() {
                override fun onShareStart(): Boolean {
                    super.onShareStart()
                    dismiss()
                    return false
                }
            })
        }

        rootView.findViewById<View>(R.id.sinaWeiBoLayout).onClick {
            val handledClick = listener?.onClickWeibo() ?: false
            if (handledClick) return@onClick
            shareHelper.shareWeiBo(shareInfo, object : ShareHelper.ShareHelperListener() {
                override fun onShareStart(): Boolean {
                    super.onShareStart()
                    dismiss()
                    return false
                }
            })
        }

//        rootView.findViewById<View>(R.id.ll_dislike).onClick {
//            val handledClick = listener?.onClickDislike() ?: false
//            if (handledClick) return@onClick
//            dismiss()
//            dislikeSheetDialog.show(arrayOf("default tag"))
//        }

//        rootView.findViewById<View>(R.id.ll_report).onClick {
//            context.statistics(Statistics.ACTION.REPORT_ENTRACE_CLICK, Statistics.CATEGORY.DIALOG,
//                    getStatisticExt(videoInfo))
//            val handledClick = listener?.onClickReport() ?: false
//            if (handledClick) return@onClick
//            dismiss()
//            reportSheetDialog.show(videoInfo)
//        }
//        downloadView = rootView.findViewById<ViewGroup>(R.id.ll_download)
//        if (BuildConfig.ENABLE_VIDEO_CACHE) {
//            downloadView?.visibility = View.VISIBLE
//        } else {
//            downloadView?.visibility = View.GONE
//        }
//        downloadBtn = rootView.findViewById<DownloadView>(R.id.ll_download_btn)
//        downloadBtn?.onClick {
//            val ext = getStatisticExt(videoInfo)
//            ext?.put("nettype", NetworkParams.getNetworkType(context).toString()) // 网络类型: 0:未知,1:wifi,2:2G,3:3G,4:4G
//            context.statistics(Statistics.ACTION.DOWNLOAD_BTN_CLICK, Statistics.CATEGORY.DOWNLOAD, ext)
//            val handledClick = listener?.onClickDownload() ?: false
//            if (handledClick) return@onClick
//            dismiss()
//            if (videoInfo != null) {
//                downloadBtn?.setCommonVideoInfo(videoInfo!!)
//                downloadBtn?.downloadInOrder()
//            }
//        }

//        bannerImg?.onClick {
//            dismiss()
//            if (configViewModel.shareDialogBannerList.size > 0) {
//                ActionHandlerUtil.handleAction(activity, ConfigViewModel.getRealBannerAction(configViewModel.shareDialogBannerList[0]))
//            }
//        }
    }

    fun setClickListener(listener: ShareSheetClickListener) {
        this.listener = listener
    }

    /**
     * 设置分享的结果监听
     */
    fun setShareListener(listener: ShareHelper.ShareHelperListener) {
        shareHelper.addShareListener(listener)
    }

    /**
     * 监听弹窗状态变化，Show/Dismiss
     */
    fun observeShow(): Observable<Boolean> {
        return subject
    }

    /**
     * 显示分享弹框
     */
    fun show(shareInfo: NewShareInfo) {
        this.shareInfo = shareInfo
//        this.videoInfo = ShareHelper.getVideoFromShareInfo(shareInfo)
//        this.authorInfo = ShareHelper.getAuthorFromShareInfo(shareInfo)
        if (!dialog.isShowing) {
            updateLayout()
            dialog.show()
        }
    }

    private fun updateLayout() {
        if (shareInfo.shareSupport) {
            wxFriendImage!!.setImageResource(R.drawable.weixin_share)
            wxGroupImage!!.setImageResource(R.drawable.weixinf_share)
        } else {
            wxFriendImage!!.setImageResource(R.drawable.weixin_share_zhihui)
            wxGroupImage!!.setImageResource(R.drawable.weixinf_share_zhihui)
        }
        if (shareInfo.shareSupport) {
            qqFriendImage!!.setImageResource(R.drawable.qq_share)
            qqSpaceImage!!.setImageResource(R.drawable.zone_share)
        } else {
            qqFriendImage!!.setImageResource(R.drawable.qq_share_zhihui)
            qqSpaceImage!!.setImageResource(R.drawable.zone_share_zhihui)
        }
        if (shareInfo.shareSupport) {
            wbImage!!.setImageResource(R.drawable.weibo_share)
        } else {
            wbImage!!.setImageResource(R.drawable.weibo_share_zhihui)
        }
//        if (configViewModel.shareDialogBannerList.size > 0) {
//            bannerImg?.visibility = View.VISIBLE
//            Glide.with(context)
//                    .load(configViewModel.shareDialogBannerList[0].image_url)
//                    .priority(Priority.HIGH)
//                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
//                    .dontTransform()
//                    .into(bannerImg)
//        } else {
//            bannerImg?.visibility = View.GONE
//        }
    }

    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    /**
     * 设置下载按钮是否可见
     */
    fun setDownloadVisible(visible: Boolean) {
//        if (visible) {
//            downloadView?.visibility = View.VISIBLE
//        } else {
//            downloadView?.visibility = View.GONE
//        }
    }

    /**
     * 设置不敢兴趣按钮是否可见
     */
    fun setDislikeVisible(visible: Boolean) {
//        if (visible) {
//            rootView.findViewById<View>(R.id.ll_dislike).visibility = View.VISIBLE
//        } else {
//            rootView.findViewById<View>(R.id.ll_dislike).visibility = View.GONE
//        }
    }

    /**
     * 设置举报按钮是否可见
     */
    fun setReportVisible(visible: Boolean) {
//        if (visible) {
//            rootView.findViewById<View>(R.id.ll_report).visibility = View.VISIBLE
//        } else {
//            rootView.findViewById<View>(R.id.ll_report).visibility = View.GONE
//        }
    }


    open class ShareSheetClickListener {
        open fun onClickWx(): Boolean {
            return false
        }

        open fun onClickWxMoments(): Boolean {
            return false
        }

        open fun onClickQQ(): Boolean {
            return false
        }

        open fun onClickQQZone(): Boolean {
            return false
        }

        open fun onClickWeibo(): Boolean {
            return false
        }

        open fun onClickCopyUrl(): Boolean {
            return false
        }

        open fun onClickDislike(): Boolean {
            return false
        }

        open fun onClickReport(): Boolean {
            return false
        }

        open fun onClickDownload(): Boolean {
            return false
        }
    }

}