package com.mivideo.mifm.data.models.jsondata.common

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.mivideo.mifm.network.commonurl.NetworkParams
import java.util.*

/**
 * Created by aaron on 2016/11/16.
 */
class CommonStat : Parcelable {
    var category: String = ""
    var position: String = ""
    var type: String = ""
    var display_type: String = ""
    var vid: String = ""
    var eid: String = ""
    var trace_id: String = ""
    var rec_queue_name: String = ""
    var rec_type: String = ""
    var rec_time: Long = 0
    var rec_position: Long = 0
    var realTraceId: String = ""
    var logTime: Long = 0
    var firstExposeTime: Long = 0
    var relate = false
    var onceState: OnceReport = OnceReport()

    override fun describeContents(): Int {
        return 0
    }

    fun getRealTraceId(context: Context) : String {
        if (TextUtils.isEmpty(realTraceId)) {
            if (TextUtils.isEmpty(trace_id) || isQuality()) {
                var header = NetworkParams.getIMEI(context.applicationContext)
                if (TextUtils.isEmpty(header)) {
                    header = NetworkParams.getDeviceMd5Id(context.applicationContext)
                }
                val timestamp = System.currentTimeMillis().toString()
                val random = Random().nextInt(Int.MAX_VALUE).toString()
                realTraceId = header + "_" + timestamp + "_" + random
            } else {
                realTraceId = trace_id
            }
        }
        return realTraceId
    }

    fun isQuality() : Boolean {
        return "99:99:99:99".equals(eid)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.category)
        dest.writeString(this.position)
        dest.writeString(this.type)
        dest.writeString(this.display_type)
        dest.writeString(this.vid)
        dest.writeString(this.eid)
        dest.writeString(this.trace_id)
        dest.writeLong(this.logTime)
        dest.writeLong(this.firstExposeTime)
    }

    constructor() {
    }

    constructor(`in`: Parcel) {
        this.category = `in`.readString()
        this.position = `in`.readString()
        this.type = `in`.readString()
        this.display_type = `in`.readString()
        this.vid = `in`.readString()
        this.eid = `in`.readString()
        this.trace_id = `in`.readString()
        this.logTime = `in`.readLong()
        this.firstExposeTime = `in`.readLong()
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<CommonStat> = object : Parcelable.Creator<CommonStat> {
            override fun createFromParcel(source: Parcel): CommonStat {
                return CommonStat(source)
            }

            override fun newArray(size: Int): Array<CommonStat?> {
                return arrayOfNulls(size)
            }
        }
    }

    class OnceReport {
        var url : String = ""
        var export: Boolean = false
        var click : Boolean = false

        var e_t = 0L                             // 曝光时间
        var is_clk = 0                           // 是否点击, 0:未点击， 1: 点击
        var c_t = 0L                             // 客户端点击时间，如果点击，则上报，未点击，上报0
        var is_view = 0                          // 是否浏览/播放: 0:未浏览， 1: 浏览
        var v_d = 0L                             // 浏览/播放时长，未浏览，则上报0
        var is_dislike = 0                       // 是否触发了负反馈
        var dislike_reason = ArrayList<String>() // 负反馈的内容
        var pos = 0                              // 该stock_id 在页面中的位置，相对于顶部的绝对位置
        var duration = -1                        // 时常 秒

        fun addDisLikeReason(list: ArrayList<String>) {
            is_dislike = 1
            dislike_reason = list
        }
    }
}
