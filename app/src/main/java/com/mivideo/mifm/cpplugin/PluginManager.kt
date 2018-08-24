package com.mivideo.mifm.cpplugin

import android.compact.impl.TaskPayload
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import com.github.salomonbrys.kodein.instance
import com.google.gson.Gson
import com.mivideo.mifm.data.models.jsondata.plugins.Plugin
import com.mivideo.mifm.data.models.jsondata.plugins.PluginResult
import com.mivideo.mifm.MainApp
import com.mivideo.mifm.data.api.APIUrl
import com.mivideo.mifm.data.models.jsondata.common.CommonVideo
import com.mivideo.mifm.data.models.jsondata.plugins.*
import com.mivideo.mifm.data.repositories.MainRepository
import com.mivideo.mifm.download.VideoDefinition
import com.mivideo.mifm.network.commonurl.NetworkParams
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.util.app.showToast
import com.limpoxe.fairy.core.FairyGlobal
import com.limpoxe.fairy.core.PluginIntentResolver
import com.limpoxe.fairy.manager.PluginManagerHelper
import com.limpoxe.fairy.util.CheckUtil
import com.mivideo.mifm.EnvConfigModel
import com.mivideo.mifm.NetworkManager
import com.mivideo.mifm.cpplugin.support.DownloadAdapter
import com.mivideo.mifm.cpplugin.support.PluginDownloadManager
import rx.Observable
import timber.log.Timber
import java.io.File
import java.util.HashMap
import java.util.concurrent.CopyOnWriteArrayList

class PluginManager(val mContext: Context) {

    private var TAG = "PM"
    private var mConnection = Connection(60000L)

    // CMS没有测试地址，所以用下面的测试地址，必须测试验证正常再上CMS，因为CMS一旦部署就是现网数据，切记
    internal var DEBUG_UPDATE_URL = "http://45.32.40.65/g/fetch_plugin"
    private var savedPlugin: CopyOnWriteArrayList<Plugin> = CopyOnWriteArrayList()
    private val mainApp = mContext.applicationContext as MainApp
    private val mainRepo: MainRepository = mainApp.kodein.instance()

    /**
     * 根据当前网络，选择更新策略
     */
    fun migratePluginsByNetworkState(context: Context, checkCache: Boolean) {
        var isUseMobile = NetworkManager.isUseMobileNetConnected(context)
        if (isUseMobile) {
            // 如果当前网络是数据，为了减少带宽占用，只更新安装过的
            checkAndUpdatePlugins(context, checkCache)
        } else {
            // 如果当前无网络或Wifi，没安装的安装，需要更新的更新（有无网处理）
            checkAndInstallPlugins(context, checkCache)
        }
    }

    /**
     * 只更新安装过的
     */
    fun checkAndUpdatePlugins(context: Context, checkCache: Boolean) {
        // 渠道插件的检查安装处理
        val cacheExist = infoCacheExist()
        if (!checkCache || (checkCache && !cacheExist)) {
            // 1.如果不检查缓存则直接启动下载校验流程
            // 2.如果检查缓存且缓存无效，也启动下载校验流程
            checkAndUpdatePlugins(context)
                    .compose(asyncSchedulers())
                    .subscribe ({ plugin ->
                        Log.d(TAG, "Result|${plugin._id}|${plugin.hint}")
                        if (!TextUtils.isEmpty(plugin.toast)) {
                            Log.d(TAG, "Toast|checkAndUpdatePlugins|${plugin._id}|${plugin.toast}")
                            showToast(context.applicationContext, plugin.toast)
                        }
                    }, {
                        Timber.e(it)
                    })
        }
        // 后续类型插件的检查安装处理
    }

    /**
     * 没安装的安装，需要更新的更新
     */
    fun checkAndInstallPlugins(context: Context, checkCache: Boolean) {
        // 渠道插件的检查安装处理
        val cacheExist = infoCacheExist()
        if (!checkCache || (checkCache && !cacheExist)) {
            // 1.如果不检查缓存则直接启动下载校验流程
            // 2.如果检查缓存且缓存无效，也启动下载校验流程
            checkAndInstallPlugins(context)
                    .compose(asyncSchedulers())
                    .subscribe ({ plugin ->
                        Log.d(TAG, "Result|${plugin._id}|${plugin.hint}")
                        if (!TextUtils.isEmpty(plugin.toast)) {
                            Log.d(TAG, "Toast|checkAndInstallPlugins|${plugin._id}|${plugin.toast}")
                            showToast(context.applicationContext, plugin.toast)
                        }
                    }, {
                        Timber.e(it)
                    })
        }
        // 后续类型插件的检查安装处理
    }

    @Synchronized
    fun syncProcessTaskPayload(context: Context, payload: TaskPayload?, type: String): TaskPayload {
        var result = TaskPayload()
        var networkConnected = NetworkManager.isNetworkConnected(context.applicationContext)
        if (!networkConnected) {
            result.state = TaskPayloadState.NETWORK_INVALID.code()
            return result
        }
        if (payload == null) {
            result.state = TaskPayloadState.NULL.code()
            return result
        }
        if (TextUtils.isEmpty(payload.identify)) {
            payload.state = TaskPayloadState.INIT_INVALID_ID.code()
            return payload
        }
        if (TextUtils.isEmpty(payload.to)) {
            payload.state = TaskPayloadState.INIT_INVALID_TO.code()
            return payload
        }
        val installed = CheckUtil.checkPluginInstalled(payload.to)
        if (!installed) {
            payload.state = TaskPayloadState.TARGET_PLUGIN_NOT_FOUND.code()
            return payload
        } else {
            var ver = ""
            var found = false
            for (pd in PluginManagerHelper.getPlugins()) {
                if (pd != null && pd.packageName == payload.to) {
                    ver = pd.version
                    found = true
                    break
                }
            }
            if (!found) {
                payload.state = TaskPayloadState.TARGET_PLUGIN_NOT_FOUND.code()
                Log.d(TAG, "Illegal: ${payload.to} not in plugin list!")
                return payload
            }
            payload.tag = ver
        }
        if (TextUtils.isEmpty(payload.from)) {
            payload.from = context.packageName
        }
        if (TextUtils.isEmpty(payload.ch)) {
            payload.ch = PluginUtil.randomString(8)
        }

        var filter = PacketIDFilter(payload.ch)
        var collector = mConnection.createPacketCollector(filter)

        val success = sendPayloadToPlugin(context, payload, type)
        if (!success) {
            result.state = TaskPayloadState.TARGET_PLUGIN_NOT_FOUND.code()
            return result
        }
        var newPacket = collector.nextResult(mConnection.safetyTimeOut)
        collector.cancel()
        if (newPacket == null || newPacket.content == null) {
            Log.d(TAG, "Illegal: newPacket.content")
            return result
        }
        if (payload.identify == newPacket.content.identify) {
            result = newPacket.content
        } else {
            Log.d(TAG, "Illegal: newPacket.content.identify")
        }
        return result
    }

    fun receiveFromPlugin(context: Context, payload: TaskPayload?) {
        if (payload == null || TextUtils.isEmpty(payload.identify)) {
            return
        }
        Log.d(TAG, "${payload.type} Receive|ch|${payload.ch}|id|${payload.identify}|to|${payload.to}")
        var packet = Packet()
        packet.content = payload
        mConnection.processPacket(packet)
    }

    fun sendPayloadToPlugin(context: Context, payload: TaskPayload, type: String?): Boolean {
        return if ("submit".equals(type)) {
            submitTask(context, payload, payload.to)
        } else if ("change".equals(type)) {
            changeTask(context, payload, payload.to)
        } else if ("callback".equals(type)) {
            callbackTask(context, payload, payload.to)
        } else if ("query".equals(type)) {
            queryTask(context, payload, payload.to)
        } else {
            submitTask(context, payload, payload.to)
        }
    }

    private fun submitTask(context: Context?, payload: TaskPayload?, pluginPackageName: String?): Boolean {
        if (context == null || payload == null || pluginPackageName == null) {
            Log.d(TAG, "submitTask|invalidInput")
            return false
        }
        var success: Boolean = false
        payload.type = "submit"
        payload.auth = context.packageName + ".auth.HOST_PROVIDER"
        val intent = Intent(pluginPackageName + ".action.PLUGIN_INTENT_SERVICE")
        intent.`package` = pluginPackageName
        if (!CheckUtil.checkIntentHasHandle(context, intent)) {
            Log.d(TAG, "submitTask|IntentHasNoHandle|${intent.toUri(Intent.URI_INTENT_SCHEME)}")
            return false
        }
        if (!CheckUtil.checkPluginReadyByClassName(pluginPackageName + ".PluginIntentService")) {
            Log.d(TAG, "submitTask|PluginNotReady|${pluginPackageName}.PluginIntentService")
            return false
        }
        intent.putExtra("taskpayload", payload as Parcelable?)
        val enableLogcat = EnvConfigModel(context.applicationContext).isOpenLog
        if (enableLogcat) {
            intent.putExtra("enableLogcat", true)
        }
        intent.putExtra("baseurl", APIUrl.BASE_URL)
        try {
            Log.d(TAG, "submitTask|ch|${payload.ch}|id|${payload.identify}|to|${pluginPackageName}")
            if (FairyGlobal.hasPluginFilter() && FairyGlobal.filterPlugin(intent)) {
                PluginIntentResolver.resolveService(intent)
            }
            context.startService(intent)
            success = true
        } catch (e: Exception) {
            success = false
            Log.d(TAG, "Exception|${e.message}")
            e.printStackTrace()
        }
        return success
    }

    fun callbackTask(context: Context?, payload: TaskPayload?, pluginPackageName: String?): Boolean {
        if (context == null || payload == null || pluginPackageName == null) {
            Log.d(TAG, "callbackTask|invalidInput")
            return false
        }
        var success: Boolean = false
        payload.type = "callback"
        payload.auth = context.packageName + ".auth.HOST_PROVIDER"
        val intent = Intent(pluginPackageName + ".action.PLUGIN_INTENT_SERVICE")
        intent.`package` = pluginPackageName
        if (!CheckUtil.checkIntentHasHandle(context, intent)) {
            Log.d(TAG, "callbackTask|IntentHasNoHandle|" + intent.toUri(Intent.URI_INTENT_SCHEME))
            return false
        }
        if (!CheckUtil.checkPluginReadyByClassName(pluginPackageName + ".PluginIntentService")) {
            Log.d(TAG, "callbackTask|PluginNotReady|${pluginPackageName}.PluginIntentService")
            return false
        }
        intent.putExtra("taskpayload", payload as Parcelable?)
        val enableLogcat = EnvConfigModel(context.applicationContext).isOpenLog
        if (enableLogcat) {
            intent.putExtra("enableLogcat", true)
        }
        intent.putExtra("baseurl", APIUrl.BASE_URL)
        try {
            Log.d(TAG, "callbackTask|ch|${payload.ch}|id|${payload.identify}|to|${pluginPackageName}")
            if (FairyGlobal.hasPluginFilter() && FairyGlobal.filterPlugin(intent)) {
                PluginIntentResolver.resolveService(intent)
            }
            context.startService(intent)
            success = true
        } catch (e: Exception) {
            success = false
            Log.d(TAG, "Exception|${e.message}")
            e.printStackTrace()
        }
        return success
    }

    private fun changeTask(context: Context?, payload: TaskPayload?, pluginPackageName: String?): Boolean {
        if (context == null || payload == null || pluginPackageName == null) {
            Log.d(TAG, "changeTask|invalidInput")
            return false
        }
        var success: Boolean = false
        payload.type = "change"
        payload.auth = context.packageName + ".auth.HOST_PROVIDER"
        val intent = Intent(pluginPackageName + ".action.PLUGIN_INTENT_SERVICE")
        intent.`package` = pluginPackageName
        if (!CheckUtil.checkIntentHasHandle(context, intent)) {
            Log.d(TAG, "changeTask|IntentHasNoHandle|${intent.toUri(Intent.URI_INTENT_SCHEME)}")
            return false
        }
        if (!CheckUtil.checkPluginReadyByClassName(pluginPackageName + ".PluginIntentService")) {
            Log.d(TAG, "changeTask|PluginNotReady|${pluginPackageName}.PluginIntentService")
            return false
        }
        intent.putExtra("taskpayload", payload as Parcelable?)
        val enableLogcat = EnvConfigModel(context.applicationContext).isOpenLog
        if (enableLogcat) {
            intent.putExtra("enableLogcat", true)
        }
        intent.putExtra("baseurl", APIUrl.BASE_URL)
        try {
            Log.d(TAG, "changeTask|ch|${payload.ch}|id|${payload.identify}|to|${pluginPackageName}")
            if (FairyGlobal.hasPluginFilter() && FairyGlobal.filterPlugin(intent)) {
                PluginIntentResolver.resolveService(intent)
            }
            context.startService(intent)
            success = true
        } catch (e: Exception) {
            success = false
            Log.d(TAG, "Exception|${e.message}")
            e.printStackTrace()
        }
        return success
    }

    private fun queryTask(context: Context?, payload: TaskPayload?, pluginPackageName: String?): Boolean {
        if (context == null || payload == null || pluginPackageName == null) {
            Log.d(TAG, "queryTask|invalidInput")
            return false
        }
        var success: Boolean = false
        payload.type = "query"
        payload.auth = context.packageName + ".auth.HOST_PROVIDER"
        val intent = Intent(pluginPackageName + ".action.PLUGIN_INTENT_SERVICE")
        intent.`package` = pluginPackageName
        if (!CheckUtil.checkIntentHasHandle(context, intent)) {
            Log.d(TAG, "queryTask|IntentHasNoHandle|${intent.toUri(Intent.URI_INTENT_SCHEME)}")
            return false
        }
        if (!CheckUtil.checkPluginReadyByClassName(pluginPackageName + ".PluginIntentService")) {
            Log.d(TAG, "queryTask|PluginNotReady|${pluginPackageName}.PluginIntentService")
            return false
        }
        intent.putExtra("taskpayload", payload as Parcelable?)
        val enableLogcat = EnvConfigModel(context.applicationContext).isOpenLog
        if (enableLogcat) {
            intent.putExtra("enableLogcat", true)
        }
        intent.putExtra("baseurl", APIUrl.BASE_URL)
        try {
            Log.d(TAG, "queryTask|ch|${payload.ch}|id|${payload.identify}|to|${pluginPackageName}")
            if (FairyGlobal.hasPluginFilter() && FairyGlobal.filterPlugin(intent)) {
                PluginIntentResolver.resolveService(intent)
            }
            context.startService(intent)
            success = true
        } catch (e: Exception) {
            success = false
            Log.d(TAG, "Exception|${e.message}")
            e.printStackTrace()
        }
        return success
    }

    fun getMessageFromTaskPayloadStates(payloadState: Int?): String {
        var message = ""
        var tps: TaskPayloadState? = null
        for (state in TaskPayloadState.values()) {
            if (state.code() == payloadState) {
                tps = state
                break
            }
        }
        tps?.let {
            when (tps) {
                TaskPayloadState.SUCCESS -> message = "成功"
                TaskPayloadState.INIT_INVALID_ID -> message = "初始化对象非法id"
                TaskPayloadState.INIT_INVALID_TO -> message = "初始化对象非法接收方"
                TaskPayloadState.PROCESS_FAILURE -> message = "插件方解析结果参数非法"
                TaskPayloadState.TARGET_PLUGIN_NOT_SUPPORT -> message = "目标插件不支持操作"
                TaskPayloadState.TARGET_PLUGIN_NOT_FOUND -> message = "没有找到插件"
                TaskPayloadState.NETWORK_REQUEST_ERR -> message = "网络请求失败"
                TaskPayloadState.NETWORK_INVALID -> message = "网络无法连接"
                else -> message += tps.name
            }
        }
        return message
    }

    fun infoCacheExist(): Boolean {
        return savedPlugin != null && savedPlugin.size > 0
    }

    fun checkCpNeedPlugin(context: Context, cp: String): Observable<Boolean> {
        Log.d(TAG, "checkCpNeedPlugin|cp|${cp}")
        // 如何判断一个视频是否需要插件支持播放？
        // 如果只判断本地，那么进入时断网，点击播放时没有安装插件，点击几次失败几次
        // 如果只判断网络，那么网络接口挂掉时，点击播放，即使有旧插件，点击几次失败几次
        // 所以有本地匹配插件时，走本地判断；有网络合法结果时，走网络判断
        // 这样最坏情况只有当渠道插件从未安装过，才会播放失败

        // 如果本地存在，即使旧版本，也认为匹配
        if (checkLocalExistCpPlugin(context, cp)) {
            return Observable.create { subscriber ->
                Log.d(TAG, "checkCpNeedPlugin|existPlugin|valid&match")
                subscriber.onNext(true)
                subscriber.onCompleted()
            }
        }

        // 如果缓存info存在，检查是否匹配
        if (checkLocalCacheCpPlugin(context, cp)) {
            return Observable.create { subscriber ->
                Log.d(TAG, "checkCpNeedPlugin|savedPlugin|valid&match")
                subscriber.onNext(true)
                subscriber.onCompleted()
            }
        }
        // 如果缓存info不为空，且上一步的匹配没有匹配上，说明该cp不是特殊cp，不需要走插件
        if (!savedPlugin?.isEmpty()) {
            return Observable.create { subscriber ->
                Log.d(TAG, "checkCpNeedPlugin|savedPlugin|valid&notMatch")
                subscriber.onNext(false)
                subscriber.onCompleted()
            }
        }
        // 这种情况就是缓存info为空，那么去请求获取缓存数据同时填充缓存info
        Log.d(TAG, "checkCpNeedPlugin|savedPlugin|invalid|needRequest")
        return requestPluginInfo(context)
                .flatMap { pluginResult -> checkPluginResult(cp, pluginResult) }
    }

    private fun checkLocalExistCpPlugin(context: Context, cp: String?): Boolean {
        Log.d(TAG, "checkLocalExistCpPlugin|cp|${cp}")
        var catched = false
        run breaking@ {
            PluginManagerHelper.getPlugins()?.forEach {
                var pluginPackageName = it.packageName
                var pluginVersion = it.version
                Log.d(TAG, "checkLocalExistCpPlugin|plugin|${pluginPackageName}|cp|${pluginVersion}")
                if (!TextUtils.isEmpty(pluginVersion) && pluginVersion.startsWith(cp + "_")) {
                    Log.d(TAG, "checkLocalExistCpPlugin|cp|${cp}|catched|${pluginPackageName}")
                    catched = true
                    return@breaking
                }
            }
        }
        return catched
    }

    private fun checkLocalCacheCpPlugin(context: Context, cp: String?): Boolean {
        Log.d(TAG, "checkLocalCacheCpPlugin|cp|${cp}")
        var catched = false
        run breaking@ {
            savedPlugin?.forEach {
                Log.d(TAG, "checkLocalCacheCpPlugin|plugin|${it._id}|cp|${it.cp}")
                if (it != null && it.cp == cp) {
                    Log.d(TAG, "checkLocalCacheCpPlugin|cp|${cp}|catched|${it._id}")
                    catched = true
                    return@breaking
                }
            }
        }
        return catched
    }

    fun checkPluginResult(cp: String?, pluginResult: PluginResult?): Observable<Boolean> {
        Log.d(TAG, "checkPluginResult|cp|${cp}")
        return Observable.create { subscriber ->
            var handle = false
            if (pluginResult == null || pluginResult.data == null
                    || !"success".equals(pluginResult.data.result)) {
                // 各种网络失败，数据失败的情况
                Log.d(TAG, "checkPluginResult|cp|${cp}|plugin data denied")
            } else {
                // 网络成功
                Log.d(TAG, "checkPluginResult|cp|${cp}|plugin data ok")
                val pluginList = pluginResult?.data?.cp_plugin
                pluginList?.forEach {
                    if (it != null && it.cp == cp) {
                        Log.d(TAG, "checkPluginResult|cp|${cp}|match|${it._id}")
                        // 当前网络返回数据有效的话，比对键值是否一致，一致的话验证通过
                        handle = true
                    }
                }
            }
            Log.d(TAG, "checkPluginResult|ret|${handle}")
            subscriber.onNext(handle)
            subscriber.onCompleted()
        }
    }

    fun submitTaskOnPlugin(context: Context, cp: String?, videoId: String?, content: Content?, targetDefinition: VideoDefinition?): Observable<TaskPayload> {
        Log.d(TAG, "submitTaskOnPlugin|cp|${cp}|videoId|${videoId}")
        return Observable.create { subscriber ->
            var result: TaskPayload? = null
            val payload = TaskPayload()
            payload.identify = videoId
            payload.cp = cp
            payload.content = Gson().toJson(content)
            payload.color = parseDefinitionCode(targetDefinition)
            payload.to = getPluginIdentifyByCpName(cp)

            Log.d(TAG, ">submitTaskOnPlugin|id|${payload.identify}|cp|${payload.cp}|to|${payload.to}|content|${payload.content}|state|${payload.state}|ex|${payload.ex}")
            result = syncProcessTaskPayload(context, payload, "submit")
            Log.d(TAG, "<submitTaskOnPlugin|ch|${result.ch}|id|${result.identify}|cp|${result.cp}|to|${result.to}|content|${result.content}|state|${result.state}|ex|${result.ex}")

            if (TaskPayloadState.SUCCESS.code() == result?.state) {
                subscriber.onNext(result)
            } else {
                var message = getMessageFromTaskPayloadStates(result.state)
                Log.e(TAG, "##submitTaskOnPlugin|failure|${message}|state|${result.state}")
                subscriber.onNext(result)
            }
            subscriber.onCompleted()
        }
    }

    fun changeTaskOnPlugin(context: Context, cp: String?, videoId: String?, definition: Int): Observable<TaskPayload> {
        Log.d(TAG, "changeTaskOnPlugin|cp|${cp}|videoId|${videoId}|definition|${definition}")
        return Observable.create { subscriber ->
            var result: TaskPayload? = null
            val payload = TaskPayload()
            payload.identify = videoId
            payload.cp = cp
            payload.color = definition
            payload.to = getPluginIdentifyByCpName(cp)

            Log.d(TAG, ">changeTaskOnPlugin|id|${payload.identify}|cp|${payload.cp}|to|${payload.to}|content|${payload.content}|state|${payload.state}|ex|${payload.ex}")
            result = syncProcessTaskPayload(context, payload, "change")
            Log.d(TAG, "<changeTaskOnPlugin|ch|${result.ch}|id|${result.identify}|cp|${result.cp}|to|${result.to}|content|${result.content}|state|${result.state}|ex|${result.ex}")

            if (TaskPayloadState.SUCCESS.code() == result?.state) {
                subscriber.onNext(result)
            } else {
                var message = getMessageFromTaskPayloadStates(result.state)
                Log.e(TAG, "##changeTaskOnPlugin|failure|${message}|state|${result.state}")
                subscriber.onNext(result)
            }
            subscriber.onCompleted()
        }
    }

    fun queryTaskOnPlugin(context: Context, cp: String?, videoId: String?, content: Content, targetDefinition: VideoDefinition?, queryWhat: PluginQuery): Observable<TaskPayload> {
        Log.d(TAG, "queryTaskOnPlugin|cp|${cp}|videoId|${videoId}|query|${queryWhat}")
        return Observable.create { subscriber ->
            var result: TaskPayload? = null
            val payload = TaskPayload()
            payload.identify = videoId
            payload.cp = cp
            content.query = queryWhat.what
            payload.content = Gson().toJson(content)
            payload.color = parseDefinitionCode(targetDefinition)
            payload.to = getPluginIdentifyByCpName(cp)

            Log.d(TAG, ">queryTaskOnPlugin|id|${payload.identify}|cp|${payload.cp}|to|${payload.to}|content|$payload.content|state|$payload.state|ex|${payload.ex}")
            result = syncProcessTaskPayload(context, payload, "query")
            Log.d(TAG, "<queryTaskOnPlugin|ch|${result.ch}|id|${result.identify}|cp|${result.cp}|to|${result.to}|content|${result.content}|state|${result.state}|ex|${result.ex}")

            if (TaskPayloadState.SUCCESS.code() == result?.state) {
                subscriber.onNext(result)
            } else {
                var message = getMessageFromTaskPayloadStates(result.state)
                Log.e(TAG, "##queryTaskOnPlugin|failure|${message}|state|${result.state}")
                subscriber.onNext(result)
            }
            subscriber.onCompleted()
        }
    }

    /**
     * checkAndRequestPlugin中的第4步
     * 详细查看：checkAndRequestPlugin
     */
    fun installPlugin(context: Context, plugin: Plugin): Observable<Plugin> {
        return Observable.create { subscriber ->
            val alreadyInstalled = CheckUtil.checkPluginInstalled(plugin._id)
            if (alreadyInstalled) {
                PluginManagerHelper.getPlugins()?.forEach {
                    if (it != null && it.packageName == plugin._id) {
                        val pluginVersion = it.version
                        Log.d(TAG, "alreadyInstalledPlugin|${plugin._id}|version|${pluginVersion}")
                    }
                }
            }
            val resultCode = PluginUtil.installPlugin(plugin._id, plugin.path)
            val resultMsg = PluginUtil.getPluginErrMsg(context.applicationContext, resultCode)
            val resultToast = PluginUtil.getPluginErrToast(context.applicationContext, resultCode)
            var appendMsg = ""
            PluginManagerHelper.getPlugins()?.forEach {
                if (it != null && it.packageName == plugin._id) {
                    val pluginMd5 = NetworkParams.getMD5(File(it.installedPath))
                    appendMsg = "|updatePlugin|packageName|${it.packageName}|version|${it.version}|enable|${it.isEnabled}|standalone|${it.isStandalone}|path|${it.installedPath}|md5|${pluginMd5}"
                }
            }
            plugin.hint = resultMsg + appendMsg
            plugin.toast = resultToast
            val installSuccess = CheckUtil.checkPluginInstalled(plugin._id)
            if (installSuccess) {
                Log.d(TAG, "installPlugin|packageName|${plugin._id}|installSuccess|delete cache")
                val cache = File(plugin.path)
                if (cache.exists() && cache.isFile) {
                    cache.delete()
                }
            }
            subscriber.onNext(plugin)
            subscriber.onCompleted()
        }
    }

    /**
     * checkAndRequestPlugin中的第3步
     * 详细查看：checkAndRequestPlugin
     */
    fun downloadPlugin(context: Context, plugin: Plugin): Observable<Plugin> {
        return Observable.create { subscriber ->
            PluginDownloadManager.get().downloadPlugin(context.applicationContext,
                    plugin._id, "apk", plugin.md5, plugin.url, object : DownloadAdapter() {
                override fun onDownloadSuccess(url: String, path: String) {
                    val p = Plugin()
                    p._id = plugin._id
                    p.md5 = plugin.md5
                    p.url = plugin.url
                    p.path = path
                    Log.d(TAG, "onDownloadSuccess|${p._id}|${p.path}|${hashCode()}|${releaseCode}")
                    subscriber.onNext(p)
                }

                override fun onDownloadFailure(url: String, message: String?) {
                    Log.d(TAG, "onDownloadFailure|${url}|${message}")
                }
            })
        }
    }

    /**
     * 升级策略（只更新安装过的）：
     * 1.如果本地存在的plugin，平台没有部署，那就是要删除（删除不需要的）
     * 2.如果本地存在的plugin，和平台存在的md5对不上，那就是要在本地的基础上升级（本地不存在的不管）
     */
    private fun checkPluginOnlyUpdate(context: Context, pluginResult: PluginResult?): Observable<List<Plugin>> {
        return Observable.create(Observable.OnSubscribe { subscriber ->
            val needToDown = ArrayList<Plugin>()
            Log.d(TAG, "check plugin need to update+")
            if (pluginResult == null || pluginResult.data == null
                    || !"success".equals(pluginResult.data.result)) {
                Log.d(TAG, "plugin data denied+")
                subscriber.onNext(needToDown)
                subscriber.onCompleted()
                return@OnSubscribe
            }
            val remotePlugins = HashMap<String, Plugin>()
            val pluginList = pluginResult?.data?.cp_plugin
            pluginList?.forEach {
                remotePlugins.put(it._id, it)
            }
            Log.d(TAG, "remote plugin size+${remotePlugins.size}")
            if (PluginManagerHelper.getPlugins() == null || PluginManagerHelper.getPlugins().size == 0) {
                Log.d(TAG, "local plugin not exist+")
            }
            var localPlugins = HashMap<String, PluginInfo>()
            PluginManagerHelper.getPlugins()?.forEach {
                val pluginMd5 = NetworkParams.getMD5(File(it.installedPath))
                Log.d(TAG, "installedPlugin+|packageName|${it.packageName}|version|${it.version}|enable|${it.isEnabled}|standalone|${it.isStandalone}|path|${it.installedPath}|md5|${pluginMd5}")
                val info = PluginInfo()
                info.id = it.packageName
                info.pluginEnable = it.isEnabled
                info.pluginStandAlone = it.isStandalone
                info.pluginVersion = it.version
                info.pluginPath = it.installedPath
                info.pluginMd5 = pluginMd5
                localPlugins.put(it.packageName, info)
            }
            // 禁止删除支持
//            val needToDeleteIds = ArrayList<String>()
//            for (local in localPlugins.values) {
//                // 如果本地存在的plugin，平台没有部署，那就是要删除
//                if (remotePlugins[local.id] == null) {
//                    Log.d(TAG, "needDelete+|" + local.id + "|" + local.pluginPath)
//                    PluginManagerHelper.remove(local.id)
//                    needToDeleteIds.add(local.id)
//                }
//            }
//            // 上面删除了平台端没有的，所以删除后需要更新本地数据
//            for (deletedId in needToDeleteIds) {
//                localPlugins.remove(deletedId)
//            }
            for (local in localPlugins.values) {
                // 如果本地存在的plugin，和平台存在的md5对不上，那就是要在本地的基础上升级
                val remotePlugin = remotePlugins[local.id]
                if (remotePlugin == null || remotePlugin.md5 == local.pluginMd5) {
                    // 平台没有，或者平台的md5与本地一致，不用更新
                    continue
                }
                needToDown.add(remotePlugin)
                Log.d(TAG, "needToDown+|${remotePlugin._id}|${remotePlugin.url}")
            }
            subscriber.onNext(needToDown)
            subscriber.onCompleted()
        })
    }

    /**
     * 升级策略（没安装的安装，需要更新的更新）：
     * 1.如果本地存在的plugin，平台没有部署，那就是要删除（删除不需要的）
     * 2.如果平台部署的plugin，本地不存在，或者和本地存在的md5对不上，那就是要更新
     *
     * checkAndRequestPlugin中的第2步
     * 详细查看：checkAndRequestPlugin
     */
    private fun checkPluginInstallOrUpdate(context: Context, pluginResult: PluginResult?): Observable<List<Plugin>> {
        return Observable.create(Observable.OnSubscribe { subscriber ->
            val needToDown = ArrayList<Plugin>()
            Log.d(TAG, "check plugin need to update:")
            if (pluginResult == null || pluginResult.data == null
                    || !"success".equals(pluginResult.data.result)) {
                Log.d(TAG, "plugin data denied:")
                subscriber.onNext(needToDown)
                subscriber.onCompleted()
                return@OnSubscribe
            }
            val remotePlugins = HashMap<String, Plugin>()
            val pluginList = pluginResult?.data?.cp_plugin
            if (pluginList != null) {
                for (plugin in pluginList) {
                    remotePlugins.put(plugin._id, plugin)
                }
            }
            Log.d(TAG, "remote plugin size:${remotePlugins.size}")
            if (PluginManagerHelper.getPlugins() == null || PluginManagerHelper.getPlugins().size == 0) {
                Log.d(TAG, "local plugin not exist:")
            }
            var localPlugins = HashMap<String, PluginInfo>()
            PluginManagerHelper.getPlugins()?.forEach {
                val pluginMd5 = NetworkParams.getMD5(File(it.installedPath))
                Log.d(TAG, "installedPlugin:|packageName|${it.packageName}|version|${it.version}|enable|${it.isEnabled}|standalone|${it.isStandalone}|path|${it.installedPath}|md5|${pluginMd5}")
                val info = PluginInfo()
                info.id = it.packageName
                info.pluginEnable = it.isEnabled
                info.pluginStandAlone = it.isStandalone
                info.pluginVersion = it.version
                info.pluginPath = it.installedPath
                info.pluginMd5 = pluginMd5
                localPlugins.put(it.packageName, info)
            }
            // 禁止删除支持
//            val needToDeleteIds = ArrayList<String>()
//            for (local in localPlugins.values) {
//                // 如果本地存在的plugin，平台没有部署，那就是要删除
//                if (remotePlugins[local.id] == null) {
//                    Log.d(TAG, "needDelete:|" + local.id + "|" + local.pluginPath)
//                    PluginManagerHelper.remove(local.id)
//                    needToDeleteIds.add(local.id)
//                }
//            }
//            // 上面删除了平台端没有的，所以删除后需要更新本地数据
//            for (deletedId in needToDeleteIds) {
//                localPlugins.remove(deletedId)
//            }
            for (remotePlugin in remotePlugins.values) {
                // 如果平台部署的plugin，和本地plugin的md5对不上，那就是要下载安装
                val local = localPlugins[remotePlugin._id]
                if (local != null && local.pluginMd5 == remotePlugin.md5) {
                    // 本地存在，并且本地的md5与平台一致，不用更新
                    continue
                }
                needToDown.add(remotePlugin)
                Log.d(TAG, "needToDown:|${remotePlugin._id}|${remotePlugin.url}")
            }
            subscriber.onNext(needToDown)
            subscriber.onCompleted()
        })
    }

    /**
     * checkAndRequestPlugin中的第1步
     * 详细查看：checkAndRequestPlugin
     */
    fun requestPluginInfo(context: Context): Observable<PluginResult> {
//        return mainRepo.getPluginInfoTestData() // 测试环境
        return mainRepo.getPluginInfo()
                .map { result ->
                    if (result?.data?.cp_plugin != null
                            && "success" == result?.data?.result) {
                        var pluginList = result?.data?.cp_plugin
                        if (pluginList != null && savedPlugin != null) {
                            savedPlugin.clear()
                            savedPlugin.addAll(pluginList)
                        }
                        result
                    } else {
                        Log.d(TAG, "checkPlugin|retJson|invalid|${result}")
                        PluginResult()
                    }
                }
                .compose(asyncSchedulers())
    }

    /**
     * 只更新安装过的
     */
    fun checkAndUpdatePlugins(context: Context): Observable<Plugin> {
        return requestPluginInfo(context)
                .flatMap { pluginResult -> checkPluginOnlyUpdate(context, pluginResult) }
                .flatMap { plugins -> Observable.from(plugins) }
                .flatMap { plugin -> downloadPlugin(context, plugin) }
                .flatMap { plugin -> installPlugin(context, plugin) }
    }

    /**
     * 没安装的安装，需要更新的更新
     * checkAndRequestPlugin中的前第4步
     * 详细查看：checkAndRequestPlugin
     * 顺序：检查-下载-安装
     */
    fun checkAndInstallPlugins(context: Context): Observable<Plugin> {
        return requestPluginInfo(context)
                .flatMap { pluginResult -> checkPluginInstallOrUpdate(context, pluginResult) }
                .flatMap { plugins -> Observable.from(plugins) }
                .flatMap { plugin -> downloadPlugin(context, plugin) }
                .flatMap { plugin -> installPlugin(context, plugin) }
    }

    /**
     * 顺序：检查-（下载单个-安装单个）-执行
     */
    fun checkAndRequestPlugin(context: Context, cp: String, videoId: String, content: Content?, targetDefinition: VideoDefinition?): Observable<TaskPayload> {
        val pluginIdentify = getPluginIdentifyByCpName(cp)
        val installed = CheckUtil.checkPluginInstalled(pluginIdentify)
        // 一切都以cp插件是否被安装为前提
        // 如果安装，直接执行向插件发送命令的语句(即直接执行第5步)，内部仍旧有是否存在接收方的判断
        // 如果未安装：
        // 第1步. 发起一次新的联网请求，此时如果另外线程已经安装完毕，则这次联网也会执行
        // 第2步. 联网后分析数据，安装或卸载或维持现状，此时如果另外线程已经安装完毕，则这次分析也会执行
        // 第3步. 逐一进行下载，此时如果另外线程已经安装完毕，则这次下载也会执行，如果另外线程正在下载，则多个线程会走唯一的下载任务
        // 第4步. 逐一进行安装，此时如果另外线程已经安装完毕，则此次安装会提示已有相同版本无需安装，线程间不受影响
        // 第5步. 向插件发送命令(此处是submit命令)，发请求和处理是单一线程执行，各线程之间会异步处理结果
        return if (installed) {
            submitTaskOnPlugin(context, cp, videoId, content, targetDefinition)
        } else {
            requestPluginInfo(context) // 第1步
                    .flatMap { pluginResult -> checkPluginInstallOrUpdate(context, pluginResult) } // 第2步
                    .flatMap { plugins -> Observable.from(plugins) }
                    .filter { plugin -> plugin._id == pluginIdentify }
                    .flatMap { plugin -> downloadPlugin(context, plugin) } // 第3步
                    .flatMap { plugin -> installPlugin(context, plugin) } // 第4步
                    .flatMap { plugin ->
                        Log.d(TAG, "Result|${plugin._id}|${plugin.hint}")
                        if (!TextUtils.isEmpty(plugin.toast)) {
                            Log.d(TAG, "Toast|checkAndRequestPlugin|${plugin._id}|${plugin.toast}")
                            showToast(context.applicationContext, plugin.toast)
                        }
                        submitTaskOnPlugin(context, cp, videoId, content, targetDefinition)
                    } // 第5步

        }
    }

    private fun getPluginIdentifyByCpName(cp: String?): String? {
        var pluginIdentify: String? = null
        run breaking@ {
            savedPlugin?.forEach {
                if (it != null && it.cp == cp) {
                    pluginIdentify = it._id
                    return@breaking
                }
            }
        }

        if (TextUtils.isEmpty(pluginIdentify)) {
            // 如果网络缓存中没有找到，去插件框架中找
            run breaking@ {
                PluginManagerHelper.getPlugins()?.forEach {
                    if (it != null && !TextUtils.isEmpty(it.version) && it.version.startsWith(cp + "_")) {
                        pluginIdentify = it.packageName
                        return@breaking
                    }
                }
            }
        }
        return pluginIdentify
    }

    fun getPlayUrlListByDefinition(ex: Ex): ArrayList<String> {
        var retList = ArrayList<String>()
        var defStr = ex.definition
        when (defStr) {
            VideoDefinition.DEFINITION_LOW.desc() -> retList.addAll(ex.lowDefList)
            VideoDefinition.DEFINITION_NORMAL.desc() -> retList.addAll(ex.normalDefList)
            VideoDefinition.DEFINITION_HIGH.desc() -> retList.addAll(ex.highDefList)
            VideoDefinition.DEFINITION_SUPER.desc() -> retList.addAll(ex.superDefList)
        }
        return retList
    }

    @Synchronized
    fun callbackCpPlugin(context: Context, identify: String, cp: String, state: TaskPayloadState) {
        Log.d(TAG, "callbackCpPlugin|cp|${cp}|identify|${identify}|state|${state}")
        if (TextUtils.isEmpty(identify)) {
            return
        }
        val pluginIdentify = getPluginIdentifyByCpName(cp)
        if (TextUtils.isEmpty(pluginIdentify)) {
            return
        }
        val payload = TaskPayload()
        payload.identify = identify
        payload.to = pluginIdentify
        payload.state = state.code()
        if (TextUtils.isEmpty(payload.from)) {
            payload.from = context.packageName
        }
        callbackTask(context, payload, payload.to)
    }

    @Synchronized
    fun clearAllCpPlugin(context: Context) {
        Log.d(TAG, "clearAllCpPlugin")
        for (pd in PluginManagerHelper.getPlugins()) {
            val payload = TaskPayload()
            payload.identify = "universe"
            payload.to = pd.packageName
            payload.state = TaskPayloadState.HOST_CALLBACK_CLEARALL.code()
            if (TextUtils.isEmpty(payload.from)) {
                payload.from = context.packageName
            }
            callbackTask(context, payload, payload.to)
        }
    }

    fun getDefinitionByCurrentNetwork(isWifi: Boolean): VideoDefinition {
        return if (isWifi) {
            // wifi下选择最高分辨率
            VideoDefinition.DEFINITION_SUPER
        } else {
            // 非wifi下选择最低分辨率
            VideoDefinition.DEFINITION_LOW
        }
    }

    fun parseDefinitionCode(definition: VideoDefinition?): Int {
        return if (definition != null) {
            definition.code()
        } else {
            VideoDefinition.DEFINITION_LOW.code()
        }
    }

    fun checkAndQueryPlugin(context: Context, cp: String, videoId: String, content: Content, targetDefinition: VideoDefinition?, queryWhat: PluginQuery): Observable<TaskPayload> {
        val pluginIdentify = getPluginIdentifyByCpName(cp)
        val installed = CheckUtil.checkPluginInstalled(pluginIdentify)
        // 一切都以cp插件是否被安装为前提
        // 如果安装，直接执行向插件发送命令的语句(即直接执行第5步)，内部仍旧有是否存在接收方的判断
        // 如果未安装：
        // 第1步. 发起一次新的联网请求，此时如果另外线程已经安装完毕，则这次联网也会执行
        // 第2步. 联网后分析数据，安装或卸载或维持现状，此时如果另外线程已经安装完毕，则这次分析也会执行
        // 第3步. 逐一进行下载，此时如果另外线程已经安装完毕，则这次下载也会执行，如果另外线程正在下载，则多个线程会走唯一的下载任务
        // 第4步. 逐一进行安装，此时如果另外线程已经安装完毕，则此次安装会提示已有相同版本无需安装，线程间不受影响
        // 第5步. 向插件发送命令(此处是query命令)，发请求和处理是单一线程执行，各线程之间异步处理结果
        return if (installed) {
            queryTaskOnPlugin(context, cp, videoId, content, targetDefinition, queryWhat)
        } else {
            requestPluginInfo(context) // 第1步
                    .flatMap { pluginResult -> checkPluginInstallOrUpdate(context, pluginResult) } // 第2步
                    .flatMap { plugins -> Observable.from(plugins) }
                    .filter { plugin -> plugin._id == pluginIdentify }
                    .flatMap { plugin -> downloadPlugin(context, plugin) } // 第3步
                    .flatMap { plugin -> installPlugin(context, plugin) } // 第4步
                    .flatMap { plugin ->
                        Log.d(TAG, "Result|${plugin._id}|${plugin.hint}")
                        if (!TextUtils.isEmpty(plugin.toast)) {
                            Log.d(TAG, "Toast|checkAndQueryPlugin|${plugin._id}|${plugin.toast}")
                            showToast(context.applicationContext, plugin.toast)
                        }
                        queryTaskOnPlugin(context, cp, videoId, content, targetDefinition, queryWhat)
                    } // 第5步

        }
    }

    fun createPluginContentByCommonVideo(appContext: Context, video: CommonVideo): Content {
        var content = Content()
        content.urlExt = video.url_ext // 平台参数，等待对接
//            if (crypted) { // 支持加密
        content.cry = true
        content.imei = NetworkParams.getCryptIMEI(appContext)
        content.did = NetworkParams.getCryptDeviceId(appContext)
        content.uid = NetworkParams.getCryptUid(appContext)
        content.utk = NetworkParams.getCryptUtk(appContext)
//            } else { // 不加密
//                content.cry = false
//                content.imei = APICommonUrl.getIMEI(appContext)
//                content.did = APICommonUrl.getDeviceMd5Id(appContext)
//                content.uid = APICommonUrl.getCryptUid(appContext)
//                content.utk = APICommonUrl.getCryptUtk(appContext)
//            }
        content.versionName = NetworkParams.getAppVerName(appContext)
        content.versionCode = NetworkParams.getAppVer(appContext)
        content.screen = NetworkParams.getResolution(appContext)
        content.vid = video.video_id
        content.videoDuration = "" + video.video_duration
        content.authorId = video.author._id
        content.abi = android.os.Build.CPU_ABI
        content.supportAbis = getSupportAbis()
        return content
    }

    private fun getSupportAbis(): String {
        var sb = StringBuilder()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                for (abi in android.os.Build.SUPPORTED_ABIS) {
                    sb.append(abi + ",")
                }
            } catch (e: Exception) {
            }
        }
        return sb.toString()
    }

    fun testPluginByCp(appContext: Context, cp: String): Observable<TaskPayload> {
        var videoId = getTestVideoIdByCp(cp)
        var content = getTestContentByCp(cp)
        return submitTaskOnPlugin(appContext, cp, videoId, content, VideoDefinition.DEFINITION_NORMAL)
    }

    private fun getTestVideoIdByCp(cp: String): String {
        return if ("cy".equals(cp)) {
            "3052576"
        } else if ("cs".equals(cp)) {
            "3053745"
        } else if ("el".equals(cp)) {
            "Ym5P9xoRLynw"
        } else if ("fh".equals(cp)) {
            "http://ips.ifeng.com/video19.ifeng.com/video09/2016/08/27/318849-102-998767-191913.mp4?vid=0178355a-1ce8-472a-9def-7b1d474a61b1&ts=" + System.currentTimeMillis()
        } else if ("fs".equals(cp)) {
            "v16214771"
        } else if ("rr".equals(cp)) {
            "http://sh.file.myqcloud.com/files/v2/1252816746/rrsp/2017/10/12/7cef4a7941184f8ea208a47b064ae07e.mp4"
        } else if ("yl".equals(cp)) {
            "qbjQzrp7X5Az"
        } else if ("yk".equals(cp)) {
            "https://api.youku.com/videos/player/file?data=WcEl1o6uUdTRNRGMyTURBNE1BPT18MnwyfDI1MjU5fDIO0O0O"
        } else if ("wb".equals(cp)) {
            "http://f.us.sinaimg.cn/000RtnXqlx07jehw2lDq01040200eYte0k010.mp4?label=hevc_mp4_hd&template=852x480.32&Expires=1522752887&ssig=LnlNAV7ZjX&KID=unistore,video"
        } else {
            ""
        }
    }

    private fun getTestContentByCp(cp: String): Content {
        var content = Content()
        return if ("cy".equals(cp)) {
            content.setIMEI("13290wehgoiho2i2982332902")
            content
        } else if ("fh".equals(cp)) {
            content.versionName = "v1.3.2"
            content.imei = "13290wehgoiho2i2982332902"
            content.videoDuration = "406"
            content.setVid("7295d732-0d41-419d-9529-67f8a8d499ab")
            content
        } else if ("fs".equals(cp)) {
            content.authorId = "c1016"
            content
        } else if ("wb".equals(cp)) {
            content.urlExt = "{\"sid\":\"1034:90f87d9248c4e2542c5045e9d29b94f8\"}"
            content.abi = android.os.Build.CPU_ABI
            content.supportAbis = getSupportAbis()
            content
        } else {
            content
        }
    }
}
