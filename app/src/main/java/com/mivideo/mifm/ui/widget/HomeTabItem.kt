package com.mivideo.mifm.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import com.mivideo.mifm.R

/**
 * 首页底部导航栏TAB item的控件
 *
 * @author LiYan
 */
class HomeTabItem : FrameLayout {
    private val mContext: Context
    private lateinit var iconImage: ImageView
    private lateinit var redDotImage: ImageView
    private lateinit var textView: TextView

    constructor(context: Context) : super(context) {
        mContext = context
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
        init()
    }

    constructor(context: Context,
                attrs: AttributeSet? = null,
                defStyle: Int = 0) : super(context, attrs, defStyle) {
        mContext = context
        init()
    }

    private fun init() {
        val layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.weight = 1f
        this.layoutParams = layoutParams

        val view = LayoutInflater.from(mContext).inflate(R.layout.layout_bottom_tab, null)
        iconImage = view.findViewById(R.id.iv_tab_icon)
        redDotImage = view.findViewById(R.id.iv_red_dot)
        textView = view.findViewById(R.id.tv_icon_text)

        addView(view)

    }

    fun loadImage(url: String) {
        Glide.with(context)
                .load(url)
                .priority(Priority.HIGH)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .override(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL)
                .dontTransform()
                .into(iconImage)
    }

    fun loadImage(drawableId: Int) {
        Glide.with(context)
                .load(drawableId)
                .priority(Priority.HIGH)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .override(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL)
                .dontTransform()
                .into(iconImage)
    }

    fun getTextView() : TextView {
        return textView
    }

    fun showRedDot(show: Boolean) {
        if (show) {
            redDotImage.visibility = View.VISIBLE
        } else {
            redDotImage.visibility = View.GONE
        }
    }

    fun setText(text: String) {
        textView.text = text
    }

    fun getText() : String {
        return textView.text.toString()
    }

    fun setDefaultTextColor() {
        textView.setTextColor(mContext.resources.getColor(R.color.common_black))
    }

    fun setCheckedTextColor() {
        textView.setTextColor(mContext.resources.getColor(R.color.themeColor))

    }

    fun updateWithAnim(body1: () -> Unit, body2: () -> Unit) {
        body1()

        val animationSet = AnimationSet(true)
        val scaleAnimation = ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f, (iconImage.width / 2).toFloat(), (iconImage.height / 2).toFloat())
        animationSet.addAnimation(scaleAnimation)
        animationSet.duration = 250
        animationSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                body2()

                /*val animationSet = AnimationSet(true)
                val scaleAnimation = ScaleAnimation(0.9f, 1.0f, 0.9f, 1.0f, (iconImage.width / 2).toFloat(), (iconImage.height / 2).toFloat())
                animationSet.addAnimation(scaleAnimation)
                animationSet.duration = 250
                iconImage.startAnimation(animationSet)*/
            }

            override fun onAnimationStart(animation: Animation?) {
            }

        })
        iconImage.startAnimation(animationSet)
    }
}
