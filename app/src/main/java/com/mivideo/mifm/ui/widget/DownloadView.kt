package com.mivideo.mifm.ui.widget

import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.NetworkManager
import com.mivideo.mifm.R
import com.mivideo.mifm.cache.AudioCacheManager
import com.mivideo.mifm.cache.VideoCacheUtils
import com.mivideo.mifm.data.models.AudioInfo
import com.mivideo.mifm.data.models.jsondata.common.CommonVideoCache
import com.mivideo.mifm.data.models.jsondata.download.DownloadInfo
import com.mivideo.mifm.data.viewmodel.CacheViewModel
import com.mivideo.mifm.download.support.DownloadState
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.util.app.showToast
import org.jetbrains.anko.onClick
import rx.Observable

class DownloadView : AppCompatImageView, KodeinInjected {

    override val injector = KodeinInjector()
    private val cacheViewModel: CacheViewModel by instance()
    private val audioCacheManager: AudioCacheManager by instance()

    private var audioInfo: AudioInfo? = null

    private var defaultDelay = 300L
    private var lastDuration = 0L
    private var currDuration = 0L

    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        init()
    }

    fun init() {
        inject(appKodein())
        onClick {
            downloadInOrder()
        }
    }

    fun setCurrentAudioInfo(audioInfo: AudioInfo) {
        this.audioInfo = audioInfo
    }

    fun getCurrentAudioInfo(): AudioInfo? {
        return audioInfo
    }

    fun downloadInOrder() {
        download(false)
    }

    fun downloadAndJumpTheQueue() {
        download(true)
    }

    private fun download(jumpTheQueue: Boolean) {
        currDuration = System.currentTimeMillis()
        if (lastDuration + defaultDelay > currDuration) {
            return
        }
        lastDuration = System.currentTimeMillis()

        if (!NetworkManager.isNetworkConnected(context.applicationContext)) {
            showToast(context.applicationContext, context.applicationContext.getString(R.string.video_cache_network_illegal))
            return
        }

        if (audioInfo == null) {
            return
        }
        val audio = audioInfo!!

        var isUseWifiConnected = NetworkManager.isUseWifiConnected(context.applicationContext)
        Log.d("DM", "DownloadView|download|isUseWifiConnected|" + isUseWifiConnected)
        if (isUseWifiConnected) {
            // 这种情况不提示直接执行下载流程
            Log.d("DM", "DownloadView.download---" + audio?.passageItem.id + "|" + audio?.albumInfo.id)
            downloadAudio(audio, jumpTheQueue)
        } else {
            // 提示用户"非WIFI网络，已添加到下载队列"
            Log.d("DM", "DownloadView.enqueue---" + audio?.passageItem.id + "|" + audio?.albumInfo.id)
            enqueueAudio(audio, jumpTheQueue)
        }
    }

    /**
     * 进下载列表然后下载
     */
    private fun downloadAudio(audioInfo: AudioInfo, jumpTheQueue: Boolean) {
        audioCacheManager.processCommonAudioInfo(context.applicationContext, audioInfo)
                .flatMap { info ->
                    if (info != null && !TextUtils.isEmpty(info.key)) {
                        info.jumpTheQueue = jumpTheQueue
                    }
                    // 前期检查工作完成，判断是否交给下载模块进行下载处理
                    if (info.canTryStartDownload) {
                        audioCacheManager.startDownloadAudio(context.applicationContext, info, audioInfo)
                    } else {
                        Observable.just(info)
                    }
                }
                .flatMap { info ->
                    // 下载模块将任务加入下载列表，判断是否写入数据
                    if (info.pushIntoDownloadQueue) {
                        Log.d("DM", "1hint|" + info.hint)
                        var cache = CommonVideoCache(audioInfo)
                        cache.setKey(info.key)
                        cache.setUrl(info.url)
                        cache.setPath(info.path)
                        cache.setState(DownloadState.WAITING.code)
                        cache.setTitle(info.title)
                        cache.setMsg(info.msg)
                        cacheViewModel.saveCache(cache)
                    } else {
                        Log.d("DM", "2hint|" + info.hint)
                        var commonVideoCache = CommonVideoCache()
                        commonVideoCache.setTitle(info.title)
                        commonVideoCache.setMsg(info.msg)
                        Observable.just(commonVideoCache)
                    }
                }
                .compose(asyncSchedulers())
                .subscribe(
                        { result ->
                            var message = result.getMsg()
                            Log.d("DM", "downloadVideo|msg|" + message)
                            if (!TextUtils.isEmpty(message)) {
                                showToast(context.applicationContext, message.toString())
                            }
                        },
                        { Log.d(VideoCacheUtils.TAG, "DM|downloadVideo|fail") })
    }

    /**
     * 只进下载列表不下载，wifi时自动下载
     */
    private fun enqueueAudio(audioInfo: AudioInfo, jumpTheQueue: Boolean) {
        audioCacheManager.processCommonAudioInfo(context.applicationContext, audioInfo)
                .flatMap { info ->
                    if (info != null && !TextUtils.isEmpty(info.key)) {
                        info.jumpTheQueue = jumpTheQueue
                    }
                    Log.d("DM", "enqueueVideo|checkAlreadyEnqueued")
                    checkAlreadyEnqueued(info)
                } // 检查出已经入队列，不操作不提示
                .flatMap { info ->
                    // 前期检查工作完成，判断是否写入数据
                    var cache = CommonVideoCache(audioInfo)
                    if (info.alreadyInQueue) {
                        Log.d("DM", "enqueueVideo|alreadyInQueue")
                        cache.setTitle(info.title)
                        cache.setMsg("") // 已经入队列的不用再提示
                        Observable.just(cache)
                    } else if (info.canTryStartDownload) {
                        cache.setKey(info.key)
                        cache.setUrl(info.url)
                        cache.setState(DownloadState.WAITING.code)
                        cache.setProgress(0)
                        cache.setCompleteSize(0L)
                        cache.setTotalSize(0L)
                        cache.setPath("")
                        Log.d("DM", "enqueueVideo|canTryStartDownload")
                        cache.setAutoStart(true) // enqueue情况，需要autoStart
                        cache.setTitle(info.title)
                        cache.setMsg(info.msg)
                        cacheViewModel.saveCache(cache)
                    } else {
                        Log.d("DM", "enqueueVideo|")
                        cache.setTitle(info.title)
                        cache.setMsg(info.msg)
                        Observable.just(cache)
                    }
                }
                .compose(asyncSchedulers())
                .subscribe(
                        { result ->
                            var message = result.getMsg()
                            Log.d("DM", "enqueueVideo|msg|" + message)
                            if (!TextUtils.isEmpty(message)) {
                                showToast(context.applicationContext, message.toString())
                            }
                        },
                        { Log.d(VideoCacheUtils.TAG, "DM|enqueueVideo|fail") })
    }

    private fun checkAlreadyEnqueued(info: DownloadInfo): Observable<DownloadInfo> {
        if (info == null || TextUtils.isEmpty(info.key)) {
            return Observable.just(info)
        }
        return cacheViewModel.getDataByKey(info.key)
                .flatMap { cache ->
                    if (cache != null && !TextUtils.isEmpty(cache.getKey())) {
                        info.alreadyInQueue = true
                        Observable.just(info)
                    } else {
                        info.alreadyInQueue = false
                        Observable.just(info)
                    }
                }
    }
}