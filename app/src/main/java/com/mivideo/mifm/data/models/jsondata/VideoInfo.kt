package com.mivideo.mifm.data.models.jsondata

import com.mivideo.mifm.data.models.jsondata.common.CommonPageVideo
import com.mivideo.mifm.data.models.jsondata.common.CommonVideo
import java.util.ArrayList

/**
 * Created by aaron on 2016/11/22.
 */
class VideoInfo {

    var data : VideoInfoData?= null
}

class VideoInfoData {
    var page_no : Int = 0
    var page_size : Int = 0
    var video : CommonVideo?= null
    var page_data = ArrayList<CommonPageVideo>()
}

/**
 * 视频类型：
 * 短视频
 * 小视频
 */
const val VIDEO_TYPE_SHORT = 0
const val VIDEO_TYPE_SMALL = 1