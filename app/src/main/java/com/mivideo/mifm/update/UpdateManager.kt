package com.mivideo.mifm.update

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.BuildConfig
import com.mivideo.mifm.data.models.jsondata.common.CommonUpdate
import com.mivideo.mifm.data.repositories.MainRepository
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.ui.widget.CustomDialog
import com.mivideo.mifm.ui.widget.CustomDialog.Companion.PROGRESS_MODE
import com.mivideo.mifm.util.MJson
import com.mivideo.mifm.util.app.showToast
import com.xiaomi.market.sdk.UpdateStatus
import com.xiaomi.market.sdk.XiaomiUpdateAgent
import org.jetbrains.anko.defaultSharedPreferences
import org.json.JSONObject
import rx.Observable
import timber.log.Timber

/**
 * 应用更新管理器
 *
 *
 * 理想的更新策略：
 * 1、DONE: 每次进入应用时检查更新
 * 2、TODO: 在合适的时机提示用户当前应用需要更新（A.间隔 xx 分钟，B.每日首次启动等等）
 * 3、TODO: 在通知中心（NotificationCenter）中记录更新事件
 * 4、TODO：
 */
class UpdateManager(private val mAppContext: Context) : KodeinInjected {

    override val injector = KodeinInjector()

    private var mDownloadObserver: DownLoadChangeObserver? = null

    private val mainRepository: MainRepository by instance()

    private var dialog: CustomDialog? = null

    init {
        inject(mAppContext.appKodein())
    }

    fun checkUpdateByUser(activity: Activity) {
        if (BuildConfig.CONFIG_USED_CUSTOM_UPDATE_DIALOG) {
            loadUpdateApk(activity, false)
        } else {
            try {
                // TODO: 暂时屏蔽应用商店升级
                XiaomiUpdateAgent.setCheckUpdateOnlyWifi(false)
                XiaomiUpdateAgent.setUpdateAutoPopup(false)
                XiaomiUpdateAgent.setUpdateListener { updateStatus, updateInfo ->
                    Timber.i(TAG, "status: " + updateStatus)

                    when (updateStatus) {
                        UpdateStatus.STATUS_UPDATE -> {
                            // 有更新， UpdateResponse为本次更新的详细信息
                            // 其中包含更新信息，下载地址，MD5校验信息等，可自行处理下载安装
                            // 如果希望 SDK继续接管下载安装事宜，可调用
                            Toast.makeText(mAppContext, "开始下载最新包...", Toast.LENGTH_SHORT).show()
                            XiaomiUpdateAgent.arrange()
                        }
                        UpdateStatus.STATUS_NO_UPDATE ->
                            // 无更新， UpdateResponse为null
                            Toast.makeText(mAppContext, "当前已是最新版本", Toast.LENGTH_SHORT).show()
                        UpdateStatus.STATUS_NO_WIFI -> {
                        }
                        UpdateStatus.STATUS_NO_NET ->
                            // 没有网络， UpdateResponse为null
                            Toast.makeText(mAppContext, "请打开网络", Toast.LENGTH_SHORT).show()
                        UpdateStatus.STATUS_FAILED -> {
                        }
                        UpdateStatus.STATUS_LOCAL_APP_FAILED -> {
                        }
                        else -> {
                        }
                    }// 设置了只在WiFi下更新，且WiFi不可用时， UpdateResponse为null
                    // 检查更新与服务器通讯失败，可稍后再试， UpdateResponse为null
                    // 检查更新获取本地安装应用信息失败， UpdateResponse为null
                }
                // true为使用沙盒测试
                XiaomiUpdateAgent.update(activity, BuildConfig.DEBUG)
            } catch (e: SecurityException) {
                // TODO: 权限不足，无法检查更新，需要在通知中心中创建通知项，在 APP 中通知用户
                Timber.i(e, "Cannot get permission")
            }
        }
    }

    fun checkUpdate(activity: Activity) {
        try {
            XiaomiUpdateAgent.setCheckUpdateOnlyWifi(true)
            XiaomiUpdateAgent.setUpdateAutoPopup(BuildConfig.CONFIG_DEFAULT_UPDATE_DIALOG)
            XiaomiUpdateAgent.setUpdateListener(null)
            // true为使用沙盒测试
            XiaomiUpdateAgent.update(activity, BuildConfig.DEBUG)

            if (BuildConfig.CONFIG_USED_CUSTOM_UPDATE_DIALOG) {
                Observable.create(Observable.OnSubscribe<Boolean> { subscribe ->
                    val sp = activity.defaultSharedPreferences
                    val currentTime = System.currentTimeMillis()
                    val lastTime = sp.getLong(SP_KEY_LAST_CANCEL_TIME, -1)
                    if ((currentTime - lastTime) > TARGET_TIME_SLOT) {
                        subscribe.onNext(true)
                    } else {
                        subscribe.onNext(false)
                    }
                    subscribe.onCompleted()
                })
                        .compose(asyncSchedulers())
                        .subscribe({ outTime ->
                            if (outTime) {
                                loadUpdateApk(activity, true)
                            }
                        }, {

                        })
            }
        } catch (e: SecurityException) {
            Timber.i(e, "Cannot get permission")
        }
    }

    private fun loadUpdateApk(context: Context, auto: Boolean) {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        Log.d("cy_mi", "versionCode--->" + packageInfo.versionCode + "---nnn-->" + context.packageName + "------>" + packageInfo.versionName)
        mainRepository.appUpdateInfo(packageInfo.versionCode, context.packageName)
                .compose(asyncSchedulers())
                .subscribe({
                    Log.d("cy_mi", "result--->" + it.toString())
//                    if (t != null) {
//                        if (t.has("data")) {
//                            var data = t.optJSONObject("data")
//                            if (data.has("result") && "success".equals(data.optString("result"))) {
//                                if (data.has("info")) {
//                                    var info = data.optJSONObject("info")
//                                    var update = MJson.getInstance().fromJson(info.toString(), CommonUpdate::class.java)
//                                    Observable.create(Observable.OnSubscribe<Boolean> { subscribe ->
//                                        val sp = context.defaultSharedPreferences
//                                        val newCode = sp.getString("updateVersionCode", "")
//                                        val downloadId = sp.getLong(newCode, -1)
//                                        var downloading = false
//                                        if (downloadId > 0) {
//                                            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//                                            val query = DownloadManager.Query()
//                                            query.setFilterById(downloadId)
//                                            val downloadCursor = downloadManager.query(query)
//                                            var status = -1
//                                            try {
//                                                if (downloadCursor != null && downloadCursor!!.moveToFirst()) {
//                                                    status = downloadCursor!!.getInt(downloadCursor!!
//                                                            .getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
//                                                }
//                                                downloading = !(status != DownloadManager.STATUS_PAUSED && status != DownloadManager.STATUS_PENDING
//                                                        && status != DownloadManager.STATUS_RUNNING)
//                                            } finally {
//                                                if (downloadCursor != null) {
//                                                    downloadCursor!!.close()
//                                                }
//                                            }
//                                        }
//                                        subscribe.onNext(downloading)
//                                        subscribe.onCompleted()
//                                    })
//                                            .compose(asyncSchedulers())
//                                            .subscribe({ downloading ->
//                                                if (!downloading) {
//                                                    showDialog(context, update, auto)
//                                                } else {
//                                                    if (!auto) {
//                                                        showToast(context, "正在下载最新包...")
//                                                    }
//                                                }
//                                            }, {})
//                                    return@subscribe
//                                }
//                            }
//                        }
//                    }
                    if (!auto)
                        Toast.makeText(mAppContext, "当前已是最新版本", Toast.LENGTH_SHORT).show()
                }, {
                    if (!auto)
                        Toast.makeText(mAppContext, "当前已是最新版本", Toast.LENGTH_SHORT).show()
                })
    }

//    fun download(context: Context, update: CommonUpdate, registObs: Boolean, dialog: CustomDialog) {
//        Observable.create(Observable.OnSubscribe<Long> { subscribe ->
//            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//            val request = DownloadManager.Request(Uri.parse(update.apkUrl))
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
//            request.setTitle("小米快视频")
//            request.setDescription("正在下载...")
//            request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS,
//                    update._id)
//            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
//            request.allowScanningByMediaScanner()
//            request.setVisibleInDownloadsUi(true)
//            request.setMimeType("application/vnd.android.package-archive")
//
//            val enqueue = downloadManager.enqueue(request)
//            val sp = context.defaultSharedPreferences
//            val editor = sp.edit()
//            editor.putString("updateVersionCode", "" + update.versionCode)
//            editor.putLong("" + update.versionCode, enqueue)
//            editor.apply()
//            subscribe.onNext(enqueue)
//            subscribe.onCompleted()
//        })
//                .compose(asyncSchedulers())
//                .subscribe({
//                    if (mDownloadObserver != null)
//                        context.contentResolver.unregisterContentObserver(mDownloadObserver)
//                    if (it > 0 && registObs) {
//                        mDownloadObserver = DownLoadChangeObserver(Handler(), context, it, dialog)
//                        context.contentResolver.registerContentObserver(Uri.parse("content://downloads/"), true,
//                                mDownloadObserver)
//                    }
//                }, {
//
//                })
//    }
//
//    private fun showDialog(context: Context, update: CommonUpdate, auto: Boolean) {
//        val builder = CustomDialog.Builder(context)
//        builder.setTitle(update.title)
//        builder.setMessage(update.content)
//        builder.setSize("" + update.apkSize)
//        builder.setVersionName(update.versionName)
//        if (update.mandatory == 0) {
//            builder.setCanceledOnTouchOutside(true)
//            builder.setPositiveButtonClickListener(View.OnClickListener {
//                showToast(context, "正在下载...")
//                download(context, update, false, dialog!!)
//                dialog?.dismiss()
//                dialog = null
//            })
//            builder.setNegativeButtonClickListener(View.OnClickListener {
//                Observable.create(Observable.OnSubscribe<Int> { subscribe ->
//                    if (auto) {
//                        val sp = context.defaultSharedPreferences
//                        val editor = sp.edit()
//                        val time = System.currentTimeMillis()
//                        editor.putLong(SP_KEY_LAST_CANCEL_TIME, time)
//                        editor.apply()
//                    }
//                    subscribe.onNext(0)
//                    subscribe.onCompleted()
//                })
//                        .compose(asyncSchedulers())
//                        .subscribe({
//                            dialog?.dismiss()
//                            dialog = null
//                        }, {
//
//                        })
//            })
//        } else {
//            builder.setCanceledOnTouchOutside(false)
//            builder.setPositiveButtonClickListener(View.OnClickListener {
//                download(context, update, true, dialog!!)
//                dialog?.changeUIMode(PROGRESS_MODE)
//            })
//        }
//        dialog = builder.create()
//        dialog!!.show()
//    }

    fun updateDialogIsShowing(): Boolean = dialog?.isShowing ?: false

    companion object {
        private val TAG = UpdateManager::class.java.simpleName

        private val TARGET_TIME_SLOT = 1

        private val SP_KEY_LAST_CANCEL_TIME = "updateCancelTime"

        private var sInstance: UpdateManager? = null

        private val objs = Any()

        fun getInstance(context: Context): UpdateManager {
            if (sInstance == null) {
                synchronized(objs) {
                    if (sInstance == null) {
                        sInstance = UpdateManager(context.applicationContext)
                    }
                }
            }
            return sInstance!!
        }
    }

    class DownLoadChangeObserver(handler: Handler, private val context: Context, private val downloadId: Long, private val dialog: CustomDialog) : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            var bytesAndStatus = getBytesAndStatus(downloadId)
            var currentSize = bytesAndStatus[0];//当前大小
            var totalSize = bytesAndStatus[1];//总大小
            var status = bytesAndStatus[2];//下载状态
            dialog.setProgress(currentSize, totalSize)
        }

        fun getBytesAndStatus(downloadId: Long): IntArray {
            val bytesAndStatus = IntArray(3)
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query()
            query.setFilterById(downloadId)
            val c = downloadManager.query(query)
            try {
                if (c != null && c!!.moveToFirst()) {
                    bytesAndStatus[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    bytesAndStatus[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    bytesAndStatus[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
                }

            } finally {
                if (c != null) {
                    c!!.close()
                }
            }
            return bytesAndStatus
        }
    }

    fun destory(context: Context) {
        if (mDownloadObserver != null)
            context.contentResolver.unregisterContentObserver(mDownloadObserver)
    }
}