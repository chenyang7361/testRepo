package com.mivideo.mifm.player.manager

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.mivideo.mifm.events.MediaCompleteEvent
import com.mivideo.mifm.events.MediaPreparedEvent
import com.mivideo.mifm.player.*
import com.mivideo.mifm.ui.widget.player.PlayControllerView
import com.mivideo.mifm.util.app.postEvent
import com.mivideo.mifm.util.bus.changeMillisecondTime2Str
import timber.log.Timber

/**
 * Created by Jiwei Yuan on 18-8-20.
 */
class DefaultController(val mContext: Context) : AudioControllerAdapter() {

    var uiListener: AudioControllerView? = null

    internal var mediaPlayer: VideoDailyPlayer? = null

    private val mHandler = Handler(Looper.getMainLooper())
    private var mUpdateProgressRunner: Runnable? = null
    private var mCachedSeekPosition = -1

    private var mIsSeeking: Boolean = false

    private val mSeekRunner = Runnable {
        if (mediaPlayer == null) {
            return@Runnable
        }
        if (mCachedSeekPosition >= 0) {
            mediaPlayer?.seekTo(mCachedSeekPosition)
        }
        mCachedSeekPosition = -1
    }

    /**
     * 添加AudioControllerView
     */
    override fun attachControllerView(controllerView: AudioControllerView) {
        uiListener = controllerView
        initMediaPlayer()
        uiListener!!.updateCurrentAudio(DataContainer.getAudioInfo())
        if (mediaPlayer != null && mediaPlayer!!.isPlayingState) {
            uiListener!!.showPlayBtn(true)
        }
        updateLastNextStatus()
        setProgress()
    }

    /**
     * 移除AudioControllerView
     */
    override fun dettachControllerView() {
        uiListener = null
    }

    /**
     * 当前视频是否正在暂停
     */
    override fun isPaused(): Boolean {
        return !isPlaying()
    }

    /**
     * 当前视频是否正在播放
     */
    override fun isPlaying(): Boolean {
        return if (mediaPlayer == null) false else mediaPlayer!!.isPlayingState
    }

    /**
     * 开始seek操作的回调
     */
    override fun onSeekStart() {
        mIsSeeking = true
    }

    /**
     * 正在seek操作回调
     */
    override fun onSeeking(progress: Int, fromUser: Boolean) {
        if (!fromUser) {
            return
        }
        val duration = mediaPlayer!!.duration
        val newPosition = duration.toLong() * progress.toLong() / 1000L
        sendSeekMessage(newPosition.toInt())
        val text = changeMillisecondTime2Str(newPosition.toInt())
        uiListener?.renderPlayTimeText(text)
        stopUpdateProgress()
    }

    /**
     * seek操作结束回调
     */
    override fun onSeekEnd() {
        mSeekRunner.run()
        if (mediaPlayer != null && !mediaPlayer!!.isPlayingState) {
            innerStart()
        }
        mIsSeeking = false
        startUpdateProgress()
    }

    private fun sendSeekMessage(seekTo: Int) {
        if (seekTo <= mediaPlayer!!.duration) {
            mCachedSeekPosition = seekTo
        }
    }

    /**
     *开始刷新进度条
     */
    private fun startUpdateProgress() {
        if (mUpdateProgressRunner == null) {
            mUpdateProgressRunner = Runnable {
                mHandler.removeCallbacks(mUpdateProgressRunner)
                if (mediaPlayer != null) {
                    if (!mIsSeeking && mediaPlayer!!.isPlaying) {
                        setProgress()
                        val pos = mediaPlayer?.currentPosition ?: 0
                        mHandler.postDelayed(mUpdateProgressRunner, (1000 - pos % 1000).toLong())
                    } else {
                        mHandler.postDelayed(mUpdateProgressRunner, 1000)
                    }
                }
            }
        }
        mHandler.removeCallbacks(mUpdateProgressRunner)
        mHandler.post(mUpdateProgressRunner)
    }

    /**
     *停止刷新进度条
     */
    private fun stopUpdateProgress() {
        mHandler.removeCallbacks(mUpdateProgressRunner)
    }

    private fun setProgress() {
        if (mediaPlayer == null) {
            setProgress(0, 0)
        }
        val position = mediaPlayer!!.currentPosition
        val duration = mediaPlayer!!.duration

        if (duration < 0) {
            setProgress(0, 0)
        }
        setProgress(position, duration)
    }

    private fun setProgress(position: Int, duration: Int) {
        if (duration <= 0) {
            return
        }
        var seekBarProgress = 0
        var pos: Long
        if (duration > 0) {
            // use long to avoid overflow
            pos = PlayControllerView.SEEK_BAR_MAX_PROGRESS.toLong() * position / duration
            seekBarProgress = pos.toInt()
        }
        val durationText = changeMillisecondTime2Str(duration)
        val positionText = changeMillisecondTime2Str(position)
        val bufferingProgress = getPlayerBufferingPercent()
        uiListener?.renderSeekLayout(seekBarProgress, bufferingProgress, durationText, positionText)
    }

    private fun getPlayerBufferingPercent(): Int {
        return (mediaPlayer?.bufferPercentage ?: 0) * 10
    }

    fun updateLastNextStatus() {
        if (!DataContainer.hasNext()) {
            uiListener?.updateNextBtnStatus(false)
        } else {
            uiListener?.updateNextBtnStatus(true)
        }
        if (!DataContainer.hasLast()) {
            uiListener?.updateLastBtnStatus(false)
        } else {
            uiListener?.updateLastBtnStatus(true)
        }
    }

    fun initIfNeed() {
        if (mediaPlayer == null) {
            mediaPlayer = VideoDailyPlayer.create(mContext.applicationContext)
            initMediaPlayer()
//            Log.d(TAG, "initMediaPlayer|" + mediaPlayer + "|" + System.currentTimeMillis())
        }

    }

    var historyPosition: Int = 0

    private fun initMediaPlayer() {
        val listener = object : MediaPlayerListener {
            override fun onPrepared() {
                uiListener?.hideLoadingView()
                mediaPlayer?.start()
                mHandler.postDelayed({
                    uiListener?.showPlayBtn(true)
                    postEvent(MediaPreparedEvent(DataContainer.album!!.id))
                }, 20 * 18)
                startUpdateProgress()
                if (historyPosition != 0) {
                    seekTo(historyPosition)
                }
            }

            override fun onCompletion() {
                uiListener?.showPlayBtn(false)
                postEvent(MediaCompleteEvent())
                stopUpdateProgress()
                if (shouldAutoPlayNext()) {
                    MediaManager.getInstance().playNext()
                }
            }

            override fun onBufferingUpdate(percent: Int) {
//                uiListener?.onBufferingUpdate(percent)
            }

            override fun onSeekComplete() {
                uiListener?.showPlayBtn(true)
                startUpdateProgress()
            }

            override fun onVideoSizeChanged(width: Int, height: Int) {
            }

            override fun onError(what: Int, extra: Int): Boolean {
//                return uiListener?.onError(what, extra) ?: false
                return false
            }

            override fun onInfo(what: Int, extra: Int): Boolean {
//                return uiListener?.onInfo(what, extra) ?: false
                return false
            }

        }
        mediaPlayer?.setMediaPlayerListener(listener)
    }

    fun prepareMediaPlayer(uri: Uri) {
        try {
            val dataSource = DataSource.builder()
                    .uri(uri)
                    .build(mContext)
            mediaPlayer?.setDataSource(dataSource)
            mediaPlayer?.prepareAsync()
            uiListener?.updateCurrentAudio(DataContainer.getAudioInfo())
            setProgress(0, 0)
//            Log.d(TAG, "prepareMediaPlayer|prepareAsync|" + uri)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun shouldAutoPlayNext(): Boolean {
        //TODO
        return true
    }


    fun innerStart() {
        mediaPlayer!!.start()
    }

    fun innerPause() {
        mediaPlayer!!.pause()
    }


    fun innerStop() {
        mediaPlayer!!.stop()
    }


    fun innerReset() {
        mediaPlayer!!.reset()
    }


    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    override fun speedUp(speed: Float) {
        mediaPlayer?.speedUp(speed)
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer!!.currentPosition
    }

    fun playerPreparing(): Boolean {
        return mediaPlayer == null || mediaPlayer!!.isPreparing
    }

    fun playerNotInit(): Boolean {
        return mediaPlayer == null
    }
}