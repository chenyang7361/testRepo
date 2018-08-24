package com.mivideo.mifm.download.support

import android.content.Context
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import java.io.File
import java.lang.reflect.Method
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import android.os.storage.StorageManager
import java.lang.reflect.InvocationTargetException
import android.os.StatFs
import android.support.v4.content.ContextCompat
import com.mivideo.mifm.network.commonurl.NetworkParams

class DownloadUtil {
    companion object {

        fun getVideoDirPath(context: Context): String {
            return getDirPathByName(context, "audio")
        }

        protected fun getDirPathByName(context: Context, name: String): String {
            var path: String? = null
            var dir: File? = null

            path = getExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, name)
            dir = File(path!!)
            if (dir != null && dir.exists() && dir.isDirectory) {
                path = dir.absolutePath
                return path
            }
            return ""
        }

        protected fun getExternalFilesDir(context: Context, environment: String, childOfEnvironment: String): String? {
            var dir: File? = null
            var path: String? = null
            try {
                dir = context.getExternalFilesDir(environment)
                if (dir == null) {
                    path = context.filesDir.toString() + File.separator + childOfEnvironment
                } else {
                    path = dir.absolutePath + File.separator + childOfEnvironment
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }

            ensureDirectoryExistAndAccessable(path)
            return path
        }

        protected fun ensureDirectoryExistAndAccessable(path: String?): Boolean {
            if (path == null || path.length == 0) {
                return false
            }
            val target = File(path)
            if (!target.exists()) {
                target.mkdirs()
                chmodCompatV23(target, 493)
                return true
            } else if (!target.isDirectory) {
                return false
            }

            chmodCompatV23(target, 493)
            return true
        }

        protected fun chmodCompatV23(path: File, mode: Int): Int {
            return if (Build.VERSION.SDK_INT > 23) {
                0
            } else chmod(path, mode)
        }

        protected fun chmod(path: File, mode: Int): Int {
            val fileUtils: Class<*>
            var setPermissions: Method? = null
            try {
                fileUtils = Class.forName("android.os.FileUtils")
                setPermissions = fileUtils.getMethod("setPermissions",
                        String::class.java, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                return setPermissions!!.invoke(null, path.absolutePath,
                        mode, -1, -1) as Int
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return 0
        }

        fun verifyBinaryFile(filePath: String?, targetMd5: String?): Boolean {
            if (filePath == null || filePath.length == 0) {
                return false
            }
            val srcFile = File(filePath)
            if (!srcFile.exists() || !srcFile.isFile) {
                return false
            }
            if (targetMd5 == null || targetMd5.length == 0) {
                return true
            }

            val result = NetworkParams.getMD5(srcFile)
            return targetMd5 == result
        }

        protected fun replaceFileInSameDir(dir: String?, fromFileName: String?, toFileName: String?): Boolean {
            var result = false
            if (TextUtils.isEmpty(dir) || TextUtils.isEmpty(fromFileName) || TextUtils.isEmpty(toFileName)) {
                return result
            }
            var fromSize = -1L
            var toSize = -1L
            var from = File(dir, fromFileName)
            if (from.exists() && from.isFile) {
                fromSize = from.length()
            }
            var to = File(dir, toFileName)
            if (to.exists() && to.isFile) {
                to.delete()
            }
            fileRename(File(dir, fromFileName), File(dir, toFileName))
            to = File(dir, toFileName)
            if (to.exists() && to.isFile) {
                toSize = to.length()
                if (toSize == fromSize && toSize > 0) {
                    from = File(dir, fromFileName)
                    if (from.exists() && from.isFile) {
                        from.delete()
                    }
                    Log.d("DM", "rename|" + fromFileName + "|" + toFileName)
                    return true
                }
            }
            to = File(dir, toFileName)
            if (to.exists() && to.isFile) {
                to.delete()
            }
            try {
                to.createNewFile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            fileChannelCopy(File(dir, fromFileName), File(dir, toFileName))
            from = File(dir, fromFileName)
            if (from.exists() && from.isFile) {
                from.delete()
            }
            to = File(dir, toFileName)
            if (to.exists() && to.isFile) {
                toSize = to.length()
                if (toSize == fromSize && toSize > 0) {
                    from = File(dir, fromFileName)
                    if (from.exists() && from.isFile) {
                        from.delete()
                    }
                    Log.d("DM", "copy|" + fromFileName + "|" + toFileName)
                    return true
                }
            }
            return false
        }

        protected fun fileRename(from: File?, to: File?): Boolean {
            var result = false
            if (from == null || to == null) {
                return result
            }
            try {
                result = from.renameTo(to)
            } catch (e: Exception) {
                e.printStackTrace()
                result = false
            }
            return result
        }

        protected fun fileChannelCopy(from: File?, to: File?) {
            var fi: FileInputStream? = null
            var fo: FileOutputStream? = null
            var `in`: FileChannel? = null
            var out: FileChannel? = null
            try {
                fi = FileInputStream(from)
                fo = FileOutputStream(to)
                `in` = fi!!.getChannel()
                out = fo!!.getChannel()
                `in`!!.transferTo(0, `in`!!.size(), out)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    fi!!.close()
                    `in`!!.close()
                    fo!!.close()
                    out!!.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun isSdcardMounted(ctx: Context): Boolean {
            if (android.os.Build.VERSION.SDK_INT < 16) {
                return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
            } else {
                val storagges = listAvaliableStorage(ctx) as ArrayList<StorageInfo>
                for (info in storagges) {
                    if (Environment.MEDIA_MOUNTED == info.state && info.isRemoveable) {
                        return true
                    }
                }
                return false
            }
        }

        fun getSDAvailableSize(con: Context): Long {
            val path = getSDCardPath(con) ?: return 0
            val stat = StatFs(path)
            var blockSize: Long = 0
            var availableBlocks: Long = 0
            if (Build.VERSION.SDK_INT >= 18) {
                blockSize = stat.blockSizeLong
                availableBlocks = stat.availableBlocksLong
            } else {
                blockSize = stat.blockSize.toLong()
                availableBlocks = stat.availableBlocks.toLong()
            }
            return blockSize * availableBlocks
        }

        fun getSDCardPath(con: Context): String? {
            val storagges = listAvaliableStorage(con) as ArrayList<StorageInfo>
            for (info in storagges) {
                if (Environment.MEDIA_MOUNTED == info.state && info.isRemoveable) {
                    return info.path
                }
            }
            return null
        }

        class StorageInfo(var path: String) {
            var state: String? = null
            var isRemoveable: Boolean = false

            val isMounted: Boolean
                get() = "mounted" == state
        }

        fun listAvaliableStorage(context: Context): List<StorageInfo> {
            val storagges = ArrayList<StorageInfo>()
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            try {
                val paramClasses = arrayOf<Class<*>>()
                val getVolumeList = StorageManager::class.java.getMethod("getVolumeList", *paramClasses)
                getVolumeList.isAccessible = true
                val params = arrayOf<Any>()
                val invokes = getVolumeList.invoke(storageManager, *params) as Array<Any>
                if (invokes != null) {
                    var info: StorageInfo? = null
                    for (i in invokes.indices) {
                        val obj = invokes[i]
                        val getPath = obj.javaClass.getMethod("getPath", *arrayOfNulls(0))
                        val path = getPath.invoke(obj, *arrayOfNulls(0)) as String
                        info = StorageInfo(path)
                        val file = File(info!!.path)
                        if (file.exists() && file.isDirectory() && file.canWrite()) {
                            val isRemovable = obj.javaClass.getMethod("isRemovable", *arrayOfNulls(0))
                            var state: String? = null
                            try {
                                val getVolumeState = StorageManager::class.java.getMethod("getVolumeState", String::class.java)
                                state = getVolumeState.invoke(storageManager, info!!.path) as String
                                info!!.state = state
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            if (info!!.isMounted) {
                                info!!.isRemoveable = (isRemovable.invoke(obj, *arrayOfNulls(0)) as Boolean)
                                storagges.add(info)
                            }
                        }
                    }
                }
            } catch (e1: NoSuchMethodException) {
                e1.printStackTrace()
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
            storagges.trimToSize()
            return storagges
        }

        fun getInternalStorageFile(context: Context): File {
            return context.applicationContext.filesDir
        }

        fun getExternalStorageFiles(context: Context): List<File> {
            var list = ArrayList<File>()
            var files = ContextCompat.getExternalFilesDirs(context.applicationContext, null)
            for (file in files) {
                if (file != null) {
                    list.add(file)
                }
            }
            return list
        }

        fun getInternalStorageAvailableSize(context: Context): Long {
            var availableSizeInBytes = 0L
            try {
                var internalStorageFile = getInternalStorageFile(context)
                if (Build.VERSION.SDK_INT <= 8) {
                    val stat = StatFs(internalStorageFile.getPath())
                    availableSizeInBytes = (stat.blockSize * stat.availableBlocks).toLong()
                } else if (Build.VERSION.SDK_INT >= 9) {
                    availableSizeInBytes = internalStorageFile.getFreeSpace()
                    if (availableSizeInBytes <= 0) {
                        if (Build.VERSION.SDK_INT >= 18) {
                            availableSizeInBytes = StatFs(internalStorageFile.getPath()).getAvailableBytes()
                        }
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            return availableSizeInBytes
        }

        fun getExternalStorageAvailableSize(context: Context): Long {
            return getExternalStorageAvailableSize(context, 0)
        }

        fun getExternalStorageAvailableSize(context: Context, index: Int): Long {
            var availableSizeInBytes = 0L
            try {
                var externalStorageFile = getExternalStorageFiles(context)[index]
                if (Build.VERSION.SDK_INT <= 8) {
                    val stat = StatFs(externalStorageFile.getPath())
                    availableSizeInBytes = (stat.blockSize * stat.availableBlocks).toLong()
                } else if (Build.VERSION.SDK_INT >= 9) {
                    availableSizeInBytes = externalStorageFile.getFreeSpace()
                    if (availableSizeInBytes <= 0) {
                        if (Build.VERSION.SDK_INT >= 18) {
                            availableSizeInBytes = StatFs(externalStorageFile.getPath()).getAvailableBytes()
                        }
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            return availableSizeInBytes
        }

        fun formatByteFileSize(context: Context, length: Long): String {
            return android.text.format.Formatter.formatShortFileSize(context, length)
        }
    }
}