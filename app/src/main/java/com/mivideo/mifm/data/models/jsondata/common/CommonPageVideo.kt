package com.mivideo.mifm.data.models.jsondata.common

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by aaron on 2016/11/22.
 */
data class CommonPageVideo(var card_type: String = "",
                           var card_content: Int = 0,
                           var card_data: ArrayList<CommonVideo> = ArrayList(),
                           var card_display_type: String = "",
                           var card_sub_list: ArrayList<CommonPageVideo> = ArrayList()) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readInt(),
            source.createTypedArrayList(CommonVideo.CREATOR),
            source.readString(),
            ArrayList<CommonPageVideo>().apply { source.readList(this, CommonPageVideo::class.java.classLoader) }
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(card_type)
        writeInt(card_content)
        writeTypedList(card_data)
        writeString(card_display_type)
        writeList(card_sub_list)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CommonPageVideo> = object : Parcelable.Creator<CommonPageVideo> {
            override fun createFromParcel(source: Parcel): CommonPageVideo = CommonPageVideo(source)
            override fun newArray(size: Int): Array<CommonPageVideo?> = arrayOfNulls(size)
        }
    }
}

/**
 * 首页card类型签到header
 */
val CARD_TYPE_BONUS_HEADER = "card_type_bonus_header"

val CARD_CONTENT_VIDEO = 0
val CARD_CONTENT_EMBED = 3