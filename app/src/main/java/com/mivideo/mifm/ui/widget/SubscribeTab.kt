package com.mivideo.mifm.ui.widget

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.mivideo.mifm.R
import org.jetbrains.anko.onClick

/**
 * Created by Jiwei Yuan on 18-7-26.
 */
class SubscribeTab : LinearLayout {

    private var defaultListener = object : OnTabClickListener {
        override fun onTabClick(position: Int, tab: TabItemView): Boolean {
            for (i in 0 until childCount) {
                var tabView = getChildAt(i) as TabItemView
                if (i == position) {
                    tabView.setCheckedStyle()
                } else {
                    tabView.setDefaultStyle()
                }
            }
            return true
        }
    }
    private var listener: OnTabClickListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private fun newTab(): TabItemView {
        return TabItemView(context)
    }

    fun updateTabData(titles: List<String>) {
        removeAllViews()
        for (i in 0 until titles.size) {
            val item = newTab()
            item.setText(titles[i])
            item.setDefaultStyle()
            item.index = i
            addView(item)
        }
        setSelectPosition(0)
    }

    private fun setSelectPosition(position: Int) {
        if (position < 0 || position >= childCount) return
        for (i in 0 until childCount) {
            val tab = getChildAt(i) as TabItemView
            if (i == position) {
                tab.setCheckedStyle()
            } else {
                tab.setDefaultStyle()
            }
        }
    }


    fun addOnTabClickListener(l: OnTabClickListener) {
        listener = l
    }

    interface OnTabClickListener {
        /**
         *return true if default listener is needed else false
         */
        fun onTabClick(position: Int, tab: TabItemView): Boolean
    }

    inner class TabItemView : FrameLayout {
        private lateinit var view: TextView
        var index: Int = 0

        constructor(context: Context) : super(context) {
            initView()
        }

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            initView()
        }

        constructor(context: Context,
                    attrs: AttributeSet? = null,
                    defStyle: Int = 0) : super(context, attrs, defStyle) {
            initView()
        }

        private fun initView() {
            val layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
            layoutParams.weight = 1f
            this.layoutParams = layoutParams
            view = LayoutInflater.from(context).inflate(R.layout.layout_subscribe_tab, null) as TextView
            addView(view)
            view.onClick {
                if (listener == null || listener!!.onTabClick(index, this@TabItemView)) {
                    defaultListener.onTabClick(index, this@TabItemView)
                }
            }
        }

        fun setText(text: String) {
            view.text = text
        }

        fun getText(): String {
            return view.text.toString()
        }

        private fun setDefaultTextColor() {
            view.setTextColor(context.resources.getColor(R.color.text_recom_more))
        }

        private fun setCheckedTextColor() {
            view.setTextColor(context.resources.getColor(R.color.text_dark_black))

        }

        fun setDefaultStyle() {
            setDefaultTextColor()
            view.typeface = Typeface.DEFAULT
        }

        fun setCheckedStyle() {
            setCheckedTextColor()
            view.typeface = Typeface.DEFAULT_BOLD
        }

    }
}

