package com.mivideo.mifm.player

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import timber.log.Timber


/**
 * Created by xingchang on 16/11/30.
 */
class ExoMediaPlayer(private val mContext: Context) : AbstractMediaPlayer(),
        ExoPlayer.EventListener, SimpleExoPlayer.VideoListener {

    private val TAG = "ExoMediaPlayer"

    private val mMediaPlayer: SimpleExoPlayer
    private var mFirstPrepare = true
    private var mLastPlaybackState = ExoPlayer.STATE_IDLE
    private var mLastPlayWhenReady = false

    override var loopPlayVideo: Boolean
        get() = mMediaPlayer.repeatMode == REPEAT_MODE_ONE
        set(value) {
            if (value) {
                mMediaPlayer.repeatMode = REPEAT_MODE_ONE
            } else {
                mMediaPlayer.repeatMode = PlaybackStateCompat.REPEAT_MODE_NONE
            }
        }

    override val bufferPercentage: Int
        get() = mMediaPlayer.bufferedPercentage

    init {
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)

        mMediaPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector)
//            simpleExoPlayerView!!.player = player
        mMediaPlayer.playWhenReady = true

//        if (mSurface != null) {
//            player!!.setVideoSurface(mSurface!!)
//        }
        mMediaPlayer.addListener(this)
        mMediaPlayer.setVideoListener(this)
    }

    private fun setVideoUri(uri: Uri?) {
        if (uri == null) return
        val videoSource = buildMediaSource(uri)
        mMediaPlayer.playWhenReady = true
        mMediaPlayer.prepare(videoSource)
        mFirstPrepare = true
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val type = Util.inferContentType(uri)
        val bandwidthMeter = DefaultBandwidthMeter()
        val dataSourceFactory = DefaultDataSourceFactory(mContext,
                Util.getUserAgent(mContext, "kuaiest"), bandwidthMeter)
        when (type) {
            C.TYPE_HLS -> {
                return HlsMediaSource(uri, dataSourceFactory, null, null)
            }

            else -> {
                val extractorsFactory = DefaultExtractorsFactory()
                return ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory,
                        null, null)
            }
        }
    }

    override val currentPosition: Int
        get() {
            return mMediaPlayer.currentPosition.toInt()
        }

    override val duration: Int
        get() = mMediaPlayer.duration.toInt()

    override val videoHeight: Int
        get() = 0

    override val videoWidth: Int
        get() = 0

    override val isPlaying: Boolean
        get() = mMediaPlayer.playWhenReady

    override fun pause() {
        Log.d(TAG, "pause")
        mMediaPlayer.playWhenReady = false
    }

    override fun prepare() {
        mFirstPrepare = true
    }

    override fun prepareAsync() {
        mFirstPrepare = true
    }

    override fun release() {
        Log.d(TAG, "release : " + mMediaPlayer)
        mMediaPlayer.removeListener(this)
        mMediaPlayer.clearVideoListener(this)
        mMediaPlayer.release()
    }

    override fun reset() {
        Log.d(TAG, "reset")
//        mMediaPlayer.release()
        mFirstPrepare = true
        mLastPlaybackState = ExoPlayer.STATE_IDLE
        mLastPlayWhenReady = false
    }

    override fun seekTo(ms: Int) {
        mMediaPlayer.seekTo(ms.toLong())
    }

    override fun setScreenOnWhilePlaying(screenOn: Boolean) {
        //TODO
    }

    override fun setDataSource(dataSource: DataSource) {
        super.setDataSource(dataSource)
        if (dataSource.uri != null) {
            setVideoUri(dataSource.uri)
        } else {
            throw IllegalArgumentException("Error DataSource")
        }
    }

    override fun setDisplay(holder: SurfaceHolder) {
        mMediaPlayer.setVideoSurfaceHolder(holder)
    }

    override fun setSurface(surface: Surface?) {
        mMediaPlayer.setVideoSurface(surface)
    }

    override fun setVolume(leftVolume: Float, rightVolume: Float) {
        mMediaPlayer.volume = rightVolume
    }

    override fun start() {
        Log.d(TAG, "start")
        mMediaPlayer.playWhenReady = true
    }

    override fun stop() {
        Log.d(TAG, "stop")
        mMediaPlayer.stop()
    }

    /**
     * 倍速播放功能
     */
    override fun speedUp(speed: Float) {
        val playbackParameters = PlaybackParameters(speed, 1.0f)
        mMediaPlayer.playbackParameters = playbackParameters
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
        Log.d(TAG, "onPlaybackParametersChanged")
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
        Log.d(TAG, "onTracksChanged")
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        Log.d(TAG, "onPlayerError")
        Timber.e(error?.cause)
        mMediaPlayerListener?.onError(error!!.type, error!!.rendererIndex)
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        Log.d(TAG, "onLoadingChanged: " + isLoading)

    }

    override fun onPositionDiscontinuity(reason: Int) {
        Log.d(TAG, "onPositionDiscontinuity reason:$reason")
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
        Log.d(TAG, "onTimelineChanged")
    }

    override fun onSeekProcessed() {
        Log.d(TAG, "onSeekProcessed")
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        Log.d(TAG, "onRepeatModeChanged")
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        Log.d(TAG, "onShuffleModeEnabledChanged")
    }


    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        Log.d(TAG, "onPlayerStateChanged: " + playWhenReady + "; " + playbackState)
        if (mLastPlaybackState == playbackState && mLastPlayWhenReady == playWhenReady) return
        if (playbackState == ExoPlayer.STATE_ENDED && mLastPlaybackState == ExoPlayer.STATE_ENDED) return
        mLastPlaybackState = playbackState
        mLastPlayWhenReady = playWhenReady
        when (playbackState) {
            ExoPlayer.STATE_IDLE -> {

            }
            ExoPlayer.STATE_BUFFERING -> {
                if (!mFirstPrepare)
                    mMediaPlayerListener?.onInfo(MediaPlayer.MEDIA_INFO_BUFFERING_START, 0)
            }
            ExoPlayer.STATE_READY -> {
                if (mFirstPrepare) {
                    mFirstPrepare = false
                    mMediaPlayerListener?.onPrepared()
                } else {
                    mMediaPlayerListener?.onInfo(MediaPlayer.MEDIA_INFO_BUFFERING_END, 0)

                }

            }
            ExoPlayer.STATE_ENDED -> {
                mMediaPlayerListener?.onCompletion()
            }
        }
    }

    override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
        mMediaPlayerListener?.onVideoSizeChanged(width, height)
    }

    override fun onRenderedFirstFrame() {
        mMediaPlayerListener?.onInfo(MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START, 0)
    }

    override fun errorCodeMapper(): ErrorCodeMapper {
        return object : ErrorCodeMapper {
            override fun map(what: Int, extra: Int): ErrorCode {
                if (what == ExoPlaybackException.TYPE_SOURCE) {
                    return ErrorCode.SOURCE_GET_ERROR
                } else if (what == ExoPlaybackException.TYPE_RENDERER) {
                    return ErrorCode.RENDER_ERROR
                } else if (what == ExoPlaybackException.TYPE_UNEXPECTED) {
                    return ErrorCode.UNKNOWN_ERROR
                }
                return ErrorCode.UNKNOWN_ERROR
            }

        }
    }

}