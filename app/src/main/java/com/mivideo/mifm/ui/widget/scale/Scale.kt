package com.mivideo.mifm.ui.widget.scale

import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import timber.log.Timber


object Scale {
    fun scaleContents(rootView: View, container: View) {
        Scale.scaleContents(rootView, container, rootView.width, rootView.height)
    }

    // Scales the contents of the given view so that it completely fills the given
    // container on one axis (that is, we're scaling isotropically).
    fun scaleContents(rootView: View, container: View, width: Int, height: Int) {
        Log.d("notcloud.scale", "Scale::scaleContents: container: " + container.width + "x" + container.height + ".")

        // Compute the scaling ratio
        val xScale = container.width.toFloat() / width
        val yScale = container.height.toFloat() / height
        val scale = Math.min(xScale, yScale)

        // Scale our contents
        Log.d("notcloud.scale", "Scale::scaleContents: scale=$scale, width=$width, height=$height.")
        scaleViewAndChildren(rootView, scale, 0)
    }

    // Scale the given view, its contents, and all of its children by the given factor.
    fun scaleViewAndChildren(root: View, scale: Float, canary: Int) {
        // Retrieve the view's layout information
        val layoutParams = root.layoutParams

        // Scale the View itself
        if (layoutParams.width != ViewGroup.LayoutParams.MATCH_PARENT && layoutParams.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
            layoutParams.width = (layoutParams.width * scale).toInt()
        }
        if (layoutParams.height != ViewGroup.LayoutParams.MATCH_PARENT && layoutParams.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
            layoutParams.height = (layoutParams.height * scale).toInt()
        }

        // If the View has margins, scale those too
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.leftMargin = (layoutParams.leftMargin * scale).toInt()
            layoutParams.topMargin = (layoutParams.topMargin * scale).toInt()
            layoutParams.rightMargin = (layoutParams.rightMargin * scale).toInt()
            layoutParams.bottomMargin = (layoutParams.bottomMargin * scale).toInt()
        }


        if (root is ImageView) {
            Timber.d("imageView height: ${root.height},width: ${root.width}")
            var height = root.height
            if (height == 0) {
                height = root.drawable.intrinsicHeight
            }
            var width = root.width
            if (width == 0) {
                width = root.drawable.intrinsicWidth
            }
            layoutParams.height = (height * scale).toInt()
            layoutParams.width = (width * scale).toInt()
        }

        root.layoutParams = layoutParams

        // Same treatment for padding
        root.setPadding(
                (root.paddingLeft * scale).toInt(),
                (root.paddingTop * scale).toInt(),
                (root.paddingRight * scale).toInt(),
                (root.paddingBottom * scale).toInt()
        )


        if (root is TextView) {
            Timber.i("origin textSize: ${root.textSize}")
            Timber.i("scaled textSize: ${root.textSize * scale}")
//            root.TypedValue.COMPLEX_UNIT_SP = root.textSize
            root.setTextSize(TypedValue.COMPLEX_UNIT_PX, root.textSize * scale)
        }


        // If it's a ViewGroup, recurse!
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                scaleViewAndChildren(root.getChildAt(i), scale, canary + 1)
            }
        }
    }
}