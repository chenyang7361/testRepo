package com.mivideo.mifm.data.models.jsondata

/**
 *底部导航栏数据实体类
 */

data class BottomTabEntity(var common: CommonEntity? = null,
                           var times: TimesEntity? = null,
                           var data: List<DataEntity>? = null) {

    data class CommonEntity(var exp: String? = null)

    data class DataEntity(var _id: String? = null,
                          var name: String? = null,
                          var order: Int = 0,
                          var alias: String? = null,
                          var icon: String? = null,
                          var icon_checked: String? = null,
                          var action: String? = null,
                          var red_point: Boolean = false,
                          var status: Int = 0,
                          var tabPosition: Int = 0,
                          var itemPosition: Int = 0
    )

    data class TimesEntity(var created: Int = 0,
                           var updated: Int = 0
    )
}
