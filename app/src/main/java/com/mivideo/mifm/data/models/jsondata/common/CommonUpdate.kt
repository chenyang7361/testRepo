package com.mivideo.mifm.data.models.jsondata.common

/**
 * Created by aaron on 2016/11/16.
 *
 * 升级
 */

class CommonUpdateResult {
    var code: Int = 0
    var data: CommonUpdate? = null
    override fun toString(): String {
        return "CommonUpdateResult(code=$code, data=$data)"
    }

}

class CommonUpdate {
    var url: String = ""
    var version_code: Int = 0
    var title: String = ""
    var force: Boolean = false
    var pkg_size: Float = 0f
    var desc: String = ""
    var version_name: String = ""
    override fun toString(): String {
        return "CommonUpdate(url='$url', version_code=$version_code, title='$title', force=$force, pkg_size=$pkg_size, desc='$desc', version_name='$version_name')"
    }


}
