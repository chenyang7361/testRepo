package com.mivideo.mifm

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.StrictMode
import android.support.multidex.MultiDex
import android.support.v7.app.AppCompatDelegate
import android.text.TextUtils
import com.alibaba.android.arouter.launcher.ARouter
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.lazy
import com.mivideo.mifm.account.AccountInfo
import com.mivideo.mifm.account.UserAccountManager
import com.mivideo.mifm.account.UserAccountUpdateListener
import com.mivideo.mifm.di.netModule
import com.mivideo.mifm.extensions.asyncTask
import com.mivideo.mifm.data.api.APIUrl
import com.mivideo.mifm.data.db.*
import com.mivideo.mifm.di.managerModule
import com.mivideo.mifm.di.repositoryModule
import com.mivideo.mifm.di.tuningModule
import com.mivideo.mifm.di.viewModelModule
import com.mivideo.mifm.push.MiPushManager
import com.mivideo.mifm.network.commonurl.NetworkParams
import com.mivideo.mifm.player.manager.MediaManager
import com.sabres.Sabres
import com.sabres.SabresObject
import com.squareup.leakcanary.LeakCanary
import me.yokeyword.fragmentation.Fragmentation
import okhttp3.OkHttpClient
import okhttp3.OkUrlFactory
import timber.log.Timber
import java.net.URL

class MainApp : Application(), KodeinAware {

    override val kodein by Kodein.lazy {
        bind<Application>() with instance(this@MainApp)
        bind<Context>("appContext") with instance(this@MainApp)
        import(netModule)
        import(repositoryModule)
        import(viewModelModule)
        import(managerModule)
        import(tuningModule)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }
//        enabledStrictMode()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        initConfig()
        NetworkManager.init(applicationContext)
        MediaManager.initContext(this@MainApp)
        initARouter()
        initDebugTool()
        initPush()
        initDBObject()
        initStatistics()
        initUserData()
        initFragmentation()
        initDownloader()
        initCrashlytics()
    }

    private fun initConfig() {
        val configModel = EnvConfigModel(applicationContext)
        val url = configModel.serverUrl
        if (!TextUtils.isEmpty(url)) {
            APIUrl.BASE_URL = url
        }
        val accountUrl = configModel.accountUrl
        if (!TextUtils.isEmpty(accountUrl)) {
            APIUrl.DOMAIN_ACCOUNT = accountUrl
        }
        MainConfig.PRINT_LOG = configModel.isOpenLog
    }

    private fun initARouter() {
        if (BuildConfig.DEBUG) {
            ARouter.openDebug()
            ARouter.openLog()
        }
        ARouter.init(this)
    }

    private fun initFragmentation() {
        if (!BuildConfig.DEBUG) return
        Fragmentation.builder()
                // 设置 栈视图 模式为 悬浮球模式   SHAKE: 摇一摇唤出   NONE：隐藏
                .stackViewMode(Fragmentation.BUBBLE)
                // ture时，遇到异常："Can not perform this action after onSaveInstanceState!"时，会抛出
                // false时，不会抛出，会捕获，可以在handleException()里监听到
                .debug(BuildConfig.DEBUG)
                .install()
    }

    /**
     * debug工具类初始化统一放入此方法中，如内存泄露检测工具，日志统计等等
     */
    private fun initDebugTool() {
        asyncTask {
            LeakCanary.install(this)
            //Timber比较方便控制Debug和Release下日志打印
            Timber.plant(LogTree())
        }
    }

    /**
     * 初始化崩溃统计工具
     */
    private fun initCrashlytics() {
//        asyncTask {
//            if (!BuildConfig.DEBUG) {
//                val crashlytics = Crashlytics.Builder()
//                        .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
//                        .build()
//                //初始化崩溃统计工具
//                val fabric = Fabric.Builder(this)
//                        .kits(crashlytics)
//                        .debuggable(true)
//                        .build()
//                Fabric.with(fabric)
//            }
//        }
    }


    private fun enabledStrictMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            val strictMode = StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
            Timber.i({ "DEBUG: " + BuildConfig.DEBUG }.invoke())
            Timber.i({ "DEBUG: " + BuildConfig.CONFIG_DEBUG }.invoke())

            if (BuildConfig.DEBUG) {
                StrictMode.setThreadPolicy(strictMode.penaltyDeath().build())
            } else {
                StrictMode.setThreadPolicy(strictMode.build())
            }

            StrictMode.setVmPolicy(
                    StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog()
                            .build())
        }
    }

    /**
     * 初始化push管理类
     */
    private fun initPush() {
        asyncTask {
            MiPushManager.get().init(applicationContext)
            MiPushManager.get().registerMiPush()
        }
    }

    /**
     * 初始化统计工具
     */
    private fun initStatistics() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH) {
            URL.setURLStreamHandlerFactory(OkUrlFactory(OkHttpClient()))
        }

//        if (!BuildConfig.DEBUG) {
//            val channel = WalleChannelReader.getChannel(this.applicationContext)
//                    ?: BuildConfig.CHANNEL
//
//            // 艾瑞
//            HMTAgent.setChannelId(this, channel)
//            HMTAgent.Initialize(this)
//
//            // 百度
//            initBaiduStatistics(this, channel)
//
//            // 小米
//            initMiStatistics(this, channel)
//        }
//        Statistics.init(applicationContext)
    }

    /**
     * 初始化下载模块
     */
    private fun initDownloader() {
//        DownloadManager.get().init(applicationContext)
    }

    /**
     * 初始化DB操作对象
     */
    private fun initDBObject() {
        SabresObject.registerSubclass(DbTabList::class.java)
        SabresObject.registerSubclass(DbRecommendList::class.java)
        SabresObject.registerSubclass(DbChannelList::class.java)
        SabresObject.registerSubclass(DbAudioCache::class.java)
        SabresObject.registerSubclass(DbCache::class.java)
        SabresObject.registerSubclass(DbHistory::class.java)
        Sabres.initialize(applicationContext)
    }


    /**
     * 初始化用户信息
     */
    private fun initUserData() {
        val userAccountManager = kodein.instance<UserAccountManager>()
        asyncTask {
            if (userAccountManager.user() != null) {
                val userId = userAccountManager.user()!!.getUserId()
                val token = userAccountManager.user()!!.getAccessToken()
                NetworkParams.updateUser(userId, token)
            }

            userAccountManager.addOnAccountUpdatedListener(object : UserAccountUpdateListener {
                override fun onUserAccountUpdated(accountInfo: AccountInfo?) {
                    if (accountInfo != null) {
                        NetworkParams.updateUser(accountInfo.getUserId(), accountInfo.getAccessToken())
                    } else {
                        NetworkParams.updateUser("", "")
                    }
                }
            })
        }
    }

    class LogTree : Timber.DebugTree() {
        override fun isLoggable(priority: Int): Boolean {
            return MainConfig.PRINT_LOG
        }
    }
}
