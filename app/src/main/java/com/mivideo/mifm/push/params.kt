package com.mivideo.mifm.push

import com.mivideo.mifm.RouterConf


/**
 * Push拉起视频播放参数
 * @param videoId 视频Id
 * @param from 来源，如“h5”,"deepLink"等等
 * @param playMode 播放模式，当前有两种模式：小视频播放和短视频播放，不填写默认短视频播放
 *
 * @see RouterConf.Routers.PLAY_MODE_SHORT_VIDEO
 * @see RouterConf.Routers.PLAY_MODE_SMALL_VIDEO
 */
data class PushPlayVideoParams(var videoId: String?,
                               var from: String?,
                               var playMode: String? = RouterConf.Routers.PLAY )