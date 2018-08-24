package com.mivideo.mifm.player

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mivideo.mifm.R
import com.mivideo.mifm.ui.widget.BrightTransform
import org.jetbrains.anko.backgroundResource
import timber.log.Timber

/**
 * 播放器控件抽象实现
 * @author LiYan
 */
abstract class AbstractVideoControllerView : FrameLayout, VideoControllerView {
    protected lateinit var mContext: Context
    protected var viewController: VideoController? = null

    private var inflater = LayoutInflater.from(context)
    private var mGestureDetector: GestureDetectorCompat? = null


    private lateinit var mLoadingGroup: View
    private lateinit var mVideoCover: ImageView
    private lateinit var mLoadingView: View
    private lateinit var mLoadingTitleView: TextView
    private lateinit var mProgressBar: ProgressBar
    protected var mPlayerInfoView: LinearLayout? = null
    private var mNoNextVideoView: LinearLayout? = null
    private var mPlayerInfoConfirm: LinearLayout? = null
    private var mPlayerInfoLeftIcon: ImageView? = null
    private var mPlayerInfoRightIcon: ImageView? = null
    private var mPlayerInfoBtnText: TextView? = null
    private var mPlayerInfoHint: TextView? = null

    protected val topInAnim = AnimationUtils.loadAnimation(context, R.anim.view_translation_top_in)
    protected val topOutAnim = AnimationUtils.loadAnimation(context, R.anim.view_translation_top_out)
    protected val bottomInAnim = AnimationUtils.loadAnimation(context, R.anim.view_translation_bottom_in)
    protected val bottomOutAnim = AnimationUtils.loadAnimation(context, R.anim.view_translation_bottom_out)
    protected val leftOutAnim = AnimationUtils.loadAnimation(context, R.anim.view_translation_left_out)
    protected val leftInAnim = AnimationUtils.loadAnimation(context, R.anim.view_translation_left_in)
    protected val rightOutAnim = AnimationUtils.loadAnimation(context, R.anim.view_translation_right_out)
    protected val rightInAnim = AnimationUtils.loadAnimation(context, R.anim.view_translation_right_in)

    protected val fadeInAnim = AnimationUtils.loadAnimation(context, R.anim.fade_in)
    protected val fadeOutAnim = AnimationUtils.loadAnimation(context, R.anim.fade_out)

    protected var mPlayerView: ViewGroup? = null
    protected var mControllerViewContainer: ViewGroup? = null

    protected var mPlayerUIListener: PlayerUIListener? = null
    protected val mSeekListener: SeekBar.OnSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {

        override fun onStartTrackingTouch(bar: SeekBar) {
            viewController?.onSeekStart()
        }

        override fun onProgressChanged(bar: SeekBar, progress: Int,
                                       fromuser: Boolean) {

            viewController?.onSeeking(progress, fromuser)
        }

        override fun onStopTrackingTouch(bar: SeekBar) {
            viewController?.onSeekEnd()
        }
    }


    @Suppress("ConvertSecondaryConstructorToPrimary")
    @JvmOverloads
    constructor(context: Context,
                attrs: AttributeSet? = null,
                defStyle: Int = 0) : super(context, attrs, defStyle) {
        init(context)
    }

    private fun init(context: Context?) {
        mContext = context!!
        mGestureDetector = GestureDetectorCompat(mContext, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent?): Boolean {
                return this@AbstractVideoControllerView.onDoubleTap(e)
            }

            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                return this@AbstractVideoControllerView.onSingleTapConfirmed(e)
            }

            override fun onDown(e: MotionEvent?): Boolean {
                return true
            }

        })

        val view = inflater.inflate(R.layout.view_abstract_video_controller, null)
        val customView = this.onCreateView()
        val customViewContainer = view.findViewById<FrameLayout>(R.id.controlViewContainer)
        customViewContainer.addView(customView)
        this.addView(view)
        initView(view)
        onViewCreated()
    }

    open fun onDoubleTap(e: MotionEvent?): Boolean {
        if (mPlayerUIListener?.onDoubleClick() != true) {
            viewController?.togglePause()
        }
        return true
    }

    open fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        if (mPlayerUIListener?.onSingleClick() != true) {
            viewController?.toggleVisible()
        }
        return true
    }


    private fun initView(view: View) {
        mLoadingGroup = view.findViewById(R.id.loadingView)
        mVideoCover = view.findViewById(R.id.iv_player_view_cover)
        mLoadingView = view.findViewById(R.id.videoLoadingView)
        mLoadingTitleView = view.findViewById(R.id.videoNextView)

        mProgressBar = view.findViewById(R.id.video_controller_progressBar)
        mProgressBar.max = 1000

        mPlayerInfoView = view.findViewById(R.id.ll_player_info)
        mPlayerInfoConfirm = view.findViewById(R.id.ll_player_info_confirm_btn)
        mPlayerInfoLeftIcon = view.findViewById(R.id.iv_player_info_left_icon)
        mPlayerInfoRightIcon = view.findViewById(R.id.iv_player_info_right_icon)
        mPlayerInfoBtnText = view.findViewById(R.id.tv_player_info_confirm)
        mPlayerInfoHint = view.findViewById(R.id.tv_player_info_hint)
        mNoNextVideoView = view.findViewById(R.id.ll_player_list_complete)
    }


    override fun setPlayerUIListener(listener: PlayerUIListener?) {
        this.mPlayerUIListener = listener
    }

    /**
     * 子类需要实现此方法，返回自定义的view
     */
    abstract fun onCreateView(): View

    /**
     * view创建成功回调
     */
    abstract fun onViewCreated()


    protected fun fadeInView(view: View) {
        if (view.visibility != View.VISIBLE) {
            view.animate().cancel()
            view.visibility = View.VISIBLE
            val animation = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)
            animation.duration = 300
            animation.start()
        }
    }

    protected fun fadeOutView(view: View) {
        if (view.visibility == View.VISIBLE) {
            view.animate().cancel()
            val animation = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f)
            animation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    view.visibility = View.GONE
                }
            })
            animation.duration = 300
            animation.start()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mGestureDetector!!.onTouchEvent(event)) {
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun setController(controller: VideoController) {
        this.viewController = controller
    }

    override fun getController(): VideoController = this.viewController!!

    override fun hideProgressBar() {
        mProgressBar.visibility = View.INVISIBLE
    }

    override fun showProgressBar(percent: Int, bufferingPercent: Int) {
        mProgressBar.visibility = View.VISIBLE
        mProgressBar.progress = percent
        mProgressBar.secondaryProgress = bufferingPercent
    }

    override fun showProgressBar() {
        mProgressBar.visibility = View.VISIBLE
    }

    override fun changePlayerSizeTo(state: PlayerSizeMode) {

    }

    override fun showNoNextVideoLayout() {
        mNoNextVideoView?.visibility = View.VISIBLE
    }

    override fun hideNoNextVideoLayout() {
        mNoNextVideoView?.visibility = View.GONE
    }

    override fun showNoNetworkLayout() {
        Timber.i("showNoNetworkLayout")
        mPlayerInfoView?.visibility = View.VISIBLE
        mPlayerInfoLeftIcon?.visibility = View.VISIBLE
        mPlayerInfoBtnText?.text = context.getString(R.string.retry)
        mPlayerInfoRightIcon?.visibility = View.GONE
        mPlayerInfoHint?.text = context.getString(R.string.tip_no_net)
        mPlayerInfoView?.setOnClickListener {
            viewController?.clickNoNetworkRetry()
        }
    }

    override fun hideNoNetworkLayout() {
        Timber.i("hideNoNetworkLayout")
        mPlayerInfoView?.visibility = View.GONE
        mPlayerInfoView?.setOnClickListener(null)
    }

    override fun showUseMobileNetLayout() {
        Timber.i("showUseMobileLayout")
        mPlayerInfoView?.visibility = View.VISIBLE
        mPlayerInfoLeftIcon?.visibility = View.GONE
        mPlayerInfoBtnText?.text = context.getString(R.string.text_continue)
        mPlayerInfoRightIcon?.visibility = View.VISIBLE
        mPlayerInfoHint?.text = context.getString(R.string.use_mobile_network_ask_continue)
        mPlayerInfoView?.setOnClickListener {
            viewController?.clickUseMobileNetContinue()
        }
    }

    override fun hideUseMobileNetLayout() {
        Timber.i("hideUseMobileLayout")
        mPlayerInfoView?.visibility = View.GONE
        mPlayerInfoView?.setOnClickListener(null)
    }


    override fun showErrorView(message: String) {
        Timber.i("showErrorView")
        mPlayerInfoView?.visibility = View.VISIBLE
        mPlayerInfoLeftIcon?.visibility = View.VISIBLE
        mPlayerInfoBtnText?.text = context.getString(R.string.retry)
        mPlayerInfoRightIcon?.visibility = View.GONE
        mPlayerInfoHint?.text = message
        mPlayerInfoView?.setOnClickListener {
            viewController?.clickPlayErrorRetry()
        }
    }

    override fun hideErrorView() {
        Timber.i("hideErrorView")
        mPlayerInfoView?.visibility = View.GONE
        mPlayerInfoView?.setOnClickListener(null)
    }

    override fun showLoadingView(title: String, transparentBackground: Boolean) {
        mLoadingGroup.visibility = View.VISIBLE
        mVideoCover.visibility = View.GONE
        if (transparentBackground) {
            mLoadingGroup.backgroundResource = android.R.color.transparent
        } else {

            mLoadingGroup.backgroundResource = android.R.color.black
        }

        mLoadingTitleView.text = title
    }

    override fun showLoadingView(title: String, coverUrl: String) {
        mLoadingGroup.visibility = View.VISIBLE
        mLoadingGroup.backgroundResource = android.R.color.transparent
        mVideoCover.visibility = View.VISIBLE
        Glide.with(context.applicationContext)
                .load(coverUrl)
                .crossFade(300)
                .priority(Priority.HIGH)
                .bitmapTransform(BrightTransform(context.applicationContext))
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(mVideoCover)
        mLoadingTitleView.text = title
    }

    override fun hideLoadingView() {
        mLoadingGroup.visibility = View.GONE
    }

    override fun showPlayBtn() {

    }

    override fun hidePlayBtn() {

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelAnimation()
        cancelAnimation()
    }

    override fun cancelAnimation() {
        clearAnimation()

        topInAnim.cancel()
        topInAnim.setAnimationListener(null)

        topOutAnim.cancel()
        topOutAnim.setAnimationListener(null)

        bottomInAnim.cancel()
        bottomInAnim.setAnimationListener(null)

        bottomOutAnim.cancel()
        topInAnim.setAnimationListener(null)

        fadeInAnim.cancel()
        fadeInAnim.setAnimationListener(null)

        fadeOutAnim.cancel()
        fadeOutAnim.setAnimationListener(null)

    }

    override fun showAdjustVolumeLayout(newValue: Int, volumeValue: String) {

    }

    override fun showBrightAdjustLayout(brightValue: String) {

    }

    override fun showVideoTitle(videoTitle: String) {
    }

    override fun hideVideoTitle() {
    }

    override fun renderSeekLayout(seekBarProgress: Int, bufferingProgress: Int, durationText: String, positionText: String) {
    }

    override fun showBottomControlView() {
    }

    override fun hideBottomControlView() {
    }

    override fun renderPlayTimeText(text: String) {
    }

    override fun showFastGroupView(setShow: Boolean) {
    }

    override fun showNextBtn(setShow: Boolean) {
    }

    override fun showLockView(setShow: Boolean) {
    }

    override fun showUnLockView(setShow: Boolean) {
    }

    override fun setSeekBarEnable(enable: Boolean) {
    }

    override fun renderFastMoveLayout(fastFaward: Boolean, durationText: String, seekText: String) {
    }

    override fun renderVideoTitle(videoTitle: String) {
    }

    override fun renderPlayBtn(isPlaying: Boolean) {

    }

    override fun hideResolutionPicker() {

    }

    override fun renderResolutionText(text: String, enable: Boolean) {

    }

    override fun showShadow() {
        Timber.i("showShadow")
        this.setBackgroundColor(context.resources.getColor(R.color.defaultImageLayerColor))
    }

    override fun hideShadow() {
        Timber.i("hideShadow")
        this.setBackgroundColor(context.resources.getColor(android.R.color.transparent))
    }


    fun attachPlayerView(playerView: ViewGroup, playerContainer: ViewGroup) {
        mPlayerView = playerView
        mControllerViewContainer = playerContainer
    }


}