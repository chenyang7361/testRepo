package com.mivideo.mifm.ui.widget

import android.content.Context
import android.graphics.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource

/**
 * Created by aaron on 2017/3/1.
 * 图片滤镜，用于提高图片亮度
 */
class BrightTransform : Transformation<Bitmap> {

    var mContext: Context
    var mBitmapPool: BitmapPool

    constructor(mContext: Context) {
        this.mContext = mContext
        mBitmapPool = Glide.get(mContext).bitmapPool
    }

    override fun getId(): String {
        return "bright_transform"
    }

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        val bitmap = resource.get()
        val cMatrix = ColorMatrix()
        /*cMatrix.set(floatArrayOf(1.5f, 0f, 0f, 0f, 0f,
                0f, 1.5f, 0f, 0f, 0f,
                0f, 0f, 1.5f, 0f, 0f,
                0f, 0f, 0f, 1.5f, 0f))*/
        cMatrix.setScale(1.1.toFloat(), 1.1.toFloat(), 1.1.toFloat(), 1.toFloat())

        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(cMatrix)

        val mBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mBitmap)
        // 在Canvas上绘制一个已经存在的Bitmap。这样，dstBitmap就和srcBitmap一摸一样了
        canvas.drawBitmap(bitmap, 0.toFloat(), 0.toFloat(), paint)

        return BitmapResource.obtain(mBitmap, mBitmapPool)
    }

}