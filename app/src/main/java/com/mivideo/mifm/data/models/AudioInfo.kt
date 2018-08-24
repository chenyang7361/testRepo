package com.mivideo.mifm.data.models

import com.mivideo.mifm.data.models.jsondata.AlbumInfo
import com.mivideo.mifm.data.models.jsondata.PassageItem

/**
 * 视频播放所需数据源
 *
 * Create by KevinTu on 2018/8/14
 */
data class AudioInfo(var albumInfo: AlbumInfo = AlbumInfo(),
                     var passageItem: PassageItem = PassageItem())
