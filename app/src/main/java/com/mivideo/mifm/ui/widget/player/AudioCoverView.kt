package com.mivideo.mifm.ui.widget.player

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.AudioInfo
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import org.jetbrains.anko.centerHorizontally
import java.lang.Exception

/**
 * Create by KevinTu on 2018/8/14
 */
class AudioCoverView : RelativeLayout {

    private val coverImg: ImageView by lazy {
        findViewById<ImageView>(R.id.cover_img)
    }
    private val coverBgImg: ImageView by lazy {
        findViewById<ImageView>(R.id.cover_bg_img)
    }

    private var lastImgUrl: String? = null

    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.layout_audio_cover, this, true)
    }

    fun refreshView(audioInfo: AudioInfo) {
        lastImgUrl?.let {
            if (it == audioInfo.albumInfo.cover) {
                return
            }
        }
        lastImgUrl = audioInfo.albumInfo.cover
        Glide.with(context)
                .load(audioInfo.albumInfo.cover)
                .asBitmap()
                .priority(Priority.HIGH)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(RoundedCornersTransformation(context, 30, 0))
                .listener(object : RequestListener<String, Bitmap> {
                    override fun onException(e: Exception?, model: String?,
                                             target: Target<Bitmap>?,
                                             isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(bitmap: Bitmap, model: String,
                                                 target: Target<Bitmap>, isFromMemoryCache: Boolean,
                                                 isFirstResource: Boolean): Boolean {
                        var imgWidth = bitmap.width
                        var imgHeight = bitmap.height
                        var minImgHeight = resources.getDimensionPixelOffset(R.dimen.audio_play_cover_img_height_width)
                        if (imgHeight < minImgHeight) {
                            var layoutParams = RelativeLayout.LayoutParams(imgWidth, imgHeight)
                            layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen.audio_play_cover_img_margin_top)
                            layoutParams.centerHorizontally()
                            coverImg.layoutParams = layoutParams
                        }

                        val color = bitmap.getPixel(bitmap.width / 8 * 5, 0)
                        createCoverBgImg(color)
                        return false
                    }
                })
                .into(coverImg)
    }

    private fun createCoverBgImg(color: Int) {
        coverImg.addOnLayoutChangeListener(object : OnLayoutChangeListener{
            override fun onLayoutChange(view: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                var shadeWidth = resources.getDimensionPixelOffset(R.dimen.audio_play_cover_bg_shade_width) * 2
                var layoutParams = RelativeLayout.LayoutParams(view.width + shadeWidth, view.height + shadeWidth)
                layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen.audio_play_cover_bg_img_margin_top)
                layoutParams.bottomMargin = resources.getDimensionPixelSize(R.dimen.audio_play_cover_bg_img_margin_bottom)
                layoutParams.centerHorizontally()
                coverBgImg.layoutParams = layoutParams
                var resourceId = R.drawable.icon_cover_bg_square
                var viewAspectRatio = view.width.toFloat() / view.height.toFloat()
                if (viewAspectRatio <= 0.85F || viewAspectRatio >= 1.15F) {
                    resourceId = R.drawable.icon_cover_bg_rectangle
                }
                coverBgImg.setImageResource(resourceId)
                coverBgImg.scaleType = ImageView.ScaleType.CENTER_CROP
                coverBgImg.setColorFilter(color)
                coverImg.removeOnLayoutChangeListener(this)
            }
        })
    }
}