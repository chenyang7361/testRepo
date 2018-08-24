package com.mivideo.mifm.player

import android.view.View

/**
 * Created by xingchang on 16/11/30.
 */

interface IVideoView {
    fun asView(): View

    val videoWidth: Int

    val videoHeight: Int

    fun requestVideoLayout()

    fun setForceFullScreen(forceFullScreen: Boolean)

    fun continuePlay(position: Int)

}