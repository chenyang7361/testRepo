package com.mivideo.mifm.data.models.jsondata.common

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by aaron on 2017/4/20.
 */
class CommonShare : Parcelable {
    var description: String = ""
    var share_url: String = ""
    var status_code: Int = 0

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.description)
        dest.writeString(this.share_url)
        dest.writeInt(this.status_code)
    }

    constructor() {}

    protected constructor(`in`: Parcel) {
        this.description = `in`.readString()
        this.share_url = `in`.readString()
        this.status_code = `in`.readInt()
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<CommonShare> = object : Parcelable.Creator<CommonShare> {
            override fun createFromParcel(source: Parcel): CommonShare {
                return CommonShare(source)
            }

            override fun newArray(size: Int): Array<CommonShare?> {
                return arrayOfNulls(size)
            }
        }
    }
}
