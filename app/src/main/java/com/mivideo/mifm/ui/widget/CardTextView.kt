package com.mivideo.mifm.ui.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.TextView
import com.mivideo.mifm.R
import com.mivideo.mifm.util.getColor

/**
 * Created by aaron on 2017/2/8.
 */
class CardTextView : TextView {

    var isSelect : Boolean = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
        setOnClickListener {
            if (isSelect) {
                isSelect = false
                setBackgroundResource(R.drawable.suggest_edit_bg)
                setTextColor(Color.parseColor("#FF000000"))
            } else {
                isSelect = true
                setBackgroundResource(R.drawable.suggest_icon_bg)
                setTextColor(Color.parseColor("#FFFFFFFF"))
            }
        }
    }
}