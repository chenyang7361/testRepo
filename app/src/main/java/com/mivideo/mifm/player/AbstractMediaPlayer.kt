package com.mivideo.mifm.player

import android.net.Uri
import java.io.IOException

/**
 * 播放器抽象类
 * @author LiYan
 */
abstract class AbstractMediaPlayer : IMediaPlayer {
    protected var mMediaPlayerListener: MediaPlayerListener? = null
    private var dataSource: DataSource? = null

    override val canPause: Boolean
        get() {
            val uri = dataSource?.uri
            if (uri != null && "rtsp" == uri.scheme && duration <= 0) {
                return false
            }
            return true
        }

    override val canSeekBackward: Boolean
        get() {
            val uri = dataSource?.uri
            if (uri != null && "rtsp" == uri.scheme && duration <= 0) {
                return false
            }
            return true
        }

    override val canSeekForward: Boolean
        get() {
            val uri = dataSource?.uri
            if (uri != null && "rtsp" == uri.scheme && duration <= 0) {
                return false
            }
            return true
        }

    override val canBuffering: Boolean
        get() {
            val uri = dataSource?.uri
            if (uri != null) {
                if (isOnlineVideo(uri) || isSmbVideo(uri)) {
                    return true
                }
            }
            return false
        }

    @Throws(IOException::class, IllegalArgumentException::class,
            SecurityException::class, IllegalStateException::class)
    override fun setDataSource(dataSource: DataSource) {
        this.dataSource = dataSource
    }


    override fun setMediaPlayerListener(listener: MediaPlayerListener) {
        this.mMediaPlayerListener = listener
    }
}

fun isOnlineVideo(uri: Uri): Boolean {
    if (uri == null) {
        return false
    }
    var scheme = uri.scheme
    if (scheme != null && (scheme.equals("http") || scheme.equals("https") || scheme.equals("rtsp"))) {
        return true
    }
    var path = uri.toString()
    if (path != null && path.contains("app_smb")) {
        return true
    }
    return false
}

fun isSmbVideo(uri: Uri): Boolean {
    if (uri == null) {
        return false
    }

    var path = uri.toString()
    if (path != null && path.contains("app_smb")) {
        return true
    } else {
        return false
    }
}