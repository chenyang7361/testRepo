package com.mivideo.mifm

/**
 * 路由配置文件
 */
class RouterConf {
    companion object {
        /**
         * 跳转作者页所属路径
         */
        const val PATH_AUTHOR = "/view/author"

        /**
         * 跳转到Tab主页
         */
        const val PATH_TAB_HOST = "/view/tabHost"

        /**
         * 跳转我的界面
         */
        const val PATH_MINE = "/view/mine"

        /**
         * 跳转H5界面
         */
        const val PATH_WEB_H5 = "/view/h5"

        /**
         * 启动应用的host
         */
        const val HOST_LAUNCH = "launch"

        /**
         * 播放器的host
         */
        const val HOST_PLAY = "play"


        /**
         * 主host，新版路由协议都以此host为主
         */
        const val HOST_MAIN = "main"

        /**
         *web页所属host，新版路由协议web页面都以此host为主
         */
        const val HOST_WEB = "web"


    }

    class Routers {
        companion object {
            const val PLAY = "play"
            const val PLAY_ARG_RADIO_ID = "radioId"
            const val PLAY_ARG_FROM = "from"
        }
    }
}
