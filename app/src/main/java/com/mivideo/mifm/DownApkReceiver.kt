package com.mivideo.mifm

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.util.Log
import org.jetbrains.anko.defaultSharedPreferences
import java.io.File

/**
 * Created by xingchang on 17/7/31.
 */
class DownApkReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            val downloadApkId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            val sp = context.defaultSharedPreferences
            val newCode = sp.getString("updateVersionCode", "")
            if (TextUtils.isEmpty(newCode)) {
                return
            }
            val saveApkId = sp.getLong(newCode, -1)
            if (downloadApkId == saveApkId) {
                checkDownloadStatus(context, downloadApkId)
            }
        }
    }

    private fun checkDownloadStatus(context: Context, downloadId: Long) {
        var downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query()
        query.setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))

            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> installApk(context, downloadId)
                DownloadManager.STATUS_FAILED -> Log.d("DownApkReceiver", "下载失败.....")
                DownloadManager.STATUS_RUNNING -> Log.d("DownApkReceiver", "正在下载.....")
                else -> {
                }
            }
        }
        cursor.close()
    }

    fun queryDownloadedApk(context: Context, downloadId: Long): File? {
        var targetApkFile: File? = null
        val downloader = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        if (downloadId != -1L) {
            val query = DownloadManager.Query()
            query.setFilterById(downloadId)
            query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL)
            val cur = downloader.query(query)
            if (cur != null) {
                if (cur.moveToFirst()) {
                    val uriString = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    if (!TextUtils.isEmpty(uriString)) {
                        targetApkFile = File(Uri.parse(uriString).path)
                    }
                }
                cur.close()
            }
        }
        return targetApkFile
    }

    private fun installApk(context: Context, downloadId: Long) {
//        val actionIntent = Intent(Intent.ACTION_INSTALL_PACKAGE)
        val intent = Intent(Intent.ACTION_VIEW)
        val apkFile = queryDownloadedApk(context, downloadId) ?: return
        var uri: Uri? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            uri = FileProvider.getUriForFile(context.applicationContext, context.packageName + ".fileprovider", apkFile)
            context.grantUriPermission("com.google.android.packageinstaller",
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            uri = Uri.fromFile(apkFile)
        }

        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        val resolveInfoList = context.packageManager
                .queryIntentActivities(intent, 0)
        var targetPkgName: String? = null
        if (resolveInfoList != null) {
            for (resolveInfo in resolveInfoList) {
                val activityInfo = resolveInfo.activityInfo
                if (activityInfo.enabled && activityInfo.exported) {
                    targetPkgName = activityInfo.packageName
                    break
                }
            }
        }
        if (TextUtils.isEmpty(targetPkgName)) {
            Log.e("DownApkReceiver", "no activity found to install apk")
            return
        }

        intent.`package` = targetPkgName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.grantUriPermission(targetPkgName,
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}