package com.mivideo.mifm.data.models

import com.mivideo.mifm.data.models.jsondata.common.CommonVideo

/**
 * Created by xingchang on 16/12/14.
 */
class CommonVideoHistory {
    private var lastPosition = 0
    private var commonVideo: CommonVideo? = null

    constructor(video: CommonVideo) {
        commonVideo = video
    }

    constructor(video: CommonVideo, position: Int) {
        commonVideo = video
        lastPosition = position
    }

    fun getLastPosition(): Int {
        return lastPosition
    }

    fun setLostPosition(position: Int) {
        lastPosition = position
    }

    fun getCommonVideo(): CommonVideo? {
        return commonVideo
    }

    fun setCommonVideo(video: CommonVideo) {
        commonVideo = video
    }
}