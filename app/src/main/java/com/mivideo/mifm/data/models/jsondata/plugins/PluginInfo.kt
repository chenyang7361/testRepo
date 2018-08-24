package com.mivideo.mifm.data.models.jsondata.plugins

import java.io.Serializable

class PluginInfo : Serializable {
    var id: String = ""
    var pluginEnable: Boolean = false
    var pluginStandAlone: Boolean = false
    var pluginVersion: String = ""
    var pluginPath: String = ""
    var pluginMd5: String = ""
}