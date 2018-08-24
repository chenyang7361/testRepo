package com.mivideo.mifm.player

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import timber.log.Timber
import java.io.IOException


/**
 * MediaPlayer封装
 * Created by xingchang on 16/11/30.
 */
class VideoDailyPlayer(private var mPlayer: IMediaPlayer, private val context: Context) :
        AbstractMediaPlayer(), IMediaPlayer by mPlayer {
    private var currentState = STATE_IDLE
    override var videoWidth: Int = 0
    override var videoHeight: Int = 0
    private var mDuration: Int = 0

    private var audioFocusHelper: AudioFocusHelper = AudioFocusHelper.get(context)

    var uri: Uri? = null

    val isReleased: Boolean
        get() {
            return currentState == STATE_IDLE &&
                    uri == null
        }

    override val currentPosition: Int
        get() {
            if (isInPlaybackState) {
                return mPlayer.currentPosition
            }
            return 0
        }

    override val duration: Int
        get() {
            if (isInPlaybackState) {
                if (mDuration <= 0) {
                    return mPlayer.duration
                } else {
                    mDuration = 0
                }
            }
            return 0
        }

    val isIdle: Boolean
        get() = currentState == STATE_IDLE

    val isPlayingState: Boolean
        get() = currentState == STATE_PLAYING

    val isPrepared: Boolean
        get() = isInPlaybackState

    val isPreparing: Boolean
        get() = currentState == STATE_PREPARING

    override val isPlaying: Boolean
        get() = isInPlaybackState && mPlayer.isPlaying

    val isInPlaybackState: Boolean
        get() = currentState != STATE_ERROR &&
                currentState != STATE_IDLE &&
                currentState != STATE_PREPARING

    @Throws(IOException::class,
            IllegalArgumentException::class,
            SecurityException::class,
            IllegalStateException::class)

    override fun setDataSource(dataSource: DataSource) {
        Timber.d("setDataSource: " + dataSource.toString())
        if (dataSource.uri == null || TextUtils.isEmpty(dataSource.uri!!.toString())) {
            handleError(0, 0)
            return
        }
        uri = dataSource.uri
        mPlayer.setDataSource(dataSource)
        videoHeight = 0
        videoWidth = videoHeight
        mDuration = 0
    }

    private fun handleError(what: Int, extra: Int): Boolean {
        currentState = STATE_ERROR
        val listener = mMediaPlayerListener
        listener?.onError(what, extra)
        return true
    }

    override fun setMediaPlayerListener(listener: MediaPlayerListener) {
        val tempListener = object : MediaPlayerListener {
            override fun onPrepared() {
                Timber.d("onPrepared.")
                currentState = STATE_PREPARED
                listener.onPrepared()
                mDuration = 0
            }

            override fun onCompletion() {
                Timber.d("onCompletion.")
                currentState = STATE_PLAYBACK_COMPLETED
                listener.onCompletion()
            }

            override fun onBufferingUpdate(percent: Int) {
                Timber.d("onBufferingUpdate : " + percent)
                listener.onBufferingUpdate(percent)
            }

            override fun onSeekComplete() {
                Timber.d("seekComplete.")
                listener.onSeekComplete()
            }

            override fun onVideoSizeChanged(width: Int, height: Int) {
                Timber.d("videoSize: width = $width, height = $height")
                videoWidth = width
                videoHeight = height
                listener.onVideoSizeChanged(width, height)
            }

            override fun onError(what: Int, extra: Int): Boolean {
                Timber.d("onError : what = $what, extra = $extra")
                currentState = STATE_ERROR
                listener.onError(what, extra)
                return true
            }

            override fun onInfo(what: Int, extra: Int): Boolean {
                Timber.d("onInfo : what = $what, extra = $extra")
                listener.onInfo(what, extra)
                return false
            }
        }
        super.setMediaPlayerListener(tempListener)
        mPlayer.setMediaPlayerListener(tempListener)
    }

    @Throws(IllegalStateException::class)
    override fun seekTo(ms: Int) {
        Timber.d("seekTo " + ms)
        if (isInPlaybackState) {
            Timber.d("do seekTo " + ms)
            mPlayer.seekTo(ms)
            //				mSeekWhenPrepared = 0;
        } else {
            //				mSeekWhenPrepared = ms;
        }
    }

    @Throws(IOException::class, IllegalStateException::class)
    override fun prepare() {
        Timber.d("prepare")
        currentState = STATE_PREPARING
        mPlayer.prepare()
        currentState = STATE_PREPARED
    }

    @Throws(IllegalStateException::class)
    override fun prepareAsync() {
        Timber.d("prepareAsync")
        mPlayer.prepareAsync()
        currentState = STATE_PREPARING
    }

    override fun release() {
        Timber.d("release")
        mPlayer.release()
        currentState = STATE_IDLE
        //			mIsPrepared = false;
        uri = null
    }

    @Throws(IllegalStateException::class)
    override fun pause() {
        if (isInPlaybackState) {
            mPlayer.pause()
            currentState = STATE_PAUSED
        }
    }

    override fun reset() {
        audioFocusHelper.releaseAudioFocus()
        currentState = STATE_IDLE
        //			mIsPrepared = false;
        mPlayer.reset()
    }

    @Throws(IllegalStateException::class)
    override fun start() {
        Timber.d("start ")
        if (!audioFocusHelper.requestAudioFocus()) return
        if (isInPlaybackState) {
            mPlayer.start()
            currentState = STATE_PLAYING
        }
    }

    @Throws(IllegalStateException::class)
    override fun stop() {
        audioFocusHelper.releaseAudioFocus()
        currentState = STATE_IDLE
        mPlayer.stop()
    }

    @Throws(IllegalStateException::class)
    override fun speedUp(speed: Float) {
        mPlayer.speedUp(speed)
    }

    companion object {
        val STATE_ERROR = -1
        val STATE_IDLE = 0
        val STATE_PREPARING = 1
        val STATE_PREPARED = 2
        val STATE_PLAYING = 3
        val STATE_PAUSED = 4
        val STATE_PLAYBACK_COMPLETED = 5

        /**
         * 获取MediaPlayer对象
         */
        fun create(context: Context): VideoDailyPlayer {
            val player: VideoDailyPlayer
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            player = VideoDailyPlayer(ExoMediaPlayer(context), context)
//            } else {
//                player = VideoDailyPlayer(OriginMediaPlayer(context), context)
//            }
            return player
        }
    }
}