package com.mivideo.mifm.player

import android.app.Activity
import android.arch.lifecycle.Lifecycle
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork
import com.github.salomonbrys.kodein.instance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mivideo.mifm.MainApp
import com.mivideo.mifm.NetworkManager
import com.mivideo.mifm.data.models.jsondata.common.VideoInfoParams
import com.mivideo.mifm.rx.asyncSchedulers
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * 播放器统一管理类
 *
 * @author LiYan
 */
class KPlayerManager(val mContext: Context) {
    companion object {

        private var instance: KPlayerManager? = null

        fun get(context: Context): KPlayerManager {
            if (instance == null) {
                synchronized(KPlayerManager::class.java) {
                    if (instance == null) {
                        instance = KPlayerManager(context)
                    }
                }
            }
            return instance!!
        }

    }

//    private var shareHelper: ShareHelper? = null
    private var playerController: VideoController? = null
    private var playerView: KPlayerView? = null
//    private var pluginManager: PluginManager
//    private var videoCacheManager: AudioCacheManager

    init {
        observeNetworkChange(mContext)
        val app = mContext.applicationContext as MainApp
//        pluginManager = app.kodein.instance()
//        videoCacheManager = app.kodein.instance()
    }

    private var observeNetworkSubscription: Subscription? = null

    private fun observeNetworkChange(context: Context) {
        observeNetworkSubscription = ReactiveNetwork
                .observeNetworkConnectivity(context.applicationContext)
                .subscribeOn(Schedulers.io())
                .debounce(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { connectivity ->
                    if (connectivity.state == NetworkInfo.State.CONNECTED) {
                        if (connectivity.type == ConnectivityManager.TYPE_WIFI) {
                            playerController?.onWifiConnected()
                        } else if (connectivity.type == ConnectivityManager.TYPE_MOBILE) {
                            playerController?.onMobileNetConnected()
                        }
                    } else if (connectivity.state == NetworkInfo.State.DISCONNECTED) {
                        playerController?.onNetworkDisConnected()
                    }
                }
    }

    fun processVideoInfoParams(video: VideoInfoParams): Observable<VideoInfoParams> {
//        if (!NetworkManager.isNetworkConnected(mContext)) {
//            // 无网情况下，先检查缓存，缓存存在，返回缓存数据，缓存不存在，抛异常
//            var cacheFilePath = VideoCacheUtils.findVideoCachePath(mContext,
//                    NetworkParams.getMD5(video.commonVideo.video_id), VideoDefinition.DEFINITION_LOW)
//            if (cacheFilePath != null && cacheFilePath.length > 0) {
//                Log.d(VideoCacheUtils.TAG, "processVideoInfoParams|find cacheFilePath|" + video.commonVideo.video_id + "|" + cacheFilePath)
//                video.commonVideo.playUrl.ld_play_url_list.add(0, cacheFilePath)
//                video.commonVideo.playUrl.nd_play_url_list.add(0, cacheFilePath)
//                video.commonVideo.playUrl.hd_play_url_list.add(0, cacheFilePath)
//                video.commonVideo.playUrl.sd_play_url_list.add(0, cacheFilePath)
//                return Observable.just(video)
//            } else {
//                Log.d(PluginUtil.TAG, "Exception|processVideoInfoParams no network connected")
//                return Observable.error(IllegalStateException("processVideoInfoParams no network connected"))
//            }
//        }
//        return pluginManager.checkCpNeedPlugin(mContext, video.commonVideo.cp)
//                .flatMap { needPlugin -> processVideoSourceByState(needPlugin, video) }
//                .compose(asyncSchedulers())
        return processVideoSourceByDefault(video)
    }

//    private fun processVideoSourceByState(needPlugin: Boolean, video: VideoInfoParams): Observable<VideoInfoParams> {
//        if (needPlugin) {
//            var isUseWifi = NetworkManager.isUseWifiConnected(mContext)
//            var targetDefinition = pluginManager.getDefinitionByCurrentNetwork(isUseWifi)
//            var content = pluginManager.createPluginContentByCommonVideo(mContext.applicationContext, video.commonVideo)
//            return pluginManager.checkAndRequestPlugin(mContext,
//                    video.commonVideo.cp, video.commonVideo.play_url, content, targetDefinition)
//                    .map { payload ->
//                        val ex = Gson().fromJson<Ex>(payload.ex, object : TypeToken<Ex>() {}.type)
//                        if (ex == null) {
//                            var message = "|ex|${payload.ex}|cp|${payload.cp}|id|${payload.identify}|state|${payload.state}|STATE|${TaskPayloadState.parse(payload.state)}|tag|${payload.tag}"
//                            Log.d(PluginUtil.TAG, "Exception|processVideo|Ex must not be null" + message)
//                            throw IllegalStateException("processVideo|Ex must not be null" + message)
//                        }
//                        Timber.i("nd size: ${ex.normalDefList.size}")
//                        Timber.i("ld size: ${ex.lowDefList.size}")
//                        Timber.i("hd size: ${ex.highDefList.size}")
//                        Timber.i("sd size: ${ex.superDefList.size}")
//                        Log.d(PluginUtil.TAG, "processVideo|cp|${payload.cp}|id|${payload.identify}|state|${payload.state}|definition|${ex.definitionList}|tag|${payload.tag}")
//                        if (PluginUtil.isPluginReturnError(payload.state)) {
//                            Log.e(PluginUtil.TAG, "##processVideo|PluginExceptions|${payload.state}")
//                            throw PluginException(payload.state.toString())
//                        }
//                        Log.d(PluginUtil.TAG, "low size: ${ex.lowDefList.size}")
//                        Log.d(PluginUtil.TAG, "normal size: ${ex.normalDefList.size}")
//                        Log.d(PluginUtil.TAG, "high size: ${ex.highDefList.size}")
//                        Log.d(PluginUtil.TAG, "super size: ${ex.superDefList.size}")
//                        if (ex.lowDefList.size == 0 && ex.normalDefList.size == 0
//                                && ex.highDefList.size == 0 && ex.superDefList.size == 0) {
//                            Log.d(PluginUtil.TAG, "Exception|cp-plugin process return extra has no available play list")
//                            throw IllegalStateException("cp-plugin process return extra has no available play list")
//                        }
//                        var ver = ex.ver
//                        var realDefinition: VideoDefinition? = null
//                        var abilityFlag = ex.ability
//                        var canChangeDefinition = PluginAbility.match(abilityFlag, PluginAbility.CAN_CHANGE_DEFINITION)
//                        if (canChangeDefinition) {
//                            // 能支持分辨率切换的渠道，用回传的确切url分辨率来确定真正的分辨率
//                            realDefinition = VideoDefinition.parse(ex.definition)
//                        }
//                        if (realDefinition == null) {
//                            // 不支持分辨率切换或无法确定url分辨率的情况，统一当作默认的normal分辨率
//                            realDefinition = VideoDefinition.DEFINITION_NORMAL
//                        }
//                        var canQueryPlayUrl = PluginAbility.match(abilityFlag, PluginAbility.CAN_QUERY_PLAY_URL)
//                        Log.d(PluginUtil.TAG, "ability|" + abilityFlag + "|canChangeDefinition|" + canChangeDefinition + "|canQueryPlayUrl|" + canQueryPlayUrl + "|ver|" + ver)
//                        // 特殊渠道（或者插件渠道）几乎都可以成功下载，所以要做缓存判断
//                        // 1.如果点击之前是wifi，特殊渠道在wifi环境下默认取super分辨率（当然一般取回的还是normal／high分辨率），按照确定的分辨率，选取缓存（优先顺序super／high／normal／low）
//                        // 2.如果点击之前是数据网络，特殊渠道在数据网环境下默认取normal分辨率（取回的只有normal／low分辨率），按照确定的分辨率，选取缓存（优先顺序super／high／normal／low）
//                        var cacheFilePath = VideoCacheUtils.findVideoCachePath(mContext,
//                                NetworkParams.getMD5(video.commonVideo.video_id), realDefinition)
//                        Log.d(VideoCacheUtils.TAG, "find cacheFilePath|" + video.commonVideo.video_id + "|" + cacheFilePath)
//                        video.commonVideo.playUrl.ld_play_url_list = ex.lowDefList
//                        video.commonVideo.playUrl.nd_play_url_list = ex.normalDefList
//                        video.commonVideo.playUrl.hd_play_url_list = ex.highDefList
//                        video.commonVideo.playUrl.sd_play_url_list = ex.superDefList
//                        video.commonVideo.playUrl.supportResolutionList = ex.definitionList
//                        if (cacheFilePath != null && cacheFilePath.length > 0) {
//                            video.commonVideo.playUrl.ld_play_url_list.add(0, cacheFilePath)
//                            video.commonVideo.playUrl.nd_play_url_list.add(0, cacheFilePath)
//                            video.commonVideo.playUrl.hd_play_url_list.add(0, cacheFilePath)
//                            video.commonVideo.playUrl.sd_play_url_list.add(0, cacheFilePath)
//                        }
//                        video
//                    }
//        } else {
//            processVideoSourceByDefault(video)
//        }
//    }

    private fun processVideoSourceByDefault(video: VideoInfoParams): Observable<VideoInfoParams> {
        return Observable
                .create(Observable.OnSubscribe<ArrayList<String>> { subscriber ->
                    if (video.commonVideo.video_id == null) {
                        subscriber.onError(Throwable("path null"))
                    } else {
                        Timber.i("origin url size: ${video.commonVideo.play_url}")
                        var paths = ArrayList<String>()
                        paths.add(video.commonVideo.play_url)
                        subscriber.onNext(paths)
                    }
                    subscriber.onCompleted()
                })
                .map {
//                    video.commonVideo.playUrl.nd_play_url_list = it
                    Log.d("PPP", "processVideoSourceByDefault|" + it)
                    var testurl = "http://obj.auto-learning.com/354e1487-18e6-4ad0-8980-24dc299be00f"
                    video.commonVideo.playUrl.nd_play_url_list.clear()
                    video.commonVideo.playUrl.nd_play_url_list.add(testurl)
                    Log.d("PPP", "origin url size: ${video.commonVideo.playUrl.nd_play_url_list.size}")
                    Log.d("PPP", "video: $video")
                    // 默认渠道（或一般渠道）大部分的play_url都可以成功下载，那么也需要做缓存判断
                    // 默认渠道（或一般渠道）分辨率默认，默认的分辨率是normal，按normal分辨率处理
//                    var cacheFilePath = VideoCacheUtils.findVideoCachePath(mContext,
//                            NetworkParams.getMD5(video.commonVideo.video_id), VideoDefinition.DEFINITION_NORMAL)
//                    Log.d(VideoCacheUtils.TAG, "find cacheFilePath|" + video.commonVideo.video_id + "|" + cacheFilePath)
//                    if (cacheFilePath != null && cacheFilePath.length > 0) {
//                        video.commonVideo.playUrl.nd_play_url_list.add(0, cacheFilePath)
//                    }
//                    Timber.i("origin url size: ${video.commonVideo.playUrl.nd_play_url_list.size}")
                    video
                }
                .compose(asyncSchedulers())
    }

    fun handleVideoSourcePlayError(video: VideoInfoParams) {
//        pluginManager.callbackCpPlugin(mContext, video.commonVideo.play_url,
//                video.commonVideo.cp, TaskPayloadState.HOST_CALLBACK_FAILURE)
    }

    fun handleVideoSourcePlaySuccess(video: VideoInfoParams) {
        Timber.i("handleVideoSourcePlaySuccess")
//        pluginManager.callbackCpPlugin(mContext, video.commonVideo.play_url,
//                video.commonVideo.cp, TaskPayloadState.HOST_CALLBACK_SUCCESS)
    }

    /**
     * 释放播放器引用资源
     */
    fun releasePlayer() {
        playerController?.release()
        playerView = null
        playerController = null
    }

    /**
     * 设置播放器给当前管理器，当前管理器由于是一个单例
     * 用于全局控制当前设置的播放器
     */
    fun setCurrentPlayer(view: KPlayerView) {
//        if (shareHelper == null) {
//            val socialManager = SocializeManager.get(view.context as Activity)
//            shareHelper = ShareHelper(socialManager, "FullScreen")
//        }
        this.playerView = view
        this.playerController = view.getController()
//        this.playerView?.setShareClickListener(object : ShareListener {
//            override fun onShareWx() {
//                if (playerController?.getPlayVideoInfo() != null) {
//                    val videoInfo = playerController?.getPlayVideoInfo()!!
//                    val shareInfo = ShareHelper.buildShareInfo(videoInfo.commonVideo)
//                    shareHelper?.shareWx(shareInfo)
//                }
//            }
//
//            override fun onShareWxMoments() {
//                if (playerController?.getPlayVideoInfo() != null) {
//                    val videoInfo = playerController?.getPlayVideoInfo()!!
//                    val shareInfo = ShareHelper.buildShareInfo(videoInfo.commonVideo)
//                    shareHelper?.shareWxMoments(shareInfo)
//                }
//            }
//
//            override fun onShareQQ() {
//                if (playerController?.getPlayVideoInfo() != null) {
//                    val videoInfo = playerController?.getPlayVideoInfo()!!
//                    val shareInfo = ShareHelper.buildShareInfo(videoInfo.commonVideo)
//                    shareHelper?.shareQQ(shareInfo)
//                }
//            }
//
//            override fun onShareWeibo() {
//                if (playerController?.getPlayVideoInfo() != null) {
//                    val videoInfo = playerController?.getPlayVideoInfo()!!
//                    val shareInfo = ShareHelper.buildShareInfo(videoInfo.commonVideo)
//                    shareHelper?.shareWeiBo(shareInfo)
//
//                }
//            }
//
//        })
    }

    fun getCurrentPlayer(): KPlayerView? {
        return this.playerView
    }

    fun onBackPressed(): Boolean {
        if (playerController?.isFullScreen() == true) {
            playerController?.handleFullScreen()
            return true
        }
        return false
    }

    fun attachLifecycleToPlayer(lifecycle: Lifecycle) {
        if (playerController != null) {
            playerController!!.mLifecycle = lifecycle
            val lifecycleObserver = PlayerLifecycleObserver(playerController!!)
            playerController!!.playerLifecycleObserver = lifecycleObserver
            lifecycle.addObserver(lifecycleObserver)
        }

    }

    fun release() {
        observeNetworkSubscription?.unsubscribe()
//        shareHelper?.release()
        releasePlayer()
        releaseVideoSourcer()
        instance = null
    }

    private fun releaseVideoSourcer() {
//        pluginManager.clearAllCpPlugin(mContext.applicationContext)
    }

}