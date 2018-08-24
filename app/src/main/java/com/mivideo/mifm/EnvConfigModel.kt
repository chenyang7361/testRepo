package com.mivideo.mifm

import android.content.Context

class EnvConfigModel(context: Context) {
    companion object {
        private const val STRING_SERVER_URL = "server_url"
        private const val STRING_ACCOUNT_URL = "account_url"
        private const val SESSION_SERVER_URL = "session_url"
        private const val BOOLEAN_PRINT_LOG = "print_log"
        private const val BOOLEAN_MOCK_AD = "mock_ad"
        private const val STRING_HISTORY_URL = "bridge_history_url"

    }

    private val spUtil: SpManager = SpManager(context.applicationContext)

    /**
     * 当前服务器使用的地址
     */
    var serverUrl: String
        get() = spUtil.get(STRING_SERVER_URL, "")
        set(value) = spUtil.save(STRING_SERVER_URL, value)

    /**
     * 当前服务器用户登录服务使用的地址
     */
    var accountUrl: String
        get() = spUtil.get(STRING_ACCOUNT_URL, "")
        set(value) = spUtil.save(STRING_ACCOUNT_URL, value)

    var sessionUrl: String
        get() = spUtil.get(SESSION_SERVER_URL, "")
        set(value) = spUtil.save(SESSION_SERVER_URL, value)

    /**
     * 日志打印开关
     */
    var isOpenLog: Boolean
        get() = spUtil.getBoolean(BOOLEAN_PRINT_LOG, BuildConfig.DEBUG)
        set(value) = spUtil.save(BOOLEAN_PRINT_LOG, value)

    /**
     * 播放器广告数据模拟
     */
    var isAdDataMock: Boolean
        get() = spUtil.getBoolean(BOOLEAN_MOCK_AD, false)
//        get() = true
        set(value) = spUtil.save(BOOLEAN_MOCK_AD, value)

    /**
     * JsBridge测试历史URL
     */
    var historyUrl: String
        get() = spUtil.get(STRING_HISTORY_URL, "")
        set(value) = spUtil.save(STRING_HISTORY_URL, value)
}
