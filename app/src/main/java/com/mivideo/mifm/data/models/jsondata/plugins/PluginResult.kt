package com.mivideo.mifm.data.models.jsondata.plugins

import java.util.*

class PluginResult {
    var data: CpPluginData = CpPluginData()
    override fun toString(): String {
        return "PluginResult(data=$data)"
    }


}

class CpPluginData {
    var result: String = ""
    var cp_plugin = ArrayList<Plugin>()

    override fun toString(): String {
        return "CpPluginData(result='$result', cp_plugin=$cp_plugin)"
    }


}

class Plugin {
    var _id: String = ""
    var md5: String = ""
    var cp: String = ""
    var url: String = ""
    var path: String = ""
    var hint: String = ""
    var toast: String = ""

    override fun toString(): String {
        return "Plugin(_id='$_id', md5='$md5', cp='$cp', url='$url', path='$path', hint='$hint', toast='$toast')"
    }


}