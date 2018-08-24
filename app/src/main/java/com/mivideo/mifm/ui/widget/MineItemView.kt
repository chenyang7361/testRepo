package com.mivideo.mifm.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.mivideo.mifm.R
import com.mivideo.mifm.extensions.tryWith
import kotlinx.android.synthetic.main.view_mine_item.view.*


/**
 * 我的界面每个Item自定义View
 * @author LiYan
 */
class MineItemView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var title: String = ""
    private var subTitle: String = ""
    private var icon: Drawable? = null
    private var arrowIcon: Drawable? = null
    private var showDivider: Boolean = true
    private var showArrow: Boolean = true

    init {
        initView(context, attrs)
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        val view = LayoutInflater.from(context).inflate(R.layout.view_mine_item, null)
        addView(view)

        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.MineItemView,
                0, 0)

        tryWith {
            subTitle = a.getString(R.styleable.MineItemView_itemSubTitle)
        }
        tryWith {
            val iconResId = a.getResourceId(R.styleable.MineItemView_itemIcon, -1)
            icon = ContextCompat.getDrawable(context, iconResId)
        }
        tryWith {
            val arrowIconResId = a.getResourceId(R.styleable.MineItemView_arrowIcon, -1)
            arrowIcon = ContextCompat.getDrawable(context, arrowIconResId)
        }
        try {
            title = a.getString(R.styleable.MineItemView_itemTitle)
            showDivider = a.getBoolean(R.styleable.MineItemView_showDivider, true)
            showArrow = a.getBoolean(R.styleable.MineItemView_showArrow, true)
        } finally {
            a.recycle()
        }

        if (!TextUtils.isEmpty(title)) {
            tv_item_title.text = title
        }
        if (!TextUtils.isEmpty(subTitle)) {
            tv_item_sub_title.text = subTitle
        }
        if (icon != null) {
            iv_item_icon.visibility = View.VISIBLE
            iv_item_icon.setImageDrawable(icon)
        } else {
            iv_item_icon.visibility = View.GONE
        }
        if (arrowIcon != null) {
            iv_item_arrow_icon.visibility = View.VISIBLE
            iv_item_arrow_icon.setImageDrawable(arrowIcon)
        } else {
            iv_item_arrow_icon.visibility = View.GONE
        }
        if (showDivider) {
            item_divider.visibility = View.VISIBLE
        } else {
            item_divider.visibility = View.GONE
        }
        if (showArrow) {
            iv_item_arrow.visibility = View.VISIBLE
        } else {
            iv_item_arrow.visibility = View.GONE
        }
    }


    /**
     * 设置标题
     */
    fun setTitle(title: String) {
        if (!TextUtils.isEmpty(title)) {
            this.title = title
            tv_item_title.visibility = View.VISIBLE
            tv_item_title.text = title
        } else {
            tv_item_title.visibility = View.GONE
        }
    }

    /**
     * 设置副标题
     */
    fun setSubTitle(subTitle: String) {
        if (!TextUtils.isEmpty(subTitle)) {
            this.subTitle = subTitle
            tv_item_sub_title.visibility = View.VISIBLE
            tv_item_sub_title.text = subTitle
        } else {
            tv_item_sub_title.visibility = View.GONE
        }
    }

    /**
     * 显示新消息圆点
     */
    fun showMsgCount(count: String) {
        tv_msg_count.visibility = View.VISIBLE
        tv_msg_count.text = count
    }

    fun hideMsgCount() {
        tv_msg_count.visibility = View.GONE
    }


}