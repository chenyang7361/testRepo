package com.mivideo.mifm.extensions

import android.graphics.PixelFormat
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.ViewTreeObserver.OnPreDrawListener

/**
 * 当 View 首次绘制时调用
 */
fun <T : View> T.deferOnPreDraw(f: (T) -> Unit) = let {
    if (viewTreeObserver.isAlive) {
        viewTreeObserver.addOnPreDrawListener(object : OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (viewTreeObserver.isAlive) {
                    viewTreeObserver.removeOnPreDrawListener(this)
                }
                f.invoke(it)
                return false
            }
        })
    }
}

/**
 * 当 View 首次布局成功后调用
 */
fun <T : View> T.deferOnGlobalLayout(f: (T) -> Unit) = let {
    if (viewTreeObserver.isAlive) {
        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (viewTreeObserver.isAlive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
                f.invoke(it)
            }
        })
    }
}

fun View.hasOpaqueBackground(): Boolean {
    return background != null && background.opacity == PixelFormat.OPAQUE
}

/**
 * Calculate if one position is above any view.
 * @param view to analyze.
 * @param x    position.
 * @param y    position.
 * @return true if x and y positions are below the view.
 */
fun View.isViewHit(view: View?, x: Float, y: Float): Boolean {
    return isViewHit(view, x.toInt(), y.toInt())
}

fun View.isViewHit(view: View?, x: Int, y: Int): Boolean {
    if (view == null) return false
    val viewLocation = IntArray(2)
    view.getLocationOnScreen(viewLocation)
    val parentLocation = IntArray(2)
    this.getLocationOnScreen(parentLocation)
    val screenX = parentLocation[0] + x
    val screenY = parentLocation[1] + y
    return screenX >= viewLocation[0] &&
            screenX < viewLocation[0] + view.width &&
            screenY >= viewLocation[1] &&
            screenY < viewLocation[1] + view.height
}

val View.isGone: Boolean
    get() = this.visibility == View.GONE
