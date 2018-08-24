package com.mivideo.mifm.events

import android.os.Parcel
import android.os.Parcelable
import com.mivideo.mifm.data.models.jsondata.HistoryItem
import com.mivideo.mifm.data.models.jsondata.MediaDetailData
import com.mivideo.mifm.data.models.jsondata.common.CommonPageVideo
import com.mivideo.mifm.data.models.jsondata.common.VideoInfoParams

/**
 * 首页底部Tab切换事件
 */
class TabChangeEvent(val tab: String)

data class TabHostChangeEvent(val fragmentName: String, val tabId: String)
/**
 * 显示Home页面
 */
data class ShowHomeFragmentEvent(val sender: String)

/**
 * Fragment是否创建成功
 */
data class FragmentCreatedEvent(val sender: String)

class CloseMainActivityEvent(val sender: String)

class CloseMediaPlayerActivityEvent(val sender: String)

/**
 * fm列表页面刷新
 */
data class RefreshMediaListEvent(val position: Int)

/**
 * 主页面ViewPager Position切换
 */
data class HomeMediaPageChangeEvent(val position: Int, val tabId: String)

/**
 * 隐藏播放详情
 */
class HideMediaDetailPageEvent

/**
 * 回复评论,暂停播放
 */
data class PauseMediaEvent(val sender: String)

/**
 * 回复评论,继续播放
 */
data class ResumeMediaEvent(val sender: String)

class VideoDownloadWaitingEvent(val key: String)

class VideoDownloadStartEvent(val key: String)

class VideoDownloadProgressEvent(val key: String, val progress: Int, val completeSize: Long, val totalSize: Long, val appendSize: Long)

class VideoDownloadCompleteEvent(val key: String, val success: Boolean, val result: String, val errorCode: Int)

class VideoDownloadClickedEvent(val key: String)

class VideoCheckBoxClickedEvent(val key: String, val check: Boolean)

class VideoRequestDeleteEvent(val key: String)

class VideoRequestPlayEvent(val key: String, val videoInfo: VideoInfoParams)

class StartDetailFragmentEvent(val id: String)
class ShareDetailEvent(val data: MediaDetailData)
class CollectDetailEvent(val id: String)
class UnCollectDetailEvent(val id: String)
class HideStatusBarEvent
class HistoryItemCheckChangeEvent(val checked: Boolean, val id: String)
class RecommendMoreEvent(val tabId: String)
class HeadLineRecommendClickEvent
class DetailPassageClickedEvent(val id: String, val position: Int)
class ShowHistoryEvent
class showMoreAlbumContentEvent(val id:String)

class PlayHistoryEvent(val item: HistoryItem)

class MediaPreparedEvent(val albumId: String)

class MediaCompleteEvent

class ShowPlayerEvent
/**
 * 隐藏播放详情，小窗口播放
 */
class HideMediaPlayerPageEvent

/**
 * 白牌单例作者页面返回按钮点击处理逻辑
 */
data class SingleFragmentBackEvent(val sender: String)

/**
 * 播放详情页点击更多显示全部相关视频
 */
data class MediaMoreClickEvent(val mediaMoreList: MutableList<CommonPageVideo>)

/**
 * 通知当前播放的
 */
data class NotifyMediaPlayEvent(var video: VideoInfoParams,
                                var autoPlay: Boolean,
                                val currPosition: Int,
                                val itemPositionInList: Int = -1) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readParcelable(VideoInfoParams::class.java.classLoader),
            parcel.readByte() != 0.toByte(),
            parcel.readInt(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(video, flags)
        parcel.writeByte(if (autoPlay) 1 else 0)
        parcel.writeInt(currPosition)
        parcel.writeInt(itemPositionInList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NotifyMediaPlayEvent> {
        override fun createFromParcel(parcel: Parcel): NotifyMediaPlayEvent {
            return NotifyMediaPlayEvent(parcel)
        }

        override fun newArray(size: Int): Array<NotifyMediaPlayEvent?> {
            return arrayOfNulls(size)
        }
    }

}