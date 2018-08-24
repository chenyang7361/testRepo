package com.mivideo.mifm.push

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import android.text.TextUtils
import android.util.Base64
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.BuildConfig
import com.mivideo.mifm.SpManager
import com.xiaomi.channel.commonutils.logger.LoggerInterface
import com.xiaomi.mipush.sdk.Logger
import com.xiaomi.mipush.sdk.MiPushClient
import timber.log.Timber
import java.io.UnsupportedEncodingException

/**
 * Created by xingchang on 17/2/23.
 */
class MiPushManager : KodeinInjected {
    override val injector = KodeinInjector()

    companion object {
        fun get(): MiPushManager {
            return MiPushManager.Inner.anotherSingle
        }
    }

    private object Inner {
        val anotherSingle = MiPushManager()
    }

    private lateinit var mAppContext: Context

    private val spManager: SpManager by instance()

    fun init(context: Context) {
        mAppContext = context.applicationContext
        inject(mAppContext.appKodein())
    }

    fun registerMiPush() {
        if (!spManager.enablePush)
            return
        if (shouldInit()) {
            MiPushClient.registerPush(mAppContext, BuildConfig.XIAOMI_APPID, BuildConfig.XIAOMI_APPKEY)
//            if (!BuildConfig.IS_MIVIDEO_VERSION) {
//                MiPushClient.subscribe(mAppContext, "kuaiest_topic", null)
//                MiPushClient.setAlias(mAppContext, "kuaiest_alias", null)
//            }
        }

        val newLogger = object : LoggerInterface {

            override fun setTag(tag: String) {
                // ignore
            }

            override fun log(content: String, t: Throwable) {
            }

            override fun log(content: String) {
                Timber.i({ content }.invoke())
            }
        }
        Logger.setLogger(mAppContext, newLogger)
    }

    fun unregisterMiPush() {
        MiPushClient.unregisterPush(mAppContext)
    }

    fun getRegId(): String {
        val regId = MiPushClient.getRegId(mAppContext)
        if (!TextUtils.isEmpty(regId)) {
            try {
                return Base64.encodeToString(regId.toByteArray(charset("UTF-8")), Base64.DEFAULT)
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }
        return ""
    }

    private fun shouldInit(): Boolean {
        val am = mAppContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processInfos = am.runningAppProcesses
        val mainProcessName = mAppContext.packageName
        val myPid = Process.myPid()
        for (info in processInfos) {
            if (info.pid == myPid && mainProcessName == info.processName) {
                return true
            }
        }
        return false
    }
}
