package com.mivideo.mifm.player

import android.content.Context
import android.text.TextUtils
import com.mivideo.mifm.data.models.jsondata.common.CommonPageVideo
import com.mivideo.mifm.data.models.jsondata.common.VideoInfoParams
import com.mivideo.mifm.events.NotifyMediaPlayEvent
import com.mivideo.mifm.util.app.postEvent
import timber.log.Timber
import java.util.ArrayList

/**
 * Created by aaron on 2016/11/29.
 * 接下来播放数据源
 */
class VideoProxySource constructor(val mContext: Context) {
    private var mPosition: Int = -1
    private var mList: ArrayList<VideoInfoParams> = ArrayList()

    /**
     * 获取下一个播放视频
     * 当视频位置为最后五项视频时开始预加载后续视频
     */
    fun getNextPlayVideo(movePosition : Boolean) : VideoInfoParams? {
        return getNextPlayVideo(mPosition, movePosition)
    }

    private fun getNextPlayVideo(position: Int, movePosition : Boolean) : VideoInfoParams? {
        var result : VideoInfoParams?= null
        var pos = 0
        if (movePosition) {
            pos = ++mPosition
        } else {
            pos = position + 1
        }
        if (pos < mList.size) {
            result = mList[pos]
        }
        if (result != null && TextUtils.isEmpty(result.commonVideo.video_id)) {
            return getNextPlayVideo(pos, movePosition)
        }
        return result
    }

    fun moveToPrePosition() {
        mPosition -= 1
    }

    /**
     * 获取当前播放位置
     */
    fun getPosition() : Int {
        return mPosition
    }

    fun setPosition(position: Int) {
        if (mList.size > position) {
            mPosition = position
        }
    }

    fun dataClear() {
        mList.clear()
        mPosition = -1
    }

    fun removeFromPlayList(position: Int) {
        if (position < mList.size) {
            mList.removeAt(position)
            if (position <= mPosition) {
                mPosition--
            }
        }
    }

    /**
     * 添加更多相关视频
     */
    fun addMoreVideo(videoMoreList: MutableList<CommonPageVideo>) {
        for (i in 0 until videoMoreList.size) {
            val videoInfoParams = VideoInfoParams()
            videoInfoParams.commonVideo = videoMoreList[i].card_data[0]
            mList.add(videoInfoParams)
        }
    }

    fun playNextVideo(isAutoPlay: Boolean): Boolean {
        val nextVideo = getNextPlayVideo(true)
        if (nextVideo == null) {
            Timber.i("!!!!!! not found next video !!!!!!")
            return false
        } else {
            if (isAutoPlay) {
                postEvent(NotifyMediaPlayEvent(nextVideo, true, getPosition()))
            } else {
                postEvent(NotifyMediaPlayEvent(nextVideo, false, -1))
            }
            return true
        }
    }
}