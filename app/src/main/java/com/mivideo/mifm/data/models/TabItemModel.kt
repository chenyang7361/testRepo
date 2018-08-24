package com.mivideo.mifm.data.models

import com.sabres.SabresObject

class TabItemModel : SabresObject() {

    var id: String
        get() = getString("id")
        set(value) = put("id", value)

    var name: String
        get() = getString("tag")
        set(value) = put("tag", value)

    var icon: String?
        get() = getString("icon")
        set(value) = put("icon", value)
}
