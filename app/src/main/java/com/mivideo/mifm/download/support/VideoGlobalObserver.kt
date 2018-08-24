package com.mivideo.mifm.download.support

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.NetworkManager
import com.mivideo.mifm.R
import com.mivideo.mifm.cache.AudioCacheManager
import com.mivideo.mifm.data.models.jsondata.common.CommonVideoCache
import com.mivideo.mifm.data.viewmodel.CacheViewModel
import com.mivideo.mifm.events.*
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.util.app.postEvent
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class VideoGlobalObserver(val appContext: Context) : FileDownloadImpl, KodeinInjected {
    internal var TAG = "VGO"
    override val injector = KodeinInjector()
    private var currentNetworkState: NetworkState? = null
    private val cacheViewModel: CacheViewModel by instance()
    private val audioCacheManager: AudioCacheManager by instance()
    private var observeNetworkSubscription: Subscription? = null
    val period = 1000L

    init {
        inject(appContext.appKodein())
        // 过滤无效数据和无效文件，该删的删
        cacheViewModel.loadAllCacheData()
                .flatMap { caches ->
                    // 区分文件和表数据双重过滤
                    audioCacheManager.listAndCheckExistCaches(appContext, caches)
                }
                .flatMap { caches ->
                    Observable.from(caches)
                }
                .flatMap { cache ->
                    // 这里都是数据表有数据，但是本地缓存丢失的情况，这种情况需要修改数据表状态
                    cache.setState(DownloadState.WAITING.code) // 状态置为"等待下载"，再点击重启下载
                    cache.setProgress(0) // 重新下载，进度为0
                    cache.setCompleteSize(0L) // 重新下载，已完成长度为0
//                    cache.setTotalSize(0L) // 重新下载，总长度保持表中数据，以示区别
                    cache.setPath("") // 重新下载，路径失效，置为空
                    cacheViewModel.saveCache(cache)
                }
                .compose(asyncSchedulers())
                .subscribe(
                        { Timber.i("list and check all cache data success") },
                        { Timber.i("list and check all cache data failure") })
        // 过滤"下载中"状态的任务，如果任务没有运行，说明是杀进程造成的，将状态置成失败，失败原因置成被杀进程
        cacheViewModel.loadAllCacheData()
                .flatMap { caches ->
                    // 过滤出kill掉的任务，这些任务状态异常，需要重置状态
                    audioCacheManager.listTaskKilledCaches(appContext, caches)
                }
                .flatMap { caches ->
                    Observable.from(caches)
                }
                .flatMap { cache ->
                    // 这里都是状态为"下载中"但是没有任务运行的情况，这种情况需要修改数据表状态
                    cache.setState(DownloadState.FAILURE.code) // 状态置为"失败"
                    cache.setErrorCode(DownloadError.PROCESS_KILLED_ERROR.ordinal) // 原因为被杀进程
                    cache.setFailReason(appContext.getString(R.string.video_cache_waiting)) // 文案显示"等待中"
                    cacheViewModel.saveCache(cache)
                }
                .compose(asyncSchedulers())
                .subscribe(
                        { Timber.i("list task killed cache data success") },
                        { Timber.i("list task killed cache data failure") })
        // 网络切换为wifi时，自动下载队列中状态等待且长度为零进度为零的任务
        if (NetworkManager.isNetworkConnected(appContext)) {
            if (NetworkManager.isUseWifiConnected(appContext)) {
                currentNetworkState = NetworkState.WIFI
            } else {
                currentNetworkState = NetworkState.MOBILE
            }
        } else {
            currentNetworkState = NetworkState.NO_CONNECT
        }
        observeNetworkSubscription = ReactiveNetwork
                .observeNetworkConnectivity(appContext.applicationContext)
                .subscribeOn(Schedulers.io())
                .debounce(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { connectivity ->
                    if (connectivity.state == NetworkInfo.State.CONNECTED) {
                        if (ConnectivityManager.TYPE_WIFI == connectivity.type) {
                            var oldNetworkState = currentNetworkState
                            currentNetworkState = NetworkState.WIFI
                            Log.d(TAG, "connect to wifi|" + oldNetworkState + "|to|" + currentNetworkState)

                            // 无论之前是数据还是断网，此时切换到wifi，都启动自动下载
                            downloadAutoStartTasks(appContext) // 启动自动下载

                        } else if (connectivity.type == ConnectivityManager.TYPE_MOBILE) {
                            var oldNetworkState = currentNetworkState
                            currentNetworkState = NetworkState.MOBILE
                            Log.d(TAG, "connect to mobile|" + oldNetworkState + "|to|" + currentNetworkState)

                            // 无论是从wifi切换到数据或是从断网切换到数据，都暂停一切下载中的任务
                            DownloadManager.get().cancelAll(InterruptReason.CONVERT_TO_MOBILE)
                        }
                    } else if (connectivity.state == NetworkInfo.State.DISCONNECTED) {
                        var oldNetworkState = currentNetworkState
                        currentNetworkState = NetworkState.NO_CONNECT
                        Log.d(TAG, "disconnect|" + oldNetworkState + "|to|" + currentNetworkState)
                    }
                }
    }

    fun downloadAutoStartTasks(context: Context) {
        if (!NetworkManager.isNetworkConnected(context.applicationContext)) {
            // 如果未连接，什么都不做
            return
        }
        // 以下情况自动下载(wifi)
        var isUseWifiConnected = NetworkManager.isUseWifiConnected(context.applicationContext)
        Log.d(TAG, "downloadAutoStartTasks|isUseWifiConnected|" + isUseWifiConnected)
        if (isUseWifiConnected) {
            cacheViewModel.loadAllCacheData()
                    .flatMap { caches ->
                        for (c in caches) {
                            Log.d(TAG, "AutoStart|listCaches|" + c.getKey() + "|" + DownloadState.parse(c.getState()) + "|" + DownloadError.parse(c.getErrorCode()) + "|[" + c.getProgress() + "]|" + c.getCompleteSize() + "/" + c.getTotalSize() + "|hasRunning|" + DownloadManager.get().hasRunningTask(c.getKey()))
                        }
                        // 列出所有数据
                        Observable.from(caches)
                    }
                    .filter { cache ->
                        if (DownloadManager.get().hasRunningTask(cache.getKey())) {
                            // 无论任何状态，运行中的任务均不干预
                            false
                        } else {
                            // 未运行的任务才判断去启动
                            // 拣出处于"等待"状态的任务
                            // 除了“下载中”“暂停”“成功”“失败”这4种状态，其他情况都是“等待”状态
                            // 注意："等待"状态还包含3种特殊的Interrupt
                            // 注意："等待"状态还包含"下载中"但是任务未运行的，这种最常见情况是强杀进程，来不及切换状态
                            // 自动下载只关心“等待”状态的任务，“暂停”状态的不去管
                            DownloadState.WAITING.code == cache.getState()
                                    || DownloadError.errorForInterrupt(DownloadError.parse(cache.getErrorCode()))
                                    || DownloadState.START.code == cache.getState() // 当前情况已经是未运行态，但是标记是"下载中"，强杀进程导致
                                    || DownloadError.errorForProcessKilled(DownloadError.parse(cache.getErrorCode())) // 这也是杀进程导致
                        }
                    }
                    .flatMap { cache ->
                        // 这样的任务检查是否能够下载
                        Log.d(TAG, "wifi auto download|" + cache.getKey() + "|" + cache.getTitle())
                        audioCacheManager.processCommonAudioInfo(appContext, cache.getAudioInfo())
                    }
                    .flatMap { info ->
                        // 检查工作完成，判断是否交给下载模块进行下载处理
                        if (info.canTryStartDownload && info.commonVideo != null) {
                            audioCacheManager.startDownloadAudio(appContext, info, info.audioInfo)
                        } else {
                            Observable.just(info)
                        }
                    }
                    .flatMap { info ->
                        // 下载模块将任务加入下载列表，判断是否写入数据
                        if (info.pushIntoDownloadQueue) {
                            Log.d(TAG, "1hint|" + info.hint)
                            var cache = CommonVideoCache(info.commonVideo)
                            cache.setKey(info.key)
                            cache.setUrl(info.url)
                            cache.setPath(info.path)
                            cache.setState(DownloadState.WAITING.code)
                            cache.setTitle(info.title)
                            cache.setMsg(info.msg)
                            cacheViewModel.saveCache(cache)
                        } else {
                            Log.d(TAG, "2hint|" + info.hint)
                            var commonVideoCache = CommonVideoCache()
                            commonVideoCache.setTitle(info.title)
                            commonVideoCache.setMsg(info.msg)
                            Observable.just(commonVideoCache)
                        }
                    }
                    .compose(asyncSchedulers())
                    .subscribe(
                            { Timber.i("re-download all 'waiting' cache data success") },
                            { Timber.i("re-download all 'waiting' cache data failure") })
        }
    }

    fun downloadJumpTheQueueTask(context: Context, key: String): Boolean {
        if (context == null || key == null || key.length == 0) {
            return false
        }
        if (!NetworkManager.isNetworkConnected(context.applicationContext)) {
            // 如果未连接，什么都不做
            return false
        }
        // 以下情况自动下载(wifi)
        var isUseWifiConnected = NetworkManager.isUseWifiConnected(context.applicationContext)
        Log.d(TAG, "downloadJumpTheQueueTask|isUseWifiConnected|" + isUseWifiConnected)
        if (isUseWifiConnected) {
            cacheViewModel.getDataByKey(key)
                    .flatMap { cache ->
                        // 这样的任务检查是否能够下载
                        Log.d(TAG, "jump the queue auto download|" + cache.getKey() + "|" + cache.getTitle())
                        audioCacheManager.processCommonAudioInfo(appContext, cache.getAudioInfo())
                    }
                    .flatMap { info ->
                        // 检查工作完成，判断是否交给下载模块进行下载处理
                        if (info.canTryStartDownload && info.commonVideo != null) {
                            audioCacheManager.startDownloadAudio(appContext, info, info.audioInfo)
                        } else {
                            Observable.just(info)
                        }
                    }
                    .flatMap { info ->
                        // 下载模块将任务加入下载列表，判断是否写入数据
                        if (info.pushIntoDownloadQueue) {
                            Log.d(TAG, "1hint|" + info.hint)
                            var cache = CommonVideoCache(info.commonVideo)
                            cache.setKey(info.key)
                            cache.setUrl(info.url)
                            cache.setPath(info.path)
                            cache.setState(DownloadState.WAITING.code)
                            cache.setTitle(info.title)
                            cache.setMsg(info.msg)
                            cacheViewModel.saveCache(cache)
                        } else {
                            Log.d(TAG, "2hint|" + info.hint)
                            var commonVideoCache = CommonVideoCache()
                            commonVideoCache.setTitle(info.title)
                            commonVideoCache.setMsg(info.msg)
                            Observable.just(commonVideoCache)
                        }
                    }
                    .compose(asyncSchedulers())
                    .subscribe(
                            { Timber.i("re-download 'jump the queue' cache data success") },
                            { Timber.i("re-download 'jump the queue' cache data failure") })
            return true
        }
        return false
    }

    fun tryToReDownload(context: Context, key: String): Observable<CommonVideoCache> {
        Log.d(TAG, "tryToReDownload|" + key)
        return cacheViewModel.getDataByKey(key)
                .flatMap { cache ->
                    Log.d(TAG, "getDataByKey|" + cache.getKey() + "|path|" + cache.getPath() + "|p|" + cache.getProgress() + "|videoId|" + cache.getCommonVideo()?.video_id)
                    if (cache != null && !TextUtils.isEmpty(cache.getKey())) {
                        cache.setState(DownloadState.WAITING.code) // 状态置为"等待下载"，准备重启下载
                        cache.setProgress(0) // 重新下载，进度为0
                        cache.setCompleteSize(0L) // 重新下载，已完成长度为0
                        cache.setTotalSize(0L) // 重新下载，总长度为0
                        cache.setPath("") // 重新下载，路径失效，置为空
                        cache.setAutoStart(false) // 重新下载，重置状态
                        cache.setClicked(false) // 重新下载，重置状态
                        cacheViewModel.saveCache(cache)
                    } else {
                        Observable.just(CommonVideoCache())
                    }
                }
    }

    fun setVideoCacheClicked(context: Context, key: String) {
        if (context == null || key == null || key.length == 0) {
            return
        }
        cacheViewModel.getDataByKey(key)
                .flatMap { cache ->
                    Log.d(TAG, "getDataByKey|" + cache.getKey() + "|path|" + cache.getPath() + "|p|" + cache.getProgress() + "|videoId|" + cache.getCommonVideo()?.video_id)
                    if (cache != null && cache.getState() == DownloadState.SUCCESS.code) {
                        cache.setClicked(true)
                        postEvent(VideoDownloadClickedEvent(key))
                        cacheViewModel.saveCache(cache)
                    } else {
                        Observable.just(cache)
                    }
                }
                .compose(asyncSchedulers())
                .subscribe(
                        { Timber.i("Clicked|getDataByKey(" + key + ")success") },
                        { Timber.i("Clicked|getDataByKey(" + key + ")failure") })

    }

    protected var progressive = ConcurrentHashMap<String, ProgressInfo>()
    protected var appended = ConcurrentHashMap<String, Long>()

    class ProgressInfo(var key: String, var url: String?, var progress: Int, var completeSize: Long, var totalSize: Long)

    override fun notifyDownloadWaiting(key: String?, url: String?) {
        if (key == null || key.length == 0) {
            return
        }
        cacheViewModel.getDataByKey(key)
                .map { cache ->
                    var hasTask = DownloadManager.get().hasTask(key)
                    var hasRunningTask = DownloadManager.get().hasRunningTask(key)
                    Log.d(TAG, ">Waiting|" + key + "|" + cache.getCompleteSize() + "|" + cache.getTotalSize() + "|[" + cache.getProgress() + "]|" + cache.getAppendSize() + "|hasTask|" + hasTask + "|hasRunning|" + hasRunningTask)
                    postEvent(VideoDownloadWaitingEvent(key))
                    cache
                }
                .compose(asyncSchedulers())
                .subscribe(
                        { Timber.i("Waiting|getDataByKey(" + key + ")success") },
                        { Timber.i("Waiting|getDataByKey(" + key + ")failure") })

    }

    override fun notifyDownloadStart(key: String?, url: String?) {
        if (key == null || key.length == 0) {
            return
        }
        cacheViewModel.getDataByKey(key)
                .flatMap { cache ->
                    if (cache != null && !TextUtils.isEmpty(cache.getKey())
                            && cache.getState() != DownloadState.SUCCESS.code
                            && cache.getState() != DownloadState.FAILURE.code) {
                        cache.setState(DownloadState.START.code)
                        Log.d(TAG, "Start|" + key)
                        postEvent(VideoDownloadStartEvent(key))
                        cacheViewModel.saveCache(cache, true)
                    } else {
                        Observable.just(cache)
                    }
                }
                .compose(asyncSchedulers())
                .subscribe(
                        { Timber.i("Start|getDataByKey(" + key + ")success") },
                        { Timber.i("Start|getDataByKey(" + key + ")failure") })
    }

    override fun notifyDownloadCancel(key: String?, url: String?) {
        Log.d(TAG, "Cancel|" + key)
    }

    override fun notifyDownloadProgress(key: String?, url: String?, progress: Int, completeSize: Long, totalSize: Long) {
        if (key != null) {
            progressive.put(key, ProgressInfo(key, url, progress, completeSize, totalSize))
            runProgressByPeriod()
        }
    }

    override fun notifyDownloadSuccess(key: String?, url: String?, path: String?) {
        Log.d(TAG, "Success|" + key + "|" + path)
    }

    override fun notifyDownloadClear(key: String?, success: Boolean, url: String?, path: String?, error: DownloadError?) {
        if (success) {
            Log.d(TAG, "Complete|" + key + "|" + path + "|success")
        } else {
            Log.d(TAG, "Complete|" + key + "|" + path + "|" + error)
        }
        if (key != null) {
            progressive.remove(key)
            if (success) {
                onCompleteSuccess(key, path)
            } else {
                onCompleteFailure(key, error)
            }
        }
    }

    override fun notifyDownloadFailure(key: String?, url: String?, error: DownloadError?, message: String?) {
        Log.d(TAG, "Failure|" + key + "|" + message + "|" + error)
    }

    fun hasProgress(): Boolean {
        return !progressive.isEmpty()
    }

    var waitting: Boolean = false

    fun runProgressByPeriod() {
        if (!waitting) {
            waitting = true
            val progress = progressive.values
            for (p in progress) {
                if (p != null) {
                    onProgressChanged(p)
                }
            }
            counting()
        }
    }

    private fun counting() {
        Handler(Looper.myLooper()).postDelayed(runnable, period)
    }

    var runnable = Runnable {
        if (hasProgress()) {
            waitting = false
        } else {
            counting()
        }
    }

    private fun onProgressChanged(p: ProgressInfo) {
        if (p == null || p.key == null || 99 < p.progress || p.progress < 0) {
            return
        }
        cacheViewModel.getDataByKey(p.key)
                .flatMap { cache ->
                    if (cache != null && !TextUtils.isEmpty(cache.getKey())
                            && cache.getState() != DownloadState.SUCCESS.code
                            && cache.getState() != DownloadState.FAILURE.code) {
                        var appendSize = p.completeSize - cache.getCompleteSize()
                        appended.put(p.key, appendSize)
                        cache.setProgress(p.progress)
                        cache.setCompleteSize(p.completeSize)
                        cache.setTotalSize(p.totalSize)
                        Log.d(TAG, "Progress|" + p.key + "|" + p.completeSize + "|" + p.totalSize + "|[" + p.progress + "]|" + appendSize)
                        postEvent(VideoDownloadProgressEvent(p.key, p.progress, p.completeSize, p.totalSize, appendSize))
                        cacheViewModel.saveCache(cache)
                    } else {
                        Observable.just(cache)
                    }
                }
                .compose(asyncSchedulers())
                .subscribe(
                        { Timber.i("ProgressChanged|getDataByKey(" + p.key + ")success") },
                        { Timber.i("ProgressChanged|getDataByKey(" + p.key + ")failure") })
        }

    private fun onCompleteSuccess(key: String?, path: String?) {
        if (key == null || key.length == 0) {
            return
        }
        if (path == null || path.length == 0) {
            return
        }
        cacheViewModel.getDataByKey(key)
                .flatMap { cache ->
                    cache.setProgress(100)
                    cache.setPath(path)
                    cache.setCompleteSize(cache.getTotalSize())
                    cache.setState(DownloadState.SUCCESS.code)
                    cache.setClicked(false) // 成功后置初值
                    cache.setAutoStart(false) // 成功后标记位清除
                    cache.setFailReason("") // 成功后标记值清除
                    Log.d(TAG, ">onCompleteSuccess|" + key + "|path|" + path)
                    postEvent(VideoDownloadCompleteEvent(key, true, path, 0))
                    cacheViewModel.saveCache(cache)
                }
                .compose(asyncSchedulers())
                .subscribe(
                        { Timber.i("CompleteSuccess|getDataByKey(" + key + ")success") },
                        { Timber.i("CompleteSuccess|getDataByKey(" + key + ")failure") })
    }

    fun onCompleteFailure(key: String?, error: DownloadError?) {
        if (key == null || key.length == 0 || error == null) {
            return
        }
        if (DownloadError.INTERRUPT_FOR_JUMP_THE_QUEUE == error) {
            // 如果是被其他任务插队造成的失败，马上重新下载
            if (downloadJumpTheQueueTask(appContext, key)) {
                return
            }
        }
        // 有3中状态：
        // 文案1.已暂停（下载中，用户手动暂停，注意是手动）
        // 文案2.下载失败，受版权保护（3次下载重试Server都返回FileNotFound做了防盗机制）
        // 文案3.下载失败，请重试（所有除1，2的其他情况）
        var failReason = ""
        if (DownloadError.INTERRUPT_FOR_MANUAL == error) {
            failReason = appContext.getString(R.string.video_cache_fail_for_manual_reason)
        } else if (DownloadError.SERVER_ERROR == error) {
            failReason = appContext.getString(R.string.video_cache_fail_for_server_reason)
        } else {
            Log.d(TAG, ">OtherFailReason|" + key + "|error|" + error)
            failReason = appContext.getString(R.string.video_cache_fail_for_others_reason)
        }
        cacheViewModel.getDataByKey(key)
                .flatMap { cache ->
                    cache.setState(DownloadState.FAILURE.code)
                    cache.setFailReason(failReason)
                    var errorCode = 0
                    if (error != null) {
                        errorCode = error.ordinal
                        cache.setErrorCode(errorCode)
                    }
                    Log.d(TAG, ">onCompleteFailure|" + key)
                    postEvent(VideoDownloadCompleteEvent(key, false, failReason, errorCode))
                    cacheViewModel.saveCache(cache)
                }
                .compose(asyncSchedulers())
                .subscribe(
                        { Timber.i("CompleteFailure|getDataByKey(" + key + ")success") },
                        { Timber.i("CompleteFailure|getDataByKey(" + key + ")failure") })
    }

    fun release() {
        // 目前全局，可能永远调用不到了
        observeNetworkSubscription?.unsubscribe()
    }
}