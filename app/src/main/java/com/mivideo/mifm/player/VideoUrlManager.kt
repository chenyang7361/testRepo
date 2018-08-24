package com.mivideo.mifm.player

import com.mivideo.mifm.data.models.jsondata.common.VideoInfoParams
import timber.log.Timber

/**
 * 视频播放地址
 * @author LiYan
 */
class VideoUrlManager {
    companion object {
        fun parse(videoInfoParams: VideoInfoParams, useWifiResolution: Boolean): VideoUrlManager {
            return VideoUrlManager(videoInfoParams, useWifiResolution)
        }

        fun create(): VideoUrlManager = VideoUrlManager()
    }

    private val addressList: ArrayList<VideoUrl> = ArrayList(0)
    private var addressPosition = 0

    private constructor(videoInfoParams: VideoInfoParams, useWifiResolution: Boolean) {
        Timber.i("constructor 2")
        if (useWifiResolution) {
            Timber.i("constructor 2 useWifiResolution")
            printSize(videoInfoParams.commonVideo.playUrl.sd_play_url_list)
            printSize(videoInfoParams.commonVideo.playUrl.hd_play_url_list)
            printSize(videoInfoParams.commonVideo.playUrl.nd_play_url_list)
            printSize(videoInfoParams.commonVideo.playUrl.ld_play_url_list)

            videoInfoParams.commonVideo.playUrl.sd_play_url_list.forEach {
                Timber.i("add RESOLUTION_SUPPER")
                addressList.add(VideoUrl(it))
            }
            videoInfoParams.commonVideo.playUrl.hd_play_url_list.forEach {
                Timber.i("add RESOLUTION_HIGH")
                addressList.add(VideoUrl(it))
            }
            videoInfoParams.commonVideo.playUrl.nd_play_url_list.forEach {
                Timber.i("add RESOLUTION_NORMAL")
                addressList.add(VideoUrl(it))
            }
            videoInfoParams.commonVideo.playUrl.ld_play_url_list.forEach {
                Timber.i("add RESOLUTION_LOW")
                addressList.add(VideoUrl(it))
            }
        } else {
            Timber.i("constructor 2 useMobileResolution")
            videoInfoParams.commonVideo.playUrl.ld_play_url_list.forEach {
                addressList.add(VideoUrl(it))
            }
            videoInfoParams.commonVideo.playUrl.nd_play_url_list.forEach {
                addressList.add(VideoUrl(it))
            }
            videoInfoParams.commonVideo.playUrl.hd_play_url_list.forEach {
                addressList.add(VideoUrl(it))
            }
            videoInfoParams.commonVideo.playUrl.sd_play_url_list.forEach {
                addressList.add(VideoUrl(it))
            }
        }
    }

    private  fun printSize(data: List<String>) {
        Timber.i("size is ${data.size}")
    }

    private constructor(){
        Timber.i("constructor")
    }

    fun addAll(urls: List<VideoUrl>) {
        addressList.addAll(urls)
    }

    fun size(): Int {
        return addressList.size
    }

    fun hasNext(): Boolean {
        if (addressList.size > 0 && addressPosition < addressList.size) {
            return true
        }
        return false
    }

    private var currentVideoUrl: VideoUrl? = null

    fun next(): String {
        if (addressPosition < addressList.size) {
            currentVideoUrl = addressList[addressPosition]
            addressPosition++
            return currentVideoUrl!!.url
        } else {
            throw NoNextVideoUrlException()
        }
    }

    /**
     * 将当前地址列表指针归0，从头还是遍历
     */
    fun resetCursor() {
        addressPosition = 0
    }

    /**
     * 当前播放器正在使用的播放器url
     */
    fun currentVideoUrl() = currentVideoUrl

}