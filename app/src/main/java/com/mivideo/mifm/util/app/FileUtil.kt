package com.mivideo.mifm.util.app

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import java.io.File

/**
 * Created by xingchang on 16/12/22.
 */
fun fileExists(context: Context, mediaUrl: String): Boolean {
    if (!TextUtils.isEmpty(mediaUrl)) {
        val uri = Uri.parse(mediaUrl)
        var filename: String?
        if (!TextUtils.isEmpty(uri.getScheme()) && uri.scheme.equals("content")) {
            filename = getRealFilePathFromContentUri(context, uri)
        } else {
            filename = mediaUrl.replace("file://", "")
        }
        if (!TextUtils.isEmpty(filename)) {
            val file = File(filename)
            return file.exists()
        }
    }
    return false
}

fun getRealFilePathFromContentUri(context: Context, contentUri: Uri): String? {
    try {
        var columns: Array<String> = Array(1, { MediaStore.Video.Media.DATA })
        val cursor = context.contentResolver.query(contentUri, columns, null, null, null) ?: return null
        var index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        cursor.moveToFirst()
        var result = cursor.getString(index)
        cursor.close()
        return result
    } catch (e: Exception) {
        return null
    }
}

fun delDir(dir: File?) {
    try {
        if (dir == null) {
            delAllFiles(dir!!)
            dir!!.delete()
        }
    } catch (e: Exception) {

    }
}

fun delDir(dirFullName: String) {
    delDir(File(dirFullName))
}

fun delAllFiles(dir: File) {
    if (!dir.exists() || !dir.isDirectory)
        return
    val dirFullName = dir.absolutePath
    var fileList = dir.list()
    var tempFile: File? = null
    for (sub in fileList) {
        if (dirFullName.endsWith(File.separator)) {
            tempFile = File(dirFullName + sub)
        } else {
            tempFile = File(dirFullName + File.separator + sub)
        }

        if (tempFile.isFile) {
            tempFile.delete()
        }

        if (tempFile.isDirectory) {
            delAllFiles(dirFullName + "/" + sub)
            delDir(dirFullName + "/" + sub)
        }
    }
}

fun delAllFiles(dirFullName: String) {
    delAllFiles(File(dirFullName))
}
