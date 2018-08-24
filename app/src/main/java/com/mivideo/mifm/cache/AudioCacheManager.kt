package com.mivideo.mifm.cache

import android.compact.impl.TaskPayload
import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.github.salomonbrys.kodein.instance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mivideo.mifm.cpplugin.PluginManager
import com.mivideo.mifm.MainApp
import com.mivideo.mifm.NetworkManager
import com.mivideo.mifm.R
import com.mivideo.mifm.cpplugin.PluginAbility
import com.mivideo.mifm.cpplugin.TaskPayloadState
import com.mivideo.mifm.data.models.AudioInfo
import com.mivideo.mifm.data.models.jsondata.common.CommonVideo
import com.mivideo.mifm.data.models.jsondata.common.CommonVideoCache
import com.mivideo.mifm.data.models.jsondata.download.DownloadInfo
import com.mivideo.mifm.data.models.jsondata.plugins.Ex
import com.mivideo.mifm.download.VideoDefinition
import com.mivideo.mifm.download.support.*
import com.mivideo.mifm.network.commonurl.NetworkParams
import com.mivideo.mifm.rx.asyncSchedulers
import rx.Observable
import java.io.File
import java.net.URI

/**
 * 操作视频缓存相关的所有操作（启动下载缓存，判断使用缓存，整理清除无效缓存）
 */
class AudioCacheManager(val mContext: Context) {

    internal var TAG = "ACT"
    internal var MIN_STOREAGE_AVAILABLE_SIZE = 200 * 1024 * 1024 // 最小200M存储空间
    private var pluginManager: PluginManager

    init {
        val app = mContext.applicationContext as MainApp
        pluginManager = app.kodein.instance()
    }

    /**
     * 检查环境，再获取url（可能通过插件），创建任务缓存，下载缓存
     */
    fun processCommonAudioInfo(context: Context, audioInfo: AudioInfo?): Observable<DownloadInfo> {
        //if (audioInfo == null || TextUtils.isEmpty(audioInfo.passageItem.id) || TextUtils.isEmpty(audioInfo.passageItem.cp)) {
        if (audioInfo == null || TextUtils.isEmpty(audioInfo.passageItem.id)) {
            // 参数非法
            return Observable.create({ subscriber ->
                var info = DownloadInfo()
                info.title = audioInfo?.passageItem?.name
                info.hint = "ArgumentIllegal|audioInfo==null|" + (audioInfo == null) + "|TextUtils.isEmpty(audioInfo.passageItem.id)|" + (audioInfo?.passageItem?.id)
                info.msg = getMsgFromWarning(context, VideoCacheWarning.ArgumentIllegal)
                info.canTryStartDownload = false
                subscriber.onNext(info)
                subscriber.onCompleted()
            })
        }
        if (!NetworkManager.isNetworkConnected(context.applicationContext)) {
            // 网络无连接
            return Observable.create({ subscriber ->
                var info = DownloadInfo()
                info.title = audioInfo.passageItem.name
                info.hint = "0NetworkIllegal|"
                info.msg = getMsgFromWarning(context, VideoCacheWarning.NetworkIllegal)
                info.canTryStartDownload = false
                subscriber.onNext(info)
                subscriber.onCompleted()
            })
        }

        //val cp = audioInfo.passageItem.cp
        val appContext = context.applicationContext

        if (!hasAvailableSize(appContext)) {
            // 设备空间不足
            return Observable.create({ subscriber ->
                var info = DownloadInfo()
                info.title = audioInfo.passageItem.name
                info.hint = "DeviceCapacityIllegal|"
                info.msg = getMsgFromWarning(context, VideoCacheWarning.DeviceCapacityIllegal)
                info.canTryStartDownload = false
                subscriber.onNext(info)
                subscriber.onCompleted()
            })
        }
        // FIXME: 2018/8/21 音频缓存下载CP逻辑，后续完善
        /*return pluginManager.checkCpNeedPlugin(appContext, cp)
                .flatMap { needPlugin -> processAudioByState(appContext, needPlugin, audioInfo) }
                .compose(asyncSchedulers())*/
        return downloadAudioByDefault(appContext, audioInfo)
                .compose(asyncSchedulers())
    }

/*    *//**
     * 特殊渠道（需要插件）和一般渠道（play_url）的处理
     *//*
    private fun processCommonVideoByState(appContext: Context, needPlugin: Boolean, video: CommonVideo): Observable<DownloadInfo> {
        if (needPlugin) {
            var isUseWifi = NetworkManager.isUseWifiConnected(appContext)
            var targetDefinition = pluginManager.getDefinitionByCurrentNetwork(isUseWifi)
            var content = pluginManager.createPluginContentByCommonVideo(appContext, video)
            return pluginManager.checkAndQueryPlugin(appContext, video.cp, video.play_url, content, targetDefinition, PluginQuery.QUERY_PLAY_URLS)
                    .flatMap { payload ->
                        val ex = Gson().fromJson<Ex>(payload.ex, object : TypeToken<Ex>() {}.type)
                        if (ex == null) {
                            var message = "|ex|${payload.ex}|cp|${payload.cp}|id|${payload.identify}|state|${payload.state}|STATE|${TaskPayloadState.parse(payload.state)}|tag|${payload.tag}"
                            Log.d(PluginUtil.TAG, "Exception|queryVideo|Ex must not be null" + message)
                            throw IllegalStateException("queryVideo|Ex must not be null" + message)
                        }
                        Log.d(TAG, "queryVideo|cp|${payload.cp}|id|${payload.identify}|state|${payload.state}|${TaskPayloadState.parse(payload.state)}|definition|${ex.definitionList}|tag|${payload.tag}")
                        Log.d(TAG, "low size: ${ex.lowDefList.size}")
                        Log.d(TAG, "normal size: ${ex.normalDefList.size}")
                        Log.d(TAG, "high size: ${ex.highDefList.size}")
                        Log.d(TAG, "super size: ${ex.superDefList.size}")
                        var ver = ex.ver
                        var abilityFlag = ex.ability
                        var canChangeDefinition = PluginAbility.match(abilityFlag, PluginAbility.CAN_CHANGE_DEFINITION)
                        var canQueryPlayUrl = PluginAbility.match(abilityFlag, PluginAbility.CAN_QUERY_PLAY_URL)
                        Log.d(TAG, "ability|" + abilityFlag + "|canChangeDefinition|" + canChangeDefinition + "|canQueryPlayUrl|" + canQueryPlayUrl + "|ver|" + ver)

                        downloadVideoWithCpPlugin(appContext, payload, ex, video, targetDefinition)
                    }

        } else {
            // 默认视频分辨率全是normal
            return downloadAudioByDefault(appContext, video)
        }
    }*/

    /**
     * 一般渠道处理方式
     */
    fun downloadAudioByDefault(context: Context, audioInfo: AudioInfo): Observable<DownloadInfo> {
        val appContext = context.applicationContext
        var targetDefinition = VideoDefinition.DEFINITION_NORMAL // 默认视频分辨率全是normal
        var title = audioInfo.passageItem.name
        var identify = NetworkParams.getMD5(audioInfo.passageItem.id)
        var url = audioInfo.passageItem.url // 默认视频播放地址是play_url
        var dir = DownloadManager.get().getDownloadVideoCacheDir(appContext)
        var simpleUrl = getSimpleUrl(url)
        if (TextUtils.isEmpty(simpleUrl) || simpleUrl.endsWith(".m3u8")) {
            // 格式不支持
            return Observable.create { subscriber ->
                var info = DownloadInfo()
                info.title = title
                info.hint = "MimeTypeIllegal|url|" + url + "|simpleUrl|" + simpleUrl
                info.msg = getMsgFromWarning(appContext, VideoCacheWarning.MimeTypeIllegal)
                info.canTryStartDownload = false
                subscriber.onNext(info)
                subscriber.onCompleted()
            }
        }
        var cacheFilePath = VideoCacheUtils.findVideoCachePath(appContext, identify, targetDefinition)
        if (cacheFilePath != null && cacheFilePath.length > 0) {
            // 缓存已存在
            return Observable.create { subscriber ->
                var info = DownloadInfo()
                info.title = title
                info.hint = "CacheAlreadyExist|title|" + title + "|definition|" + targetDefinition + "|cachePath|" + cacheFilePath
                info.msg = getMsgFromWarning(appContext, VideoCacheWarning.CacheAlreadyExist)
                info.canTryStartDownload = false
                subscriber.onNext(info)
                subscriber.onCompleted()
            }
        }
        var isConnected = NetworkManager.isNetworkConnected(appContext)
        if (!isConnected) {
            // 网络未连接
            return Observable.create({ subscriber ->
                var info = DownloadInfo()
                info.title = title
                info.hint = "1NetworkIllegal|"
                info.msg = getMsgFromWarning(appContext, VideoCacheWarning.NetworkIllegal)
                info.canTryStartDownload = false
                subscriber.onNext(info)
                subscriber.onCompleted()
            })
        }

        return Observable.create { subscriber ->
            var info = DownloadInfo()
            info.title = title
            info.hint = "1PushIntoDownloadQueue|" + title
            info.msg = getMsgFromWarning(appContext, VideoCacheWarning.PushIntoDownloadQueue)
            info.canTryStartDownload = true
            // 下面的参数下载专用
            info.url = url
            info.dir = dir
            info.key = identify
            info.definitionCode = targetDefinition.code
            info.audioInfo = audioInfo
            subscriber.onNext(info)
            subscriber.onCompleted()
        }
    }

    /**
     * 特殊渠道处理方式
     */
    fun downloadVideoWithCpPlugin(context: Context, payload: TaskPayload, ex: Ex, video: CommonVideo, targetDefinition: VideoDefinition): Observable<DownloadInfo> {
        val appContext = context.applicationContext
        var title = ""
        if (video != null && !TextUtils.isEmpty(video.video_title)) {
            title = video.video_title
        }
        var state = TaskPayloadState.parse(payload.state)
        if (TaskPayloadState.SUCCESS != state) {
            // 插件返回非法
            Log.d(TAG, "downloadCpVideo|illegal|" + state)
            return Observable.create { subscriber ->
                var info = DownloadInfo()
                info.title = title
                info.hint = "PluginStateIllegal|PluginState|" + state
                info.msg = getMsgFromWarning(appContext, VideoCacheWarning.PluginStateIllegal)
                info.canTryStartDownload = false
                subscriber.onNext(info)
                subscriber.onCompleted()
            }
        }
        var canQueryPlayUrl = PluginAbility.match(ex.ability, PluginAbility.CAN_QUERY_PLAY_URL)
        if (!canQueryPlayUrl) {
            // 插件不支持获取下载地址ver1.5_15以上版本
            Log.d(TAG, "downloadCpVideo|illegal|canQueryPlayUrl|" + canQueryPlayUrl)
            return Observable.create { subscriber ->
                var info = DownloadInfo()
                info.title = title
                info.hint = "PluginUnsupportQueryUrl|canQueryPlayUrl|" + canQueryPlayUrl
                info.msg = getMsgFromWarning(appContext, VideoCacheWarning.PluginUnsupportQueryUrl)
                info.canTryStartDownload = false
                subscriber.onNext(info)
                subscriber.onCompleted()
            }
        }
        var playListAllEmpty = (ex.lowDefList.size == 0 && ex.normalDefList.size == 0 && ex.highDefList.size == 0 && ex.superDefList.size == 0)
        if (playListAllEmpty) {
            // 插件获取到的地址为空
            Log.d(TAG, "downloadCpVideo|illegal|defListAllEmpty")
            return Observable.create { subscriber ->
                var info = DownloadInfo()
                info.title = title
                info.hint = "PlayUrlListEmpty|playListAllEmpty|" + playListAllEmpty
                info.msg = getMsgFromWarning(appContext, VideoCacheWarning.PlayUrlListEmpty)
                info.canTryStartDownload = false
                subscriber.onNext(info)
                subscriber.onCompleted()
            }
        }
        var isUseWifi = NetworkManager.isUseWifiConnected(appContext)
        var playUrls = getPlayUrlsByCurrentNetwork(ex, isUseWifi)
        var targetDefinition = playUrls.def
        var identify = NetworkParams.getMD5(video.video_id)
        var url = ""
        if (playUrls.def_urls.size > 0) {
            url = playUrls.def_urls[0]
        }
        var dir = DownloadManager.get().getDownloadVideoCacheDir(appContext)
        var simpleUrl = getSimpleUrl(url)
        if (TextUtils.isEmpty(simpleUrl) || simpleUrl.endsWith(".m3u8")) {
            // 插件获取到的地址格式不支持
            return Observable.create { subscriber ->
                var info = DownloadInfo()
                info.title = title
                info.hint = "2MimeTypeIllegal|url|" + url + "|simpleUrl|" + simpleUrl
                info.msg = getMsgFromWarning(appContext, VideoCacheWarning.MimeTypeIllegal)
                info.canTryStartDownload = false
                subscriber.onNext(info)
                subscriber.onCompleted()
            }
        }
        var cacheFilePath = VideoCacheUtils.findVideoCachePath(appContext, identify, targetDefinition)
        if (cacheFilePath != null && cacheFilePath.length > 0) {
            // 插件获取到的地址已经存在缓存
            return Observable.create { subscriber ->
                var info = DownloadInfo()
                info.title = title
                info.hint = "2CacheAlreadyExist|title|" + title + "|definition|" + targetDefinition + "|cachePath|" + cacheFilePath
                info.msg = getMsgFromWarning(appContext, VideoCacheWarning.CacheAlreadyExist)
                info.canTryStartDownload = false
                subscriber.onNext(info)
                subscriber.onCompleted()
            }
        }
        var isConnected = NetworkManager.isNetworkConnected(appContext)
        if (!isConnected) {
            // 当前网络未连接
            return Observable.create { subscriber ->
                var info = DownloadInfo()
                info.title = title
                info.hint = "2NetworkIllegal|"
                info.msg = getMsgFromWarning(appContext, VideoCacheWarning.NetworkIllegal)
                info.canTryStartDownload = false
                subscriber.onNext(info)
                subscriber.onCompleted()
            }
        }

        return Observable.create { subscriber ->
            var info = DownloadInfo()
            info.title = title
            info.hint = "2PushIntoDownloadQueue|" + title
            info.msg = getMsgFromWarning(appContext, VideoCacheWarning.PushIntoDownloadQueue)
            info.canTryStartDownload = true
            // 下面的参数下载专用
            info.url = url
            info.dir = dir
            info.key = identify
            info.definitionCode = targetDefinition.code
            info.commonVideo = video
            subscriber.onNext(info)
            subscriber.onCompleted()
        }
    }

    fun startDownloadAudio(appContext: Context, downloadInfo: DownloadInfo, audioInfo: AudioInfo): Observable<DownloadInfo> {
        var title = downloadInfo.title
        var url = downloadInfo.url
        var dir = downloadInfo.dir
        var identify = downloadInfo.key
        var targetDefinition = VideoDefinition.parse(downloadInfo.definitionCode)
        var downloadPayload = URLConnectionPayload(url, dir, identify, targetDefinition, downloadInfo.jumpTheQueue)
        return Observable.create({ subscriber ->
            DownloadManager.get().downloadVideo(appContext, downloadPayload,
                    object : DownloadVideoAdapter(audioInfo) {
                        override fun onDownloadWaiting(url: String) {
                            var info = DownloadInfo()
                            info.title = title
                            info.hint = "WaitingDownload|identify|" + identify + "|canTryStartDownload"
                            info.msg = getMsgFromWarning(appContext, VideoCacheWarning.WaitingDownload)
                            info.key = identify
                            info.url = url
                            info.pushIntoDownloadQueue = true
                            info.audioInfo = audioInfo
                            subscriber.onNext(info)
                            subscriber.onCompleted()
                        }
                    })
        })
    }

    /**
     * 将url中的query部分去掉，留下'?'之前的部分，用来检查url类型
     */
    fun getSimpleUrl(playUrl: String?): String {
        if (playUrl == null || playUrl.length == 0) {
            return ""
        }
        try {
            // https://docs.oracle.com/javase/7/docs/api/java/net/URI.html
            // hierarchical: [scheme:][//authority][path][?query][#fragment]
            // refer from https://stackoverflow.com/questions/27267111/whats-the-best-way-to-get-rid-of-get-parameters-from-url-string
            var uri = URI(playUrl)
            var shortUrl = URI(uri.scheme,
                    uri.authority,
                    uri.path,
                    null, // Ignore the query part of the input url
                    null).toString() // Ignore the fragment part of the input url
            return shortUrl
        } catch (e : Exception) {
            e.printStackTrace()
        }
        // 如果发生异常，放弃检测，返回原值
        return playUrl
    }

    class PlayUrls {
        var def: VideoDefinition = VideoDefinition.DEFINITION_NORMAL
        var def_urls: List<String> = ArrayList<String>()
    }

    private fun getPlayUrlsByCurrentNetwork(ex: Ex, isUseWifi: Boolean): PlayUrls {
        var playUrls = PlayUrls()
        if (isUseWifi) {
            if (ex.superDefList.size > 0) {
                playUrls.def = VideoDefinition.DEFINITION_SUPER
                playUrls.def_urls = ex.superDefList
            } else if (ex.highDefList.size > 0) {
                playUrls.def = VideoDefinition.DEFINITION_HIGH
                playUrls.def_urls = ex.highDefList
            } else if (ex.normalDefList.size > 0) {
                playUrls.def = VideoDefinition.DEFINITION_NORMAL
                playUrls.def_urls = ex.normalDefList
            } else if (ex.lowDefList.size > 0) {
                playUrls.def = VideoDefinition.DEFINITION_LOW
                playUrls.def_urls = ex.lowDefList
            }
        } else {
            if (ex.lowDefList.size > 0) {
                playUrls.def = VideoDefinition.DEFINITION_LOW
                playUrls.def_urls = ex.lowDefList
            } else if (ex.normalDefList.size > 0) {
                playUrls.def = VideoDefinition.DEFINITION_NORMAL
                playUrls.def_urls = ex.normalDefList
            } else if (ex.highDefList.size > 0) {
                playUrls.def = VideoDefinition.DEFINITION_HIGH
                playUrls.def_urls = ex.highDefList
            } else if (ex.superDefList.size > 0) {
                playUrls.def = VideoDefinition.DEFINITION_SUPER
                playUrls.def_urls = ex.superDefList
            }
        }
        return playUrls
    }

    fun getMsgFromWarning(context: Context, warning: VideoCacheWarning): String {
        return when (warning) {
            VideoCacheWarning.ArgumentIllegal -> context.applicationContext.getString(R.string.video_cache_argument_illegal)
            VideoCacheWarning.DeviceCapacityIllegal -> context.applicationContext.getString(R.string.video_cache_device_capacity_illegal)
            VideoCacheWarning.MimeTypeIllegal -> context.applicationContext.getString(R.string.video_cache_mimetype_illegal)
            VideoCacheWarning.CacheAlreadyExist -> context.applicationContext.getString(R.string.video_cache_already_exist)
            VideoCacheWarning.NetworkIllegal -> context.applicationContext.getString(R.string.video_cache_network_illegal)
            VideoCacheWarning.PushIntoDownloadQueue -> context.applicationContext.getString(R.string.video_cache_push_into_download_queue)
            VideoCacheWarning.WaitingDownload -> context.applicationContext.getString(R.string.video_cache_waiting_download)
            VideoCacheWarning.PluginStateIllegal -> context.applicationContext.getString(R.string.video_cache_plugin_state_illegal)
            VideoCacheWarning.PluginUnsupportQueryUrl -> context.applicationContext.getString(R.string.video_cache_plugin_unsupport_query_url)
            VideoCacheWarning.PlayUrlListEmpty -> context.applicationContext.getString(R.string.video_cache_play_list_empty)
            else -> ""
        }
    }

    fun listAndCheckExistCaches(context: Context, caches: List<CommonVideoCache>): Observable<List<CommonVideoCache>> {
        Log.d(TAG, "listAndCheckExists|@|" + hashCode())
        return Observable.create(Observable.OnSubscribe<List<CommonVideoCache>> { subscriber ->
            var files = ArrayList<File>()
            var videoCacheDirPath = DownloadManager.get().getDownloadVideoCacheDir(context.applicationContext)
            if (!TextUtils.isEmpty(videoCacheDirPath)) {
                var dirFile = File(videoCacheDirPath)
                if (dirFile.exists() && dirFile.isDirectory) {
                    files.addAll(dirFile.listFiles())
                }
            }
            var keys = ArrayList<String>()
            for (c in caches) {
                Log.d(TAG, "CheckExists|listCaches|" + c.getKey() + "|" + DownloadState.parse(c.getState()) + "|" + DownloadError.parse(c.getErrorCode()) + "|[" + c.getProgress() + "]|" + c.getCompleteSize() + "/" + c.getTotalSize())
                keys.add(c.getKey())
            }
            // 过滤文件
            // 如果文件存在，数据表里有记录，那是正常的
            // 如果文件存在，数据表里没有记录，那一定是多余的
            var found = false
            var fileName = ""
            for (file in files) {
                fileName = file.name
                if (fileName.startsWith("t_")) {
                    fileName = fileName.substring(2, fileName.length)
                }
                found = false
                for (key in keys) {
                    if (fileName.startsWith(key)) {
                        found = true
                        break
                    }
                }
                if (!found) {
                    Log.d(TAG, "listAndCheckExists|delete file|" + file.absolutePath)
                    file.delete()
                }
            }
            // 过滤数据表
            // 如果数据表里有记录，文件存在，那是正常的
            // 如果数据表里有记录，文件不存在，那是有可能的
            //（WAITING状态允许没有启动下载存在，其他状态这种情况改成WAITING，这样点击之后就会启动下载了）
            var convertToWaiting = ArrayList<CommonVideoCache>()
            for (cache in caches) {
                if (DownloadState.SUCCESS.code == cache.getState()) {
                    // 如果是下载成功的数据，发现没有文件存在的，在点击时才去处理，这里不管
                    continue
                }
                found = false
                for (file in files) {
                    fileName = file.name
                    if (fileName.startsWith("t_")) {
                        fileName = fileName.substring(2, fileName.length)
                    }
                    if (fileName.startsWith(cache.getKey())) {
                        found = true
                        break
                    }
                }
                if (!found) {
                    Log.d(TAG, "listAndCheckExists|to waiting|" + cache.getKey())
                    convertToWaiting.add(cache)
                }
            }
            subscriber.onNext(convertToWaiting)
            subscriber.onCompleted()
        })
    }

    fun listTaskKilledCaches(context: Context, caches: List<CommonVideoCache>): Observable<List<CommonVideoCache>> {
        Log.d(TAG, "listTaskKilledCaches|@|" + hashCode())
        return Observable.create(Observable.OnSubscribe<List<CommonVideoCache>> { subscriber ->
            var taskKilledCaches = ArrayList<CommonVideoCache>()
            for (c in caches) {
                if (c != null && DownloadState.START.code == c.getState()) {
                    var isRunning = DownloadManager.get().hasRunningTask(c.getKey())
                    if (!isRunning) {
                        Log.d(TAG, "CheckKilled|listCaches|" + c.getKey() + "|" + DownloadState.parse(c.getState()) + "|" + DownloadError.parse(c.getErrorCode()) + "|[" + c.getProgress() + "]|" + c.getCompleteSize() + "/" + c.getTotalSize())
                        // 虽然状态是"下载中"，但是查询到所属任务并没有在运行，那么是之前被强杀进程了
                        taskKilledCaches.add(c)
                    }
                }
            }
            subscriber.onNext(taskKilledCaches)
            subscriber.onCompleted()
        })
    }

    fun hasAvailableSize(appContext: Context): Boolean {
        var hasAvailableSize = true
        try {
            var availableSize = 0L
            var internalFile = DownloadUtil.getInternalStorageFile(appContext)
            var externalFile = DownloadUtil.getExternalStorageFiles(appContext)[0]
            var videoCacheDirFilePath = DownloadManager.get().getDownloadVideoCacheDir(appContext)
            if (videoCacheDirFilePath!!.contains(internalFile.absolutePath)) {
                availableSize = DownloadUtil.getInternalStorageAvailableSize(appContext)
            } else if (videoCacheDirFilePath!!.contains(externalFile.absolutePath)) {
                availableSize = DownloadUtil.getExternalStorageAvailableSize(appContext, 0)
            }
            if (availableSize < (MIN_STOREAGE_AVAILABLE_SIZE)) {
                Log.d(TAG, "IllegalStorageAvailableSize|" + availableSize + "|" + DownloadUtil.formatByteFileSize(appContext, availableSize))
                hasAvailableSize = false
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            hasAvailableSize = true
        }
        return hasAvailableSize
    }
}