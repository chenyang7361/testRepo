package com.mivideo.mifm.ui.widget

import android.content.Context
import android.content.res.TypedArray
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.mivideo.mifm.R
import com.mivideo.mifm.extensions.tryWith
import org.jetbrains.anko.textColor

/**
 * Created by aaron on 2017/2/9.
 * 自定义ActionBar组件
 *
 * @author LiYan(modify)
 */
class SimpleTitleBar : FrameLayout {
    var backLayout: LinearLayout? = null

    private var flLeft: FrameLayout? = null
    private var flRight: FrameLayout? = null
    private var tvRight: TextView? = null
    private var ivRight: ImageView? = null
    private var tvMiddle: TextView? = null
    private var tvLeft: TextView? = null
    private var ivLeft: ImageView? = null
    private var ivArrowBack: ImageView? = null


    constructor(context: Context) : super(context) {

        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        init(context, attrs)
    }

    private fun init(context: Context?, attrs: AttributeSet?) {
        if (context == null) {
            return
        }
//        setBackgroundColor(Color.WHITE)
        val rootView = LayoutInflater.from(context).inflate(R.layout.widget_titlebar, null)
        val height = context.resources.getDimension(R.dimen.commentToolbarSize)
        val layoutParams: ViewGroup.LayoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height.toInt())
        this.addView(rootView, layoutParams)

        initView(rootView)

        /**
         * 初始化自定义属性
         */
        if (attrs != null) {
            var ta: TypedArray? = null
            try {
                ta = context.obtainStyledAttributes(attrs, R.styleable.SimpleTitleBar)

                tryWith {
                    val leftTitleText = ta.getString(R.styleable.SimpleTitleBar_leftTitle)
                    if (!TextUtils.isEmpty(leftTitleText)) {
                        tvLeft?.text = leftTitleText
                    }
                }
                tryWith {
                    val leftTitleColor = ta.getColor(R.styleable.SimpleTitleBar_leftTitleColor, tvLeft!!.textColor)
                    tvLeft?.setTextColor(leftTitleColor)
                }
                tryWith {
                    val showBackArrow: Boolean = ta.getBoolean(R.styleable.SimpleTitleBar_showBackArrow, true)
                    if (showBackArrow) {
                        ivArrowBack?.visibility = View.VISIBLE
                    } else {
                        ivArrowBack?.visibility = View.GONE
                    }
                }
                tryWith {
                    val leftResId = ta.getResourceId(R.styleable.SimpleTitleBar_leftDrawable, -1)
                    val leftDrawable = ContextCompat.getDrawable(context, leftResId)
                    if (leftDrawable != null) {
                        backLayout?.visibility = View.GONE
                        ivLeft?.visibility = View.VISIBLE
                        ivLeft?.setImageDrawable(leftDrawable)
                    } else {
                        backLayout?.visibility = View.VISIBLE
                        ivLeft?.visibility = View.GONE
                    }
                }

                tryWith {
                    val middleTitleText = ta.getString(R.styleable.SimpleTitleBar_middleTitle)
                    val middleTitleColor = ta.getColor(R.styleable.SimpleTitleBar_middleTitleColor,
                            tvMiddle!!.currentTextColor)
                    if (!TextUtils.isEmpty(middleTitleText)) {
                        tvMiddle?.visibility = View.VISIBLE
                        tvMiddle?.text = middleTitleText
                        tvMiddle?.setTextColor(middleTitleColor)
                    }
                }


                tryWith {
                    val rightTitleText = ta.getString(R.styleable.SimpleTitleBar_rightTitle)
                    if (!TextUtils.isEmpty(rightTitleText)) {
                        flRight?.visibility = View.VISIBLE
                        tvRight?.text = rightTitleText
                    }
                }
                tryWith {
                    val rightTitleColor = ta.getColor(R.styleable.SimpleTitleBar_rightTitleColor,
                            tvRight!!.currentTextColor)
                    tvRight!!.setTextColor(rightTitleColor)
                }
                tryWith {
                    val rightResId = ta.getResourceId(R.styleable.SimpleTitleBar_rightDrawable, -1)
                    val rightDrawable = ContextCompat.getDrawable(context, rightResId)
                    if (rightDrawable != null) {
                        flRight?.visibility = View.VISIBLE
                        tvRight?.visibility = View.GONE
                        ivRight?.visibility = View.VISIBLE
                        ivRight?.setImageDrawable(rightDrawable)
                    } else {
                        tvRight?.visibility = View.VISIBLE
                        ivRight?.visibility = View.GONE
                    }
                }


            } finally {
                ta?.recycle()

            }

        }
    }

    private fun initView(rootView: View) {
        backLayout = rootView.findViewById(R.id.backLayout)

        flRight = rootView.findViewById(R.id.fl_titleBar_right)
        tvRight = rootView.findViewById(R.id.tv_titleBar_right)
        ivRight = rootView.findViewById(R.id.iv_titleBar_right)

        tvMiddle = rootView.findViewById(R.id.tv_titleBar_middle_title)

        flLeft = rootView.findViewById(R.id.fl_titleBar_left)
        tvLeft = rootView.findViewById(R.id.tv_titleBar_left)
        ivLeft = rootView.findViewById(R.id.iv_titleBar_left)
        ivArrowBack = rootView.findViewById(R.id.iv_titleBar_back_arrow)
    }

    fun setText(text: String) {
        tvLeft?.text = text
    }

    fun showMiddleTitle(text: String) {
        tvMiddle?.text = text
        tvMiddle?.visibility = View.VISIBLE
    }

    /**
     * 设置TitleBar右边按钮点击事件
     */
    fun setRightClickListener(listener: View.OnClickListener) {
        flRight?.setOnClickListener(listener)
    }

    /**
     * 设置TitleBar左边按钮点击事件
     */
    fun setLeftClickListener(listener: View.OnClickListener) {
        flLeft?.setOnClickListener(listener)
    }

    fun showBackArrow(show: Boolean) {
        if (show) {
            ivArrowBack?.visibility = View.VISIBLE
        } else {
            ivArrowBack?.visibility = View.GONE
        }
    }

    fun showRightBtn(show: Boolean) {
        if (show) {
            flRight?.visibility = View.VISIBLE
        } else {
            flRight?.visibility = View.GONE
        }
    }


}
