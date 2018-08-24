package com.mivideo.mifm.data.models.jsondata

import android.os.Parcel
import android.os.Parcelable

class MediaDetailResult {
    var code: Int = 0
    var data: MediaDetailData? = null
}

class MediaDetailData() : AlbumInfo(), Parcelable {
    var sections: ArrayList<PassageItem> = ArrayList()
    var has_next: Boolean = true

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        title = parcel.readString()
        author = parcel.readString()
        desc = parcel.readString()
        price = parcel.readFloat()
        cid = parcel.readString()
        num = parcel.readString()
        cover = parcel.readString()
        listenTimes = parcel.readInt()
        updated_at = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(author)
        parcel.writeString(desc)
        parcel.writeFloat(price)
        parcel.writeString(cid)
        parcel.writeString(num)
        parcel.writeString(cover)
        parcel.writeInt(listenTimes)
        parcel.writeString(updated_at)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MediaDetailData> {
        override fun createFromParcel(parcel: Parcel): MediaDetailData {
            return MediaDetailData(parcel)
        }

        override fun newArray(size: Int): Array<MediaDetailData?> {
            return arrayOfNulls(size)
        }
    }
}

data class PassageItem(
        var id: String = "",
        var name: String = "",
        var url: String = "",
        var seconds: Int = 0,
        var duration: String = "",
        var headline: Int = 0) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(url)
        parcel.writeInt(seconds)
        parcel.writeString(duration)
        parcel.writeInt(headline)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PassageItem> {
        override fun createFromParcel(parcel: Parcel): PassageItem {
            return PassageItem(parcel)
        }

        override fun newArray(size: Int): Array<PassageItem?> {
            return arrayOfNulls(size)
        }
    }
}
