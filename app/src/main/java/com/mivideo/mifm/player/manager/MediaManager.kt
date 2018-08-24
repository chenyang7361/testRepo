package com.mivideo.mifm.player.manager

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.support.v4.app.NotificationCompat
import android.widget.RemoteViews
import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork
import com.mivideo.mifm.MainActivity
import com.mivideo.mifm.MainApp
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.jsondata.AlbumInfo
import com.mivideo.mifm.data.models.jsondata.HistoryItem
import com.mivideo.mifm.data.models.jsondata.PassageItem
import com.mivideo.mifm.events.MediaCompleteEvent
import com.mivideo.mifm.events.MediaPreparedEvent
import com.mivideo.mifm.player.AudioController
import com.mivideo.mifm.util.app.postEvent
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MediaManager private constructor(val controller: DefaultController) : AudioController by controller {

    companion object {
        private lateinit var app: MainApp
        private var sInstance: MediaManager? = null
        fun getInstance(): MediaManager {
            if (sInstance == null) {
                val controller = DefaultController(app.applicationContext)
                sInstance = MediaManager(controller)
            }
            return sInstance!!
        }

        fun initContext(context: MainApp) {
            app = context
        }
    }

    private var observeNetworkSubscription: Subscription? = null

    private var contentManager: ContentManager? = null
    private var notifyManager: NotificationManager
    private var remoteViews: RemoteViews


    var history = false
    internal var TAG = "MIM"

    init {
        initIfNeeded()
        notifyManager = app.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        remoteViews = RemoteViews(app.applicationContext.packageName, R.layout.notify_view)
        observeNetworkChange(app.applicationContext)
    }

    private fun initIfNeeded() {
        controller.initIfNeed()
        if (contentManager == null) {
            contentManager = ContentManager(app.applicationContext)
            contentManager!!.attachLoadDataListener(object : ContentManager.LoadDataListener {
                override fun onNextMoreLoaded() {
                    controller.updateLastNextStatus()
                }

                override fun onLastMoreLoaded() {
                    controller.updateLastNextStatus()
                }

                override fun historyFirstPageLoading() {

                }

                override fun historyFirstPageLoaded() {
                    controller.updateLastNextStatus()
                }
            })
        }
    }


    private fun observeNetworkChange(context: Context) {
        observeNetworkSubscription = ReactiveNetwork
                .observeNetworkConnectivity(context.applicationContext)
                .subscribeOn(Schedulers.io())
                .debounce(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { connectivity ->
                    if (connectivity.state == NetworkInfo.State.CONNECTED) {
                        if (connectivity.type == ConnectivityManager.TYPE_WIFI) {
                            controller.onWifiConnected()
                        } else if (connectivity.type == ConnectivityManager.TYPE_MOBILE) {
                            controller.onMobileNetConnected()
                        }
                    } else if (connectivity.state == NetworkInfo.State.DISCONNECTED) {
                        controller.onNetworkDisConnected()
                    }
                }
    }

    fun playAlbum(albumInfo: AlbumInfo, position: Int, list: List<PassageItem>, page: Int) {
        history = false
        initIfNeeded()
        if (checkPositionValid(position, list) && !(contentManager!!.isCurrentItem(albumInfo, position, list) && controller.isPlaying())) {
            var lastPosition = -1
            if (controller.isPlaying()) {
                lastPosition = controller.getCurrentPosition()
            }
            contentManager?.playAlbum(albumInfo, position, list, page)
            playNewUrl(list[position].url)
            contentManager?.saveToHistory(lastPosition)
        }
    }

    private fun checkPositionValid(position: Int, list: List<PassageItem>): Boolean {
        return position >= 0 && position < list.size
    }


    fun playHistory(item: HistoryItem) {
        history = true
        initIfNeeded()
        var lastPosition = -1
        if (controller.isPlaying()) {
            lastPosition = controller.getCurrentPosition()
        }
        contentManager?.playHistory(item)
        playNewUrl(item.item?.url)
        contentManager?.saveToHistory(lastPosition)
        controller.historyPosition = item.lastPosition
    }

    override fun playNext() {
        history = false
        if (controller.playerPreparing()) {
            return
        }
        val next = contentManager?.playNextContent()
        if (next != null) {
            playNewUrl(next.url)
            controller.updateLastNextStatus()
        }
    }

    override fun playLast() {
        history = false
        val last = contentManager?.playLastContent()
        if (last != null) {
            playNewUrl(last.url)
            controller.updateLastNextStatus()
        }
    }


    private fun updateNotification() {

        val builder = NotificationCompat.Builder(app.applicationContext, "mifm")
        val intentMain = Intent(app.applicationContext, MainActivity::class.java)
        val pendingMain = PendingIntent.getActivity(app.applicationContext, 0, intentMain, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.notice, pendingMain)

        val intentLast = Intent()
        intentLast.action = AudioReceiver.ACTION_LAST
        val pendingLast = PendingIntent.getBroadcast(app.applicationContext, 1, intentLast, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.widget_prev, pendingLast)

        if (controller.isPlaying()) {
            val playorpause = Intent()
            playorpause.action = AudioReceiver.ACTION_PAUSE
            val pendingPause = PendingIntent.getBroadcast(app.applicationContext, 2,
                    playorpause, PendingIntent.FLAG_UPDATE_CURRENT)
            remoteViews.setOnClickPendingIntent(R.id.widget_play, pendingPause)
        }
        if (!controller.isPlaying()) {
            val playorpause = Intent()
            playorpause.action = AudioReceiver.ACTION_PLAY
            val pendingPlay = PendingIntent.getBroadcast(app.applicationContext, 3,
                    playorpause, PendingIntent.FLAG_UPDATE_CURRENT)
            remoteViews.setOnClickPendingIntent(R.id.widget_play, pendingPlay)
        }

        val intentNext = Intent()
        intentNext.action = AudioReceiver.ACTION_NEXT
        val pendingNext = PendingIntent.getBroadcast(app.applicationContext, 4, intentNext, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.widget_next, pendingNext)

        remoteViews.setTextViewText(R.id.widget_title, DataContainer.item!!.name)


        val notify = builder.setCustomContentView(remoteViews)
                .setCustomBigContentView(remoteViews)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_launcher)
                .build()
        notify.flags = Notification.FLAG_ONGOING_EVENT

        notifyManager.notify(100, notify)

    }

    private fun playNewUrl(url: String?) {
        controller.initIfNeed()
//        Log.d(TAG, "playNewUrl|" + url)
        controller.innerReset()
        val uri = Uri.parse(url)
        controller.prepareMediaPlayer(uri)
        updateNotification()
        controller.updateLastNextStatus()
    }

    /**
     * 播放器开始播放
     */
    override fun start() {
        if (!DataContainer.hasData()) {
            return
        }
        if (controller.playerNotInit()) {
            return
        }

        if (controller.isPlaying()) {
            controller.innerStart()
            postEvent(MediaPreparedEvent(DataContainer.album!!.id))
        } else {
            if (DataContainer.hasData()) {
                playNewUrl(DataContainer.item!!.url)
            }
        }
    }

    override fun pause() {
        if (controller.playerNotInit()) {
            return
        }
        controller.innerPause()
        contentManager?.saveToHistory(controller.getCurrentPosition())
        postEvent(MediaCompleteEvent())
    }

    fun timerStop(min: Int) {

    }

}