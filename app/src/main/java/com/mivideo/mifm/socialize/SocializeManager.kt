package com.mivideo.mifm.socialize

import android.app.Activity
import android.content.Intent
import com.mivideo.mifm.socialize.internel.QQSocializer
import com.mivideo.mifm.socialize.internel.WeiboSocializer
import com.mivideo.mifm.socialize.internel.WxSocializer
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


/**
 * 社交化工具(微信，微博，QQ等)统一管理类
 *
 * 社交化相关的操作都通过此类来管理，是整个社交化模块的入口
 *
 * @see <a href="http://git.mvideo.xiaomi.srv/videodaily/videodaily-android/wikis/social-module">社交化模块文档</a>
 * @author LiYan
 */
class SocializeManager private constructor(val activity: Activity) {

    companion object {

        val FROM_WB = "sfrom=wb"
        val FROM_WX = "sfrom=wx"
        val FROM_WF = "sfrom=wf"
        val FROM_QQ = "sfrom=qq"
        val FROM_QS = "sfrom=qs"

        val SHARE_IMAGE_WIDTH = 150
        val SHARE_IMAGE_HEIGHT = 150


        private var instances: HashMap<Activity, SocializeManager> = HashMap()

        fun get(activity: Activity): SocializeManager {
            if (instances.get(activity) == null) {
                val socializeManager = SocializeManager(activity)
                instances.put(activity, socializeManager)
            }
            return instances.get(activity)!!
        }
    }

    private var wx: WxSocializer? = null
    private var qq: QQSocializer? = null
    private var weibo: WeiboSocializer? = null

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        wx?.handleActivityResult(requestCode, resultCode, data)
        qq?.handleActivityResult(requestCode, resultCode, data)
        weibo?.handleActivityResult(requestCode, resultCode, data)
    }


    fun handleNewIntent(intent: Intent?) {
        wx?.handleNewIntent(intent)
        qq?.handleNewIntent(intent)
        weibo?.handleNewIntent(intent)
    }

    fun wx(): WxSocializer {
        if (wx == null) {
            wx = WxSocializer(activity)
        }
        return wx!!
    }

    fun qq(): QQSocializer {
        if (qq == null) {
            qq = QQSocializer(activity)
        }
        return qq!!
    }

    fun weibo(): WeiboSocializer {
        if (weibo == null) {
            weibo = WeiboSocializer(activity)
        }
        return weibo!!
    }

    /**
     * 释放引用的资源，用以释放内存或防止内存泄露。
     * 通常此方法在Ui销毁的生命周期方法中调用，如Activity的onDestroy()方法或
     * Fragment的onDetach()方法
     */
    fun release() {
        wx?.release()
        wx = null

        weibo?.release()
        weibo = null

        qq?.release()
        qq = null

        instances.remove(activity)
    }
}