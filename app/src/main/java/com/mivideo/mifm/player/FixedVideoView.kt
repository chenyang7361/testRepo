package com.mivideo.mifm.player

import android.content.Context
import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import timber.log.Timber
import java.net.URLDecoder

/**
 * 类似于Android原生中的VideoView空间，本控件通过TextureView来实现
 *
 * Created by xingchang on 16/11/7.
 * @author LiYan
 */
class FixedVideoView : FrameLayout, IVideoView, IMediaPlayer {
    /**
     * 倍速播放功能
     */
    override fun speedUp(speed: Float) {
    }

    var mContext: Context
    private lateinit var mMediaPlayer: VideoDailyPlayer

    private var mTextureView: MyTextureView? = null
    private var mSurface: Surface? = null
    private var mSurfaceTexture: SurfaceTexture? = null

    private var mMediaPlayerListener: MediaPlayerListener? = null
    private var mPlayOffset = -1
    private var mCurrentState = VideoDailyPlayer.STATE_IDLE
    private var mSeekWhenPrepared: Int = 0
    private var mUri: Uri? = null

    private var mVideoPrepared = false
    private var mDelayOnPrepared = false
    private var mForceFullScreen = false
    private var mWaittingSurface = false


    override val canPause: Boolean
        get() = mMediaPlayer.canPause

    override val canSeekBackward: Boolean
        get() = mMediaPlayer.canSeekBackward

    override val canSeekForward: Boolean
        get() = mMediaPlayer.canSeekForward

    override val canBuffering: Boolean
        get() = mMediaPlayer.canBuffering

    override val currentPosition: Int
        get() = mMediaPlayer.currentPosition

    override val duration: Int
        get() = mMediaPlayer.duration

    override val videoWidth: Int
        get() = mMediaPlayer.videoWidth

    override val videoHeight: Int
        get() = mMediaPlayer.videoHeight

    override val isPlaying: Boolean
        get() = mMediaPlayer.isPlaying

    override val bufferPercentage: Int
        get() = mMediaPlayer.bufferPercentage

    override var loopPlayVideo: Boolean
        get() = mMediaPlayer.loopPlayVideo
        set(value) {
            mMediaPlayer.loopPlayVideo = value
        }

    @JvmOverloads
    constructor(context: Context,
                attrs: AttributeSet? = null,
                defStyle: Int = 0) : super(context, attrs, defStyle) {
        mContext = context
        mMediaPlayer = VideoDailyPlayer.create(mContext)
        initMediaPlayer(mMediaPlayer)
        init()
    }

    private fun initMediaPlayer(mediaPlayer: VideoDailyPlayer) {
        val listener = object : MediaPlayerListener {
            override fun onPrepared() {
                mVideoPrepared = true
                if (mSurface == null) {
                    mDelayOnPrepared = true
                    return
                }
                handleOnPrepared()
            }

            override fun onCompletion() {
                mMediaPlayerListener?.onCompletion()
            }

            override fun onBufferingUpdate(percent: Int) {
                mMediaPlayerListener?.onBufferingUpdate(percent)
            }

            override fun onSeekComplete() {
                mMediaPlayerListener?.onSeekComplete()
            }

            override fun onVideoSizeChanged(width: Int, height: Int) {
                mTextureView?.adaptVideoSize(width, height)
                mMediaPlayerListener?.onVideoSizeChanged(width, height)
            }

            override fun onError(what: Int, extra: Int): Boolean {
                return mMediaPlayerListener?.onError(what, extra) ?: false

            }

            override fun onInfo(what: Int, extra: Int): Boolean {
                mMediaPlayerListener?.onInfo(what, extra)
                return false
            }

        }
        mediaPlayer.setMediaPlayerListener(listener)
        if (mSurface != null) {
            mediaPlayer.setSurface(mSurface!!)
        }
    }

    override fun setDisplay(surfaceHolder: SurfaceHolder) {
        mMediaPlayer.setDisplay(surfaceHolder)
    }

    override fun setSurface(surface: Surface?) {
        mMediaPlayer.setSurface(surface)
    }

    override fun setVolume(leftVolume: Float, rightVolume: Float) {
        mMediaPlayer.setVolume(leftVolume, rightVolume)
    }

    override fun prepareAsync() {
        mMediaPlayer.prepareAsync()
    }

    override fun prepare() {
        mMediaPlayer.prepare()
    }

    override fun setScreenOnWhilePlaying(screenOn: Boolean) {
        mMediaPlayer.setScreenOnWhilePlaying(screenOn)
    }

    private val mSurfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            Timber.i("onSurfaceTextureDestroyed: surface -> $surface")
            return mSurfaceTexture == null
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int,
                                               height: Int) {
            Timber.i("onSurfaceTextureAvailable: surface -> $surface , width -> $width" +
                    "height -> $height")
            try {
                if (mSurfaceTexture != null &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    Timber.i("textureView.setSurfaceTexture")
                    mTextureView?.surfaceTexture = mSurfaceTexture
                } else {
                    mSurfaceTexture = surface
                    if (mSurface == null) {
                        mSurface = Surface(surface)
                    }
                    mMediaPlayer.setSurface(mSurface!!)
                    keepScreenOn = true
                    if (mWaittingSurface) {
                        mMediaPlayer.start()
                    }
                    mWaittingSurface = false
                }
            } catch (e: Exception) {
                Timber.e(e)
            }

        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
            Timber.i("onSurfaceTextureSizeChanged: surface -> $surface ,width -> $width" +
                    "height -> $height ")
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        }
    }

    fun init() {
        mTextureView = MyTextureView(mContext)
        mTextureView!!.surfaceTextureListener = mSurfaceTextureListener

        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        params.gravity = Gravity.CENTER
        addView(mTextureView, params)
    }

    override fun stop() {
        mMediaPlayer.stop()
    }

    private fun handleOnPrepared() {
        mDelayOnPrepared = false
        mMediaPlayerListener?.onPrepared()
        if (mSeekWhenPrepared != 0) {
            seekTo(mSeekWhenPrepared)
        }
        mSeekWhenPrepared = 0
        if (mTextureView != null) {
            mTextureView!!.requestLayout()
            mTextureView!!.invalidate()
        }
    }

    private fun prepareMediaPlayer(player: VideoDailyPlayer, uri: Uri) {
        try {
            val dataSource = DataSource.builder()
                    .uri(uri)
                    .build(mContext)
            player.setDataSource(dataSource)
            player.prepareAsync()
        } catch (e: Exception) {
            Timber.e(e)
        }

    }

    private fun isSurfaceCreated(): Boolean {
        return mSurface != null
    }

    override fun setDataSource(dataSource: DataSource) {
        mPlayOffset = dataSource.offset
        val fullPath = dataSource.uri?.toString() ?: ""
        var urlEscape = fullPath.replace("\\/", "/")
        setVideoUri(Uri.parse(urlEscape), dataSource.headers)
    }

    private fun setVideoUri(uri: Uri?, headers: Map<String, String>?) {
        if (uri == null) {
            throw IllegalArgumentException("uri can not be empty.")
        }
        mUri = getRealUri(uri)
        mMediaPlayer.reset()
        prepareMediaPlayer(mMediaPlayer, uri)
        requestLayout()
        invalidate()
    }

    private fun getRealUri(uri: Uri?): Uri? {
        if (uri != null) {
            var url = uri.toString()
            if (url != null && url.startsWith("file:///content")) {
                url = url.substring(15, url.length)
                try {
                    url = URLDecoder.decode(url, "utf-8")
                    val pos = url.indexOf("/")
                    if (pos >= 0) {
                        return Uri.parse("content://" + url.substring(pos + 1, url.length))
                    }
                } catch (e: Exception) {
                }

            }
        }
        return uri
    }

    override fun start() {
        if (mCurrentState == VideoDailyPlayer.STATE_IDLE) {
            addTextureView()
        }
        mCurrentState = VideoDailyPlayer.STATE_PLAYING
        if (isSurfaceCreated()) {
            mMediaPlayer.start()
        } else {
            mWaittingSurface = true
        }
        keepScreenOn = true
    }

    private fun addTextureView() {
        Timber.i("addTextureView.....")
        if (mTextureView == null) return

        try {
            removeView(mTextureView)
            val params = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER)
            addView(mTextureView, params)
        } catch (e: Exception) {
            Timber.d(e)
        }

    }

    override fun pause() {
        mCurrentState = VideoDailyPlayer.STATE_PAUSED
        if (isSurfaceCreated()) {
            mMediaPlayer.pause()
        }
        keepScreenOn = false
    }

    override fun seekTo(pos: Int) {
        if (mMediaPlayer.isInPlaybackState) {
            mMediaPlayer.seekTo(pos)
            mSeekWhenPrepared = 0
        } else {
            mSeekWhenPrepared = pos
        }
    }

    override fun reset() {
        Timber.i("reset....player reset")
        try {
            mMediaPlayer.setSurface(null)
            mSurfaceTexture?.release()
            mSurface?.release()
            mSurface = null
            mSurfaceTexture = null
        } catch (e: Exception) {
            Timber.e(e)
        }
        mMediaPlayer.reset()
    }

    override fun release() {
        Timber.i("release.....")
        if (!mMediaPlayer.isReleased) {
            mMediaPlayer.release()

            clearSurface()

            mSeekWhenPrepared = 0
            mCurrentState = VideoDailyPlayer.STATE_IDLE
            mDelayOnPrepared = false
            mVideoPrepared = false
        }

    }


    private fun clearSurface() {
        if (mSurface != null) {
            mSurface?.release()
            mSurface = null
        }

        removeView(mTextureView)
        if (mSurfaceTexture != null) {
            mSurfaceTexture?.release()
            mSurfaceTexture = null
        }
    }

    override fun asView(): View {
        return this
    }

    override fun requestVideoLayout() {
        if (mTextureView != null) {
            mTextureView!!.requestLayout()
        }
    }

    override fun setForceFullScreen(forceFullScreen: Boolean) {
        mForceFullScreen = forceFullScreen
        if (mTextureView != null) {
            mTextureView!!.requestLayout()
        }
    }

    override fun continuePlay(position: Int) {
        if (mPlayOffset >= 0) {
            return
        }
        if (position > 5000) {
            seekTo(position)
        }
    }

    override fun setMediaPlayerListener(listener: MediaPlayerListener) {
        this.mMediaPlayerListener = listener
    }

    override fun errorCodeMapper(): ErrorCodeMapper {
        return mMediaPlayer.errorCodeMapper()
    }

}