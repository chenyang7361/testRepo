package com.mivideo.mifm.download.support

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.text.TextUtils
import android.util.Log
import com.mivideo.mifm.cpplugin.PluginUtil
import com.mivideo.mifm.data.models.jsondata.common.CommonVideoCache
import rx.Observable
import java.util.concurrent.*

/**
 * management download files.
 */
class DownloadManager : FileDownloadImpl {
    companion object {
        private var instance: DownloadManager? = null
        fun get(): DownloadManager {
            if (instance == null) {
                synchronized(DownloadManager::class.java) {
                    if (instance == null) {
                        instance = DownloadManager()
                    }
                }
            }
            return instance!!
        }
    }

    constructor() {
        initHandler("init thread handler at construct")
    }

    private fun initHandler(desc: String?) {
        if (mHandler == null) {
            var postThread = HandlerThread("download-post-state-thread")
            postThread.start()
            mHandler = Handler(postThread.looper)
            if (!TextUtils.isEmpty(desc)) {
                Log.d("DM", desc)
            }
        }
    }

    private var globalObservers = ConcurrentHashMap<String, FileDownloadImpl>()

    fun init(appContext: Context) {
        if (PluginUtil.isPluginProcess(appContext)) {
            // 防止生成两个下载管理，导致同一个文件两个进程一起下载，内存数据数据库数据和文件状态均混乱
            return
        }
        var videoGlobalObserver = globalObservers.get(DownloadType.VIDEO.name)
        if (videoGlobalObserver == null) {
            videoGlobalObserver = VideoGlobalObserver(appContext.applicationContext)
            globalObservers.put(DownloadType.VIDEO.name, videoGlobalObserver)
        }
    }

    fun downloadAutoStartVideos(appContext: Context) {
        init(appContext)
        var videoGlobalObserver = globalObservers.get(DownloadType.VIDEO.name)
        if (videoGlobalObserver is VideoGlobalObserver) {
            videoGlobalObserver.downloadAutoStartTasks(appContext)
        }
    }

    fun setVideoCacheClicked(appContext: Context, key: String) {
        init(appContext)
        var videoGlobalObserver = globalObservers.get(DownloadType.VIDEO.name)
        if (videoGlobalObserver is VideoGlobalObserver) {
            videoGlobalObserver.setVideoCacheClicked(appContext, key)
        }
    }

    fun tryToReDownloadVideo(context: Context, key: String): Observable<CommonVideoCache> {
        init(context.applicationContext)
        var videoGlobalObserver = globalObservers.get(DownloadType.VIDEO.name)
        if (videoGlobalObserver is VideoGlobalObserver) {
            return videoGlobalObserver.tryToReDownload(context.applicationContext, key)
        } else {
            return Observable.create(Observable.OnSubscribe<CommonVideoCache> { subscriber ->
                subscriber.onNext(CommonVideoCache())
                subscriber.onCompleted()
            })
        }
    }

    fun setVideoTaskFailure(context: Context, key: String, error: DownloadError) {
        init(context.applicationContext)
        var videoGlobalObserver = globalObservers.get(DownloadType.VIDEO.name)
        if (videoGlobalObserver is VideoGlobalObserver) {
            videoGlobalObserver.onCompleteFailure(key, error)
        }
    }

    protected var downloadPluginExecutor: ExecutorService? = null
    protected var downloadVideoExecutor: ThreadPoolExecutor? = null
    protected var mHandler: Handler? = null
    protected var observers = ConcurrentHashMap<String, List<DownloadListener>>()
    protected var videoCachePath: String? = null

    private fun addObserver(url: String, listener: List<DownloadListener>) {
        observers.put(url, listener)
    }

    private fun getObserver(url: String): List<DownloadListener>? {
        return observers.get(url)
    }

    private fun removeObserver(url: String): List<DownloadListener>? {
        return observers.remove(url)
    }

    protected var ftasks = ConcurrentHashMap<String, DownloadWorker>()

    private fun addTasks(url: String, worker: DownloadWorker) {
        ftasks.put(url, worker)
    }

    private fun getTasks(url: String): DownloadWorker? {
        return ftasks.get(url)
    }

    private fun removeTasks(url: String, key: String) {
        var task = ftasks.remove(url)
        if (task == null) {
            var it = ftasks.iterator()
            while (it.hasNext()) {
                var i = it.next()
                if (i != null && i.key == key) {
                    it.remove()
                }
            }
        }
    }

    fun getOlderRunningTask(): DownloadWorker? {
        var olderTask: DownloadWorker? = null
        var ts = System.currentTimeMillis()
        for (task in ftasks.values) {
            if (task.running()) {
                if (ts > task.startTimeStamp()) {
                    ts = task.startTimeStamp()
                    olderTask = task
                }
            }
        }
        return olderTask
    }

    fun hasTask(key: String?): Boolean {
        if (key == null || key.length == 0) {
            return false
        }
        var found = false
        for (task in ftasks.values) {
            if (task != null && key == task.key) {
                found = true
                break
            }
        }
        return found
    }

    fun hasRunningTask(key: String?): Boolean {
        if (key == null || key.length == 0) {
            return false
        }
        var found = false
        for (task in ftasks.values) {
            if (task != null && key == task.key && task.running()) {
                found = true
                break
            }
        }
        return found
    }

    private fun getTask(key: String?): DownloadWorker? {
        var worker: DownloadWorker? = null
        if (key == null || key.length == 0) {
            return worker
        }
        for (task in ftasks.values) {
            if (task != null && key == task.key) {
                worker = task
                break
            }
        }
        return worker
    }

    fun cancelTaskByKey(key: String?) {
        cancelTaskByKey(key, null)
    }

    fun cancelTaskByKey(key: String?, reason: InterruptReason?) {
        if (key == null || key.length == 0) {
            return
        }
        var interruptReason = reason
        if (interruptReason == null) {
            interruptReason = InterruptReason.IGNORE
        }
        for (task in ftasks.values) {
            if (task != null && key == task.key) {
                Log.d("DM", "cancelTask|" + key + "|" + interruptReason)
                task.setInterrupt(interruptReason)
            }
        }
    }

    fun cancelAll(reason: InterruptReason?) {
        var interruptReason = reason
        if (interruptReason == null) {
            interruptReason = InterruptReason.IGNORE
        }
        for (task in ftasks.values) {
            if (task != null) {
                Log.d("DM", "cancelAll|" + task.key + "|" + interruptReason)
                task.setInterrupt(interruptReason)
            }
        }
    }

    fun getDownloadPluginDir(context: Context): String? {
        if (null == videoCachePath) {
            videoCachePath = PluginUtil.getPatchDirPath(context)
        }
        return videoCachePath
    }

    fun getDownloadVideoCacheDir(context: Context): String? {
        if (TextUtils.isEmpty(videoCachePath)) {
            videoCachePath = DownloadUtil.getVideoDirPath(context)
        }
        return videoCachePath
    }

    @Synchronized
    fun downloadPlugin(context: Context, identify: String, suffix: String?, md5: String?,
                       url: String?, listener: DownloadListener?) {
        init(context.applicationContext)
        initHandler("init thread handler at download request")
        if (url == null || url.length == 0) {
            return
        }
        var l: DownloadListener? = listener
        if (l == null) {
            l = object : DownloadAdapter() {

            }
        }
        if (l.checkInitialized(context, url)) {
            return
        }
        var ls: List<DownloadListener>? = getObserver(url)
        if (ls == null || ls.size == 0) {
            if (ls == null) {
                ls = ArrayList()
            }
            Log.d("DM", "DownloadManager add task:" + l.hashCode() + "|" + l.releaseCode + "|" + identify)
            ls += l
            addObserver(url, ls)
            val worker = createDownloadWorker(url,
                    getDownloadPluginDir(context), identify, suffix, md5)
            if (downloadPluginExecutor == null) {
                downloadPluginExecutor = Executors.newFixedThreadPool(2) { runnable ->
                    val thread = Thread(runnable,
                            "ADM download-worker")
                    thread.priority = Thread.MAX_PRIORITY - 1
                    thread
                }
            }
            // 相同的url，跑一个任务
            downloadPluginExecutor!!.submit(worker)

        } else {
            Log.d("DM", "DownloadManager add task+" + l.hashCode() + "|" + l.releaseCode + "|" + identify)
            ls += l
            addObserver(url, ls)
            Log.d("DM", "ls size|" + ls.size)
        }
    }

    @Synchronized
    fun downloadVideo(context: Context, payload: URLConnectionPayload, listener: DownloadListener?) {
        init(context.applicationContext)
        initHandler("init thread handler at download request")
        if (payload == null || payload.url == null || payload.url.length == 0) {
            return
        }
        var l: DownloadListener? = listener
        if (l == null) {
            l = object : DownloadAdapter() {
            }
        }
        if (l.checkInitialized(context, payload.url)) {
            return
        }
        var jumpTheQueue = payload.jumpTheQueue
        var ls: List<DownloadListener>? = getObserver(payload.url)
        if (ls == null || ls.size == 0) {
            if (ls == null) {
                ls = ArrayList()
            }
            Log.d("DM", "DownloadManager add task:" + l.hashCode() + "|" + l.releaseCode + "|" + payload.key)
            ls += l
            addObserver(payload.url, ls)
            notifyDownloadWaiting(payload.key, payload.url)

            if (downloadVideoExecutor == null) {
                downloadVideoExecutor = ThreadPoolExecutor(2, 2,
                        0L, TimeUnit.MILLISECONDS,
                        LinkedBlockingDeque<Runnable>(),
                        { runnable ->
                            val thread = Thread(runnable,
                                    "ADM download-worker")
                            thread.priority = Thread.MAX_PRIORITY - 1
                            thread
                        })
            }
            var worker = createDownloadWorker(payload)
            if (!jumpTheQueue) {
                // 相同的url，跑一个任务
                var future = downloadVideoExecutor!!.submit(worker)
                worker.setFuture(future)
                addTasks(payload.url, worker)
                Log.d("DM", "0.add task|" + worker.key)
            } else {
                // 这种情况是插队情况
                var activeCount = downloadVideoExecutor!!.activeCount
                if (activeCount < 2) {
                    // 队列未满，直接submit
                    var future = downloadVideoExecutor!!.submit(worker)
                    worker.setFuture(future)
                    addTasks(payload.url, worker)
                    Log.d("DM", "1.add task|" + worker.key)
                } else {
                    // 1.task在队列中，找到task，remove，然后置于第一位
                    var taskQueue = downloadVideoExecutor!!.queue as LinkedBlockingDeque
                    if (taskQueue.contains(worker)) {
                        taskQueue.remove(worker)
                    }
                    taskQueue.addFirst(worker)
                    addTasks(payload.url, worker)
                    Log.d("DM", "2.add task|" + worker.key)
                    // 2.暂停一个正在执行的任务，找最先执行的
                    var oldWorker = getOlderRunningTask()
                    if (oldWorker != null && !TextUtils.isEmpty(oldWorker.key)) {
                        cancelTaskByKey(oldWorker.key, InterruptReason.SOME_ONE_JUMP_THE_QUEUE)
                    }
                }
            }

        } else {
            Log.d("DM", "DownloadManager add task+" + l.hashCode() + "|" + l.releaseCode + "|" + payload.key)
            if (jumpTheQueue) {
                Log.d("DM", payload.key + " has jump the queue!")
            }
            ls += l
            addObserver(payload.url, ls)
            Log.d("DM", "ls size|" + ls.size)
            notifyDownloadWaiting(payload.key, payload.url)
        }
    }

    protected fun createDownloadWorker(url: String?, cachePath: String?, fileName: String?, suffix: String?, md5: String?): DownloadWorker {
        return URLConnectionWorker(url, cachePath, fileName, suffix, md5)
    }

    protected fun createDownloadWorker(payload: URLConnectionPayload): DownloadWorker {
        return VideoDownloadWorker(payload)
    }

    override fun notifyDownloadWaiting(key:String, url: String) {
        initHandler("init thread handler at download waiting")
        mHandler?.post {
            val ls = getObserver(url)
            if (ls != null) {
                // 防止类型回调中崩溃影响其他类型回调
                try {
                    for (l in ls) {
                        l.onDownloadWaiting(url)
                    }
                } catch (t: Throwable) {
                }
            }
            try {
                for (o in globalObservers.values) {
                    o.notifyDownloadWaiting(key, url)
                }
            } catch (t: Throwable) {
            }
        }
    }

    override fun notifyDownloadStart(key:String, url: String) {
        initHandler("init thread handler at download start")
        mHandler?.post {
            val ls = getObserver(url)
            if (ls != null) {
                // 防止类型回调中崩溃影响其他类型回调
                try {
                    for (l in ls) {
                        l.onDownloadStart(url)
                    }
                } catch (t: Throwable) {
                }
            }
            try {
                for (o in globalObservers.values) {
                    o.notifyDownloadStart(key, url)
                }
            } catch (t: Throwable) {
            }
        }
    }

    override fun notifyDownloadFailure(key:String, url: String, error: DownloadError?, message: String?) {
        initHandler("init thread handler at download failure")
        mHandler?.post {
            val ls = getObserver(url)
            if (ls != null) {
                // 防止类型回调中崩溃影响其他类型回调
                try {
                    for (l in ls) {
                        l.onDownloadFailure(url, error, message)
                    }
                } catch (t: Throwable) {
                }
            }
            try {
                for (o in globalObservers.values) {
                    o.notifyDownloadFailure(key, url, error, message)
                }
            } catch (t: Throwable) {
            }
        }
    }

    override fun notifyDownloadCancel(key:String, url: String) {
        initHandler("init thread handler at download cancel")
        mHandler?.post {
            val ls = getObserver(url)
            if (ls != null) {
                // 防止类型回调中崩溃影响其他类型回调
                try {
                    for (l in ls) {
                        l.onDownloadCancel(url)
                    }
                } catch (t: Throwable) {
                }
            }
            try {
                for (o in globalObservers.values) {
                    o.notifyDownloadCancel(key, url)
                }
            } catch (t: Throwable) {
            }
        }
    }

    override fun notifyDownloadProgress(key:String, url: String, progress: Int, completeSize: Long, totalSize: Long) {
        initHandler("init thread handler at download progress")
        mHandler?.post {
            val ls = getObserver(url)
            if (ls != null) {
                // 防止类型回调中崩溃影响其他类型回调
                try {
                    for (l in ls) {
                        l.onDownloadProgress(url, progress, completeSize, totalSize)
                    }
                } catch (t: Throwable) {
                }
            }
            try {
                for (o in globalObservers.values) {
                    o.notifyDownloadProgress(key, url, progress, completeSize, totalSize)
                }
            } catch (t: Throwable) {
            }
        }
    }

    override fun notifyDownloadSuccess(key:String, url: String, path: String) {
        initHandler("init thread handler at download success")
        mHandler?.post {
            val ls = getObserver(url)
            if (ls != null) {
                // 防止类型回调中崩溃影响其他类型回调
                Log.d("DM", "download success ls size:" + ls.size)
                try {
                    for (l in ls) {
                        l.onDownloadSuccess(url, path)
                    }
                } catch (t: Throwable) {
                }
            }
            try {
                for (o in globalObservers.values) {
                    o.notifyDownloadSuccess(key, url, path)
                }
            } catch (t: Throwable) {
            }
        }
    }

    override fun notifyDownloadClear(key:String, success: Boolean, url: String,
                                     path: String?, error: DownloadError?) {
        initHandler("init thread handler at download clear")
        mHandler?.post {
            var ls = removeObserver(url)
            if (ls != null) {
                // 防止类型回调中崩溃影响其他类型回调
                try {
                    for (l in ls) {
                        Log.d("DM", "download complete ls size:" + ls.size)
                        l.onDownloadClear(success, url, path, error)
                        Log.d("DM", "DownloadManager runned task " + l.hashCode() + "|" + l.releaseCode)
                    }
                } catch (t: Throwable) {
                }
            }
            try {
                for (o in globalObservers.values) {
                    o.notifyDownloadClear(key, success, url, path, error)
                }
            } catch (t: Throwable) {
            }
            try {
                removeTasks(url, key)
            } catch (t: Throwable) {
            }
        }
    }
}