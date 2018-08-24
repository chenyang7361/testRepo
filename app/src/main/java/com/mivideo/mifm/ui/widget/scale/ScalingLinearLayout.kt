package com.mivideo.mifm.ui.widget.scale

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import timber.log.Timber

class ScalingLinearLayout : LinearLayout {
    private var baseWidth: Int = 0
    private var baseHeight: Int = 0
    private var alreadyScaled: Boolean = false
    private var scale: Float = 0.toFloat()
    private var expectedWidth: Int = 0
    private var expectedHeight: Int = 0

    constructor(context: Context) : super(context) {

        Timber.d("notcloud.view", "ScalingLinearLayout: width=" + this.width + ", height=" + this.height)
        this.alreadyScaled = false
    }

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {

        Timber.d("notcloud.view", "ScalingLinearLayout: width=" + this.width + ", height=" + this.height)
        this.alreadyScaled = false
    }

    public override fun onFinishInflate() {
        super.onFinishInflate()
        Timber.d("notcloud.view", "ScalingLinearLayout::onFinishInflate: 1 width=" + this.width + ", height=" + this.height)

        // Do an initial measurement of this layout with no major restrictions on size.
        // This will allow us to figure out what the original desired width and height are.
        this.measure(1000, 1000) // Adjust this up if necessary.
        this.baseWidth = this.measuredWidth
        this.baseHeight = this.measuredHeight
        Log.d("notcloud.view", "ScalingLinearLayout::onFinishInflate: 2 width=" + this.width + ", height=" + this.height)

        Log.d("notcloud.view", "ScalingLinearLayout::onFinishInflate: alreadyScaled=" + this.alreadyScaled)
        Log.d("notcloud.view", "ScalingLinearLayout::onFinishInflate: scale=" + this.scale)
        if (this.alreadyScaled) {
            Scale.scaleViewAndChildren(this as LinearLayout, this.scale, 0)
        }
    }

    override fun draw(canvas: Canvas) {
        // Get the current width and height.
        val width = this.width
        val height = this.height

        // Figure out if we need to scale the layout.
        // We may need to scale if:
        //    1. We haven't scaled it before.
        //    2. The width has changed.
        //    3. The height has changed.
        if (!this.alreadyScaled || width != this.expectedWidth || height != this.expectedHeight) {
            // Figure out the x-scaling.
            var xScale = width.toFloat() / this.baseWidth
            if (this.alreadyScaled && width != this.expectedWidth) {
                xScale = width.toFloat() / this.expectedWidth
            }
            // Figure out the y-scaling.
            var yScale = height.toFloat() / this.baseHeight
            if (this.alreadyScaled && height != this.expectedHeight) {
                yScale = height.toFloat() / this.expectedHeight
            }

            // Scale the layout.
            this.scale = Math.min(xScale, yScale)
            Log.d("notcloud.view", "new scale: $scale")
            Log.d("notcloud.view", "ScalingLinearLayout::onLayout: Scaling!")
            Scale.scaleViewAndChildren(this as LinearLayout, this.scale, 0)

            // Mark that we've already scaled this layout, and what
            // the width and height were when we did so.
            this.alreadyScaled = true
            this.expectedWidth = width
            this.expectedHeight = height

            // Finally, return.
            return
        }

        super.draw(canvas)
    }
}