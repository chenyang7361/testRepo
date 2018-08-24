package com.mivideo.mifm.data.models.jsondata.common

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by aaron on 2016/11/16.
 */
class CommonAuthor : Parcelable {
    var id: String = ""
    var _id: String = ""
    var name: String = ""
    var poster_url: String = ""
    var desc: String = ""
    var cp: String = ""
    var cp_name: String = ""
    var subscribed : Int = 0
    var sub_count : Int = 0
    var videoId: String = ""
    var video_count: Int = 0
    var author_type: Int = 0

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.id)
        dest.writeString(this._id)
        dest.writeString(this.name)
        dest.writeString(this.poster_url)
        dest.writeString(this.desc)
        dest.writeString(this.cp)
        dest.writeString(this.cp_name)
        dest.writeInt(this.subscribed)
        dest.writeInt(this.sub_count)
        dest.writeString(this.videoId)
        dest.writeInt(this.video_count)
        dest.writeInt(this.author_type)
    }

    constructor() {
    }

    constructor(`in`: Parcel) {
        this.id = `in`.readString()
        this._id = `in`.readString()
        this.name = `in`.readString()
        this.poster_url = `in`.readString()
        this.desc = `in`.readString()
        this.cp = `in`.readString()
        this.cp_name = `in`.readString()
        this.sub_count = `in`.readInt()
        this.subscribed = `in`.readInt()
        this.videoId = `in`.readString()
        this.video_count = `in`.readInt()
        this.author_type = `in`.readInt()
    }

    companion object {
        const val SUBSRIBE_STATUS_SUBSCRIBED = 0

        @JvmField val CREATOR: Parcelable.Creator<CommonAuthor> = object : Parcelable.Creator<CommonAuthor> {
            override fun createFromParcel(source: Parcel): CommonAuthor {
                return CommonAuthor(source)
            }

            override fun newArray(size: Int): Array<CommonAuthor?> {
                return arrayOfNulls(size)
            }
        }
    }
}

/**
 * 作者类型：
 * 短视频作者
 * 小视频作者
 */
const val AUTHOR_TYPE_SHORT = 0
const val AUTHOR_TYPE_SMALL = 1