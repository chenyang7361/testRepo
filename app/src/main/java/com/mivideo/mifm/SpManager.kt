package com.mivideo.mifm

import android.content.Context
import android.content.SharedPreferences
import com.f2prateek.rx.preferences.RxSharedPreferences

class SpManager(val mContext: Context) {
    object Key {
        const val STRING_LONGITUDE = "longitude"
        const val STRING_LATITUDE = "latitude"
        const val STRING_DISLIKE_TAGS = "dislike_tags"
        const val STRING_REPORT_TAGS = "report_tags"

        const val FIRST_SUBSCRIBE = "first_subscribe"
        const val FIRST_COMMENT = "first_comment"

        const val VIDEO_DETAIL_HOME_CLICK = "video_detail_click"
        const val STRING_VIDEO_RESOLUTION: String = "video_resolution"
        // 新手引导弹窗显示的次数
        const val GUIDE_DIALOG_SHOW_COUNT = "guide_dialog_show_count"
    }

    private val preferences: RxSharedPreferences
    private var sp: SharedPreferences

    init {
        val packageName = mContext.packageName
        sp = mContext.getSharedPreferences(packageName + "_preferences", Context.MODE_PRIVATE)
        preferences = RxSharedPreferences.create(sp)
    }

    /**
     * 设置界面-热门推送开关
     */
    var enablePush: Boolean
        get() = getBoolean("enable_push", true)
        set(value) = save("enable_push", value)

    /**
     * 设置界面-播放器小窗模式
     */
    var enableMiniMode: Boolean
        get() = getBoolean("enable_player_mini", false)
        set(value) = save("enable_player_mini", value)

    /**
     * 设置界面-使用手机流量播放视频开关
     */
    var noWifiPlay: Boolean
        get() = getBoolean("no_wify_play", false)
        set(value) = save("no_wify_play", value)

    /**
     * 是否显示过小窗操作引导（首次使用app会显示引导）
     */
    var showedGuide: Boolean
        get() = getBoolean("show_guide", false)
        set(value) = save("show_guide", value)

    /**
     * 是否显示过下拉变换小船引导
     */
    var showedDropDownGuide: Boolean
        get() = getBoolean("show_drop_down_guide", false)
        set(value) = save("show_drop_down_guide", value)

    /**
     * 应用启动保存的经度
     */
    var longitude: String
        get() = get(Key.STRING_LONGITUDE, "")
        set(value) = save(Key.STRING_LONGITUDE, value)

    /**
     * 应用启动保存的维度
     */
    var latitude: String
        get() = get(Key.STRING_LATITUDE, "")
        set(value) = save(Key.STRING_LATITUDE, value)

    /**
     * 视频不敢兴趣操作网络缓存的tags
     */
    var dislikeTags: String
        get() = get(Key.STRING_DISLIKE_TAGS, "")
        set(value) = save(Key.STRING_DISLIKE_TAGS, value)

    /**
     * 视频举报操作网络缓存的tags
     */
    var reportTags: String
        get() = get(Key.STRING_REPORT_TAGS, "")
        set(value) = save(Key.STRING_REPORT_TAGS, value)

    /**
     * 记录当前用户是否第一次订阅频道
     */
    var firstSubscribe: Boolean
        get() = getBoolean(Key.FIRST_SUBSCRIBE, true)
        set(value) = save(Key.FIRST_SUBSCRIBE, value)

    /**
     * 记录当前用户是否第一次评论
     */
    var firstComment: Boolean
        get() = getBoolean(Key.FIRST_COMMENT, true)
        set(value) = save(Key.FIRST_COMMENT, value)

    /**
     * 播放详情页界面home按钮点击事件记录
     */
    var videoDetailClick: String
        get() = get(Key.VIDEO_DETAIL_HOME_CLICK, "")
        set(value) = save(Key.VIDEO_DETAIL_HOME_CLICK, value)

    /**
     * 用户上次选择的视频清晰度
     */
    var videoResolutionChoosed: String
        get() = get(Key.STRING_VIDEO_RESOLUTION, "")
        set(value) = save(Key.STRING_VIDEO_RESOLUTION, value)


    fun save(key: String, value: String) {
        val edit = sp.edit()
        edit.putString(key, value)
        edit.apply()
    }

    fun save(key: String, value: Boolean) {
        val edit = sp.edit()
        edit.putBoolean(key, value)
        edit.apply()
    }

    fun save(key: String, value: Long) {
        val edit = sp.edit()
        edit.putLong(key, value)
        edit.apply()
    }

    fun save(key: String, value: Int) {
        val edit = sp.edit()
        edit.putInt(key, value)
        edit.apply()
    }

    fun remove(key: String) {
        val edit = sp.edit()
        edit.remove(key)
        edit.apply()
    }

    fun get(key: String, default: String): String = sp.getString(key, default)

    fun getBoolean(key: String, default: Boolean): Boolean = sp.getBoolean(key, default)

    fun getLong(key: String, default: Long) = sp.getLong(key, default)

    fun getInt(key: String, default: Int) = sp.getInt(key, default)
}
