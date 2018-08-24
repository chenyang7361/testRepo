package com.mivideo.mifm.util

import android.net.Uri

/**
 * Created by xingchang on 16/11/30.
 */
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

var CLARITY_NORMAL = 1

