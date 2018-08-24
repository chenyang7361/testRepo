package com.mivideo.mifm.ui.widget

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.mivideo.mifm.R
import com.mivideo.mifm.util.ScreenUtil
import org.jetbrains.anko.textColor
import java.util.ArrayList

/**
 * 页面加载自定义View
 * @author LiYan
 */
class LoadingView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {
    companion object {
        const val LOADING_TYPE_TEXT = 0
        const val LOADING_TYPE_IMAGE = 1

    }

    private var mAnimatorSet: AnimatorSet? = null
    private var viewList: ArrayList<View> = ArrayList()

    /**
     * 动画间隔
     */
    private val interval = 500L

    init {
        initView(context, attrs, defStyleAttr)
    }

    private var loadingType = 0
    private var text: String = ""

    private fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val typeArray = context.theme.obtainStyledAttributes(attrs, R.styleable.LoadingView, 0, 0)
        try {
            loadingType = typeArray.getInt(R.styleable.LoadingView_loadingType, LOADING_TYPE_TEXT)
        } finally {
            typeArray.recycle()
        }
        gravity = Gravity.CENTER
        orientation = HORIZONTAL
//        addTextView(context, attrs)
        addImageView(context, attrs)
        showLoading()
    }

    private fun addTextView(context: Context, attrs: AttributeSet?) {
        val text = "小米快视频"
        text.forEach {
            val textView = createTextView(context, attrs)
            textView.text = it.toString()
            addView(textView)
            viewList.add(textView)
        }
    }

    private fun addImageView(context: Context, attrs: AttributeSet?) {
        val imageView = ImageView(context, attrs)
        val layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        imageView.layoutParams = layoutParams
        imageView.setImageResource(R.drawable.icon_loading)
        viewList.add(imageView)

        addView(imageView)

    }

    private fun createTextView(context: Context, attrs: AttributeSet?): TextView {
        val textView = TextView(context, attrs)
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        textView.layoutParams = layoutParams
        textView.textColor = Color.parseColor("#55A7A9AE")
        textView.textSize = ScreenUtil.dip2px(context, 21f).toFloat()
        textView.paint.isFakeBoldText = true
        textView.paint.typeface = Typeface.SERIF
        return textView
    }

    fun showLoading() {
        if (visibility != View.VISIBLE)
            return
        if (mAnimatorSet == null)
            initAnimation()
        if (mAnimatorSet!!.isRunning || mAnimatorSet!!.isStarted)
            return
        mAnimatorSet!!.start()
    }

    fun hideLoading() {
        if (mAnimatorSet == null) {
            return
        }
        if (!mAnimatorSet!!.isRunning && !mAnimatorSet!!.isStarted) {
            return
        }
        mAnimatorSet!!.removeAllListeners()
        mAnimatorSet!!.cancel()
        mAnimatorSet!!.end()
    }

    private fun initAnimation() {
        mAnimatorSet = AnimatorSet()
        val animatorList = ArrayList<Animator>()
        val length = viewList.size - 1
        for (i in 0..length) {
            val loadAnimator = ObjectAnimator.ofFloat(viewList[i], "alpha", 1.0f, 0.5f).setDuration(interval)
            loadAnimator.startDelay = (100 * i).toLong()
            loadAnimator.repeatMode = ObjectAnimator.REVERSE
            loadAnimator.repeatCount = -1
            animatorList.add(loadAnimator)
        }
        mAnimatorSet!!.playTogether(animatorList)
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility != View.VISIBLE)
            hideLoading()
    }
}