package com.mivideo.mifm.data.models.jsondata.common

import android.os.Parcel
import android.os.Parcelable
import android.os.SystemClock
import android.util.Log
import com.mivideo.mifm.data.models.jsondata.common.CommonVideoExtension
import com.mivideo.mifm.util.MJson
import timber.log.Timber
import kotlin.collections.ArrayList

/**
 * Created by aaron on 2016/11/21.
 */
class CommonVideo : Parcelable {
    var video_id = ""
    var video_title = ""
    var video_duration = 0
    var comment_count = 0
    var play_count = 0
    var video_image = ""
    var play_url = ""
    var cp = ""
    var cp_name = ""
    var loved = 0
    var love_count = 0
    var created_at = 0
    var updated_at = 0
    var video_type = 0

    var playUrl = PlayUrl()

    var play_url_list: ArrayList<String>
        get() = playUrl.defaultUrlList()
        private set(value) {
            play_url_list = value
        }
    var category = ArrayList<String>()
    var share_info = CommonShare()
    var author = CommonAuthor()
    var stat = CommonStat()
    var stat_ext = ""
    val banner = ArrayList<CommonBanner>()
    var showDislikeTagLayout: Boolean = false
    var showDislikeResultLayout: Boolean = false
    var tag = ArrayList<String>()
    var selectTags = ArrayList<String>()
    var embed = ArrayList<CommonEmbed>()
    var relate = false

    var startTime = 0L
    var adTime = 0L
    var viewTime = 0L
    var viewHasAdTime = 0L

    var url_ext = ""
    var videoExt: CommonVideoExtension? = null

    fun getLinkUrl(): String {
        if (videoExt == null) {
            videoExt = MJson.getInstance().fromJson<CommonVideoExtension>(url_ext, CommonVideoExtension::class.java)
        }
        return when (cp) {
            "yk" -> "https://m.youku.com/video/id_${videoExt?.sid}.html"
            else -> ""
        }
    }

    fun getSchemeStr(): String {
        if (videoExt == null) {
            videoExt = MJson.getInstance().fromJson<CommonVideoExtension>(url_ext, CommonVideoExtension::class.java)
        }
        return when (cp) {
            "yk" -> "youku://play?vid=${videoExt?.sid}&source=xiaomi"
            else -> ""
        }
    }

    fun saveStartTime() {
        startTime = SystemClock.uptimeMillis()
    }

    fun saveTotalTime() {
        adTime = SystemClock.uptimeMillis()
    }

    fun saveViewTime() {
        if (startTime > 0) {
            viewTime += SystemClock.uptimeMillis() - startTime
        }
        if (adTime > 0) {
            viewHasAdTime += SystemClock.uptimeMillis() - adTime
        }
        stat.onceState.v_d = viewTime
        startTime = 0
        adTime = 0
    }

    fun getCurrentViewTime() : Long {
        if (startTime <= 0)
            return viewTime
        return viewTime + SystemClock.uptimeMillis() - startTime
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.video_id)
        dest.writeString(this.video_title)
        dest.writeInt(this.video_duration)
        dest.writeInt(this.comment_count)
        dest.writeInt(this.play_count)
        dest.writeInt(this.loved)
        dest.writeInt(this.love_count)
        dest.writeInt(this.created_at)
        dest.writeInt(this.updated_at)
        dest.writeInt(this.video_type)
        dest.writeString(this.video_image)
        dest.writeString(this.play_url)
        dest.writeString(this.cp)
        dest.writeString(this.cp_name)
        dest.writeString(this.stat_ext)
        dest.writeParcelable(this.author, flags)
        dest.writeParcelable(this.stat, flags)
        dest.writeParcelable(this.share_info, flags)
        dest.writeByte((if (showDislikeTagLayout) 1 else 0).toByte())
        dest.writeByte((if (showDislikeResultLayout) 1 else 0).toByte())
        dest.writeString(this.url_ext)
    }

    override fun toString(): String {
        return "CommonVideo(${hashCode()})(video_id='$video_id', " +
                "video_title='$video_title', " +
                "video_duration=$video_duration)" +
                "PlayUrl=$playUrl"
    }

    constructor() {
        Timber.i("CommonVideo constructor hashCode:${hashCode()}")
    }

    constructor(`in`: Parcel) {
        this.video_id = `in`.readString()
        this.video_title = `in`.readString()
        this.video_duration = `in`.readInt()
        this.comment_count = `in`.readInt()
        this.play_count = `in`.readInt()
        this.loved = `in`.readInt()
        this.video_type = `in`.readInt()
        this.love_count = `in`.readInt()
        this.created_at = `in`.readInt()
        this.updated_at = `in`.readInt()
        this.video_image = `in`.readString()
        this.play_url = `in`.readString()
        this.cp = `in`.readString()
        this.cp_name = `in`.readString()
        this.stat_ext = `in`.readString()
        this.author = `in`.readParcelable<CommonAuthor>(CommonAuthor::class.java.classLoader)
        this.stat = `in`.readParcelable<CommonStat>(CommonStat::class.java.classLoader)
        this.share_info = `in`.readParcelable<CommonShare>(CommonShare::class.java.classLoader)
        this.showDislikeTagLayout = `in`.readByte() != 0.toByte()
        this.showDislikeResultLayout = `in`.readByte() != 0.toByte()
        this.url_ext = `in`.readString()
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<CommonVideo> = object : Parcelable.Creator<CommonVideo> {
            override fun createFromParcel(source: Parcel): CommonVideo {
                return CommonVideo(source)
            }

            override fun newArray(size: Int): Array<CommonVideo?> {
                return arrayOfNulls(size)
            }
        }
    }


    class PlayUrl {
        var ld_play_url_list = ArrayList<String>() // 流畅
        var nd_play_url_list = ArrayList<String>() // 标清
        var hd_play_url_list = ArrayList<String>() // 高清
        var sd_play_url_list = ArrayList<String>() // 超清
        //当前支持的清晰度列表，可能内容为："ld","sd","hd","nd"一项或多项
        var supportResolutionList = ArrayList<String>()

        fun defaultUrlList(): ArrayList<String> {
            if (nd_play_url_list.size > 0) {
                return nd_play_url_list
            } else if (sd_play_url_list.size > 0) {
                return sd_play_url_list
            } else if (hd_play_url_list.size > 0) {
                return hd_play_url_list
            } else if (ld_play_url_list.size > 0) {
                return ld_play_url_list
            } else {
                return ArrayList()
            }
        }

        fun hasUrl(): Boolean {
            if (defaultUrlList().size > 0) {
                return true
            }
            return false
        }

        override fun toString(): String {
            return "PlayUrl(hash:${hashCode()})(ld_play_url_list=$ld_play_url_list ${ld_play_url_list.hashCode()}, " +
                    "sd_play_url_list=$sd_play_url_list ${sd_play_url_list.hashCode()}, " +
                    "hd_play_url_list=$hd_play_url_list ${hd_play_url_list.hashCode()}, " +
                    "nd_play_url_list=$nd_play_url_list ${nd_play_url_list.hashCode()}, " +
                    "supportResolutionList=$supportResolutionList ${supportResolutionList.hashCode()})"
        }


    }
}
