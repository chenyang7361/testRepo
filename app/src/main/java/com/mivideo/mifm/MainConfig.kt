package com.mivideo.mifm

class MainConfig {
    companion object {
        /**
         * db中保存的视频列表最大值,即每一项Tab在本地最多保存的视频条数
         */
        val TOTAL_DB_VIDEO_SIZE: Int = 100
        /**
         * Feed流页面Page_size
         */
        val VIDEO_LIST_PAGE_SIZE: Int = 8
        /**
         * 轮询拉去消息数据时间间隔
         */
        val LOOP_UPDATE_NEWS_INTERVAL: Long = 5 * 60 * 1000
        /**
         * 主要用于保存搜索记录的条数，默认为6条，即当搜索条数大于6条时，第一条搜索记录会被覆盖
         */
        val SEARCH_QUEUE_HISTORY_SIZE: Int = 10
        /**
         * 搜索历史页面默认被展示的个数
         */
        val SEARCH_QUEUE_HISTORY_SHOW: Int = 3
        /**
         * 视频详情页面关联视频默认显示个数
         */
        val VIDEO_DETAIL_ABOUT_TOP_COUNT: Int = 8
        /**
         * 视频是否可分享
         */
        val VIDEO_CAN_SHARE: Int = 200
        /**
         * 外发版本主页面两次返回按键退出的时间间隔
         */
        val STANDARD_BACK_INTERVAL_TIME: Long = 3 * 1000
        /**
         * 外发版推荐Tab视频列表滚动到该位置时，执行底部Tab动画效果
         */
        val RECOMMEND_VISIBLE_ITEM_POSITION: Int = 4

        /**
         * 打印log开关
         */
        var PRINT_LOG = BuildConfig.DEBUG

        val INLINE_MINI_CODE = 1011

        val DEFAULT_FIRST_PAGE_INDEX: Int = 1
    }
}
