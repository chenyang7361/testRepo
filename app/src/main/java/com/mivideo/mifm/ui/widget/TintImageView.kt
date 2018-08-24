package com.mivideo.mifm.ui.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.AttributeSet
import android.widget.ImageView
import com.mivideo.mifm.R

/**
 * Created by aaron on 2017/8/24.
 */
class TintImageView : ImageView {

    lateinit var tintColor: ColorStateList

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    fun init(attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.TintImageView)
        if (ta.getColorStateList(R.styleable.TintImageView_tintColor) != null) {
            tintColor = ta.getColorStateList(R.styleable.TintImageView_tintColor)
            setImageDrawable(tintDrawable(drawable.mutate(), tintColor))
        }
    }

    /**
     * 为Drawable着色
     */
    fun tintDrawable(drawable: Drawable, colors: ColorStateList): Drawable {
        val wrappedDrawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTintList(wrappedDrawable, colors)
        return wrappedDrawable
    }

    fun setTintImageResource(resourceId: Int) {
        setImageResource(resourceId)
        setImageDrawable(tintDrawable(drawable.mutate(), tintColor))
    }

    fun createSelector(drawable: Drawable): StateListDrawable {
        val stateDrawable = StateListDrawable()
        stateDrawable.addState(intArrayOf(android.R.attr.state_pressed), drawable)
        stateDrawable.addState(intArrayOf(-android.R.attr.state_pressed), drawable)

        return stateDrawable
    }
}