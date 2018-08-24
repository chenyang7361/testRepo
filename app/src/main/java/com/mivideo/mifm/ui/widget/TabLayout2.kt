package com.mivideo.mifm.ui.widget

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.support.design.widget.TabLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.mivideo.mifm.R
import org.jetbrains.anko.textColor

/**
 * Created by aaron on 2018/3/15.
 * 自定义TabLayout控件
 */
class TabLayout2 : TabLayout {

    var tBackcolor = 0xFFFFFF

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }


    private fun init(attrs: AttributeSet) {

        try {
            val array = context.obtainStyledAttributes(attrs, R.styleable.TabLayout2)
            tBackcolor = array.getColor(R.styleable.TabLayout2_tBackground, 0xFFFFFF)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        addOnTabSelectedListener(object: OnTabSelectedListener {
            override fun onTabReselected(tab: Tab?) {
            }

            override fun onTabUnselected(tab: Tab?) {
            }

            override fun onTabSelected(tab: Tab?) {
                for (i in 0 until tabCount) {
                    if (tab == getTabAt(i)) {
                        setSelectPosition(i)
                    }
                }
            }

        })
    }

    /**
     * 更新
     */
    fun updateTabData(titleList: List<String>) {
        if (titleList.size != tabCount) return
        for (i in 0 until tabCount) {
            val tab = getTabAt(i)
            if (tab?.customView == null) {
                val tabLayout = LayoutInflater.from(context).inflate(R.layout.home_tab_item, null, false)
                val textView = tabLayout.findViewById<TextView>(R.id.titleView)
                // tabLayout.setBackgroundColor(tBackcolor)
                textView.text = titleList[i]
                tab?.customView = tabLayout
            } else {
                val textView = tab.customView?.findViewById<TextView>(R.id.titleView)
                textView?.text = titleList[i]
            }
        }
        setSelectPosition(0)
    }

    private fun setSelectPosition(position: Int) {
        if (position < 0 || position >= tabCount) return
        for (i in 0 until tabCount) {
            val tab = getTabAt(i)
            if (i == position) {
                tab?.customView?.findViewById<TextView>(R.id.titleView)?.textColor = Color.parseColor("#ff000000")
                tab?.customView?.findViewById<TextView>(R.id.titleView)?.typeface = Typeface.DEFAULT_BOLD
                tab?.customView?.findViewById<View>(R.id.indicatorView)?.visibility = View.GONE
            } else {
                tab?.customView?.findViewById<TextView>(R.id.titleView)?.textColor = Color.parseColor("#9DB2BC")
                tab?.customView?.findViewById<TextView>(R.id.titleView)?.typeface = Typeface.DEFAULT
                tab?.customView?.findViewById<View>(R.id.indicatorView)?.visibility = View.GONE
            }
        }
    }
}
