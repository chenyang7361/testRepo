package com.mivideo.mifm.ui.widget.player

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.AudioInfo
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

/**
 * Create by KevinTu on 2018/8/14
 */
class AudioAlbumView : RelativeLayout {

    private val albumImg: ImageView by lazy {
        findViewById<ImageView>(R.id.album_img)
    }
    private val albumName: TextView by lazy {
        findViewById<TextView>(R.id.album_name)
    }
    private val subscribeCount: TextView by lazy {
        findViewById<TextView>(R.id.subscribe_count)
    }

    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.layout_audio_album, this, true)
    }

    fun refreshView(audioInfo: AudioInfo) {
        Glide.with(context)
                .load(audioInfo.albumInfo.cover)
                .priority(Priority.HIGH)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .bitmapTransform(RoundedCornersTransformation(context, 15, 0))
                .into(albumImg)
        albumName.text = audioInfo.albumInfo.author
    }
}