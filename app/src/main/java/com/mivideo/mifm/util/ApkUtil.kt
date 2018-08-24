package com.mivideo.mifm.util

import android.content.Context
import android.content.pm.PackageManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader

/**
 *
 * Apk操作相关方法工具类，包括判断系统是否安装了指定apk等
 *
 * Created by yamlee on 23/05/2017.
 * @author LiYan
 */
class ApkUtil {
    companion object {

        const val WX_PACKAGE = "com.tencent.mm"
        const val QQ_PACKAGE = "com.tencent.mobileqq"
        const val WB_PACKAGE = "com.sina.weibo"
        const val TIM_PACKAGE = "com.tencent.tim"
        const val YOUKU_PACKAGE = "com.youku.phone"

        /**
         * 判断某个应用是否安装
         * @param context
         * @param packageName
         * @return
         */
        fun isInstalled(context: Context, packageName: String): Boolean {
            var hasPackage = true
            try {
                context.packageManager.getPackageInfo(packageName, PackageManager.GET_GIDS)
            } catch (e: Exception) {
                // 抛出找不到的异常，说明该程序已经被卸载
                hasPackage = false
            }
            return hasPackage
        }

        /**
         * 通过PackageManager.getInstalledPackage方法获取应用列表判断
         * 此方法在某些有 “读取应用列表”权限控制的手机可能会获取应用列表失败
         */
        fun isInstalledByPmList(context: Context, packageName: String): Boolean {
            val packageManager = context.packageManager
            // 获取所有已安装程序的包信息
            val info = packageManager.getInstalledPackages(0)

            return info.indices.any { info[it].packageName.equals(packageName, ignoreCase = true) }
        }


        /**
         * 通过adb pm list命令获取安装的应用列表，再从其中找出对应的应用包名是否安装
         * 此种方法有IO操作，非官方sdk提供，不到万不得以，请慎用
         */
        fun isInstalledByAdb(packageName: String): Boolean {
            try {
                val process = Runtime.getRuntime().exec("pm list package -e")
                val bis = BufferedReader(InputStreamReader(process.inputStream) as Reader?)
                var line = bis.readLine()
                while (line != null) {
                    line = line.substring(line.indexOf("package:")
                            + "package:".length)
                    if (packageName == line) {
                        return true
                    }
                    line = bis.readLine()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return false
        }
    }
}
