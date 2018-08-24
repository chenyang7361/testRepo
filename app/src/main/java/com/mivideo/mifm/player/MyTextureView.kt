package com.mivideo.mifm.player

import android.content.Context
import android.view.TextureView
import timber.log.Timber

/**
 * 播放器组件自定义RenderView
 * @author LiYan
 */
class MyTextureView(context: Context) : TextureView(context) {

    fun adaptVideoSize(videoWidth: Int, videoHeight: Int) {
        val viewWidth = width
        val viewHeight = height
        Timber.i("video width: $videoWidth height: $videoHeight")
        Timber.i("view width: $viewWidth height: $viewHeight")
        //视频尺寸宽度大于高度情况（通常属于短视频流）
        if (videoWidth > videoHeight) {
            val scale = viewWidth / (videoWidth * 1.0f)
            scaleX = 1f
            scaleY = (videoHeight * 1.0f) / viewHeight * scale
            if (scaleY > 1) {
                scaleY = 1f
            }
        } else {//视频尺寸宽度小于高度情况（通常属于小视频流）
            if (viewWidth > viewHeight) {
                val scale = viewHeight / (videoHeight * 1.0f)
                scaleX = videoWidth / (viewWidth * 1.0f) * scale
                scaleY = 1f
            } else {
                scaleX = 1f
                scaleY = 1f
            }
        }
    }

    override fun onDetachedFromWindow() {
        //android 4.4以下SurfaceTexture会产生Error during detachFromGLContext的问题
        try {
            super.onDetachedFromWindow()
        } catch (e: Throwable) {
            Timber.e(e)
        }
    }
}