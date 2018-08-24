package com.mivideo.mifm.util.app

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import com.mivideo.mifm.util.SystemUtil.Companion.isFlyme
import com.mivideo.mifm.util.SystemUtil.Companion.isMiui
import com.mivideo.mifm.network.commonurl.WLReflect
import timber.log.Timber


object DisplayUtil {

    var screenWidthPx: Int = 0 //屏幕宽 px
    var screenHeightPx: Int = 0 //屏幕高 px
    var density: Float = 0.toFloat()//屏幕密度
    var densityDPI: Int = 0//屏幕密度
    var screenWidthDip: Float = 0.toFloat()//  dp单位
    var screenHeightDip: Float = 0.toFloat()//  dp单位
    var statusBarHeight: Int = 0

    val DEFAULT_STATUS_BAR_ALPHA: Int = 0

    /**
     * 在Application中初始化设备的尺寸信息
     */
    fun initDisplayOpinion(mContext: Activity) {
        val dm = mContext.resources.displayMetrics
        density = dm.density
        densityDPI = dm.densityDpi
        screenWidthPx = dm.widthPixels
        screenHeightPx = dm.heightPixels

        screenWidthDip = px2dip(mContext.applicationContext, dm.widthPixels).toFloat()
        screenHeightDip = px2dip(mContext.applicationContext, dm.heightPixels).toFloat()
        statusBarHeight = getStatusBarHeight(mContext)
    }

    /**
     * 获取设备状态栏的高度
     */
    private fun getStatusBarHeight(context: Activity): Int {
        val frame = Rect()
        context.window.decorView.getWindowVisibleDisplayFrame(frame)

        return frame.top
    }

    /**
     * 设置状态栏字体图标为深色，需要MIUI6以上

     * @param isFontColorDark 是否把状态栏字体及图标颜色设置为深色
     * *
     * @return boolean 成功执行返回true
     */
    fun setStatusBarLightMode(activity: Activity, isFontColorDark: Boolean) {
        when {
            isMiui() -> setMiuiStatusBarLightMode(activity, isFontColorDark)
            isFlyme() -> setFlymeStatusBarLightMode(activity, isFontColorDark)
            else -> setOtherStatusBarLightMode(activity, isFontColorDark)
        }
    }

    private fun setMiuiStatusBarLightMode(activity: Activity, isFontColorDark: Boolean) {
        Timber.i("setMiuiStatusBarLightMode")
        try {
            val window = activity.window
            if (window != null) {
                if (isOverMIUI9() && Build.VERSION.SDK_INT >= 23) {
                    if (isFontColorDark) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        window.statusBarColor = Color.WHITE
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        /*window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)*/
                        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                        window.statusBarColor = Color.BLACK
                    }
                } else {
                    val clazz = window::class.java
                    try {
                        var darkModeFlag = 0
                        val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                        val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
                        darkModeFlag = field.getInt(layoutParams)
                        val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                        if (isFontColorDark) {
                            extraFlagField.invoke(window, darkModeFlag, darkModeFlag)//状态栏透明且黑色字体
                        } else {
                            extraFlagField.invoke(window, 0, darkModeFlag)//清除黑色字体
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setFlymeStatusBarLightMode(activity: Activity, isFontColorDark: Boolean): Boolean {
        Timber.i("setFlymeStatusBarLightMode")
        var result = false
        val window = activity.window
        if (window != null) {
            try {
                val lp = window.getAttributes()
                val darkFlag = WindowManager.LayoutParams::class.java
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                val meizuFlags = WindowManager.LayoutParams::class.java
                        .getDeclaredField("meizuFlags")
                darkFlag.isAccessible = true
                meizuFlags.isAccessible = true
                val bit = darkFlag.getInt(null)
                var value = meizuFlags.getInt(lp)
                if (isFontColorDark) {
                    value = value or bit
                } else {
                    value = value and bit.inv()
                }
                meizuFlags.setInt(lp, value)
                window.setAttributes(lp)
                result = true
            } catch (e: Exception) {

            }
        }
        return result
    }

    private fun setOtherStatusBarLightMode(activity: Activity, isFontColorDark: Boolean) {
        Timber.i("setOtherStatusBarLightMode")
        if (activity.window != null) {
            if (isFontColorDark) {
                setColor(activity, Color.WHITE, 60, isFontColorDark)
            } else {
                setColor(activity, Color.BLACK, 0, isFontColorDark)
            }
        }
    }

    private fun isOverMIUI9(): Boolean {
        var overMIUI9: Boolean = false
        try {
            val miuiVersion = WLReflect.getSystemProperties("ro.miui.ui.version.name")
            if (!TextUtils.isEmpty(miuiVersion) && miuiVersion.length >= 2) {
                val version = miuiVersion.substring(1).toInt()
                if (version >= 9) {
                    overMIUI9 = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return overMIUI9
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    fun px2dip(context: Context, pxValue: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }


    /**
     * 将px值转换为sp值，保证文字大小不变

     * @param pxValue （DisplayMetrics类中属性scaledDensity）
     * *
     * @return
     */
    fun px2sp(context: Context, pxValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (pxValue / fontScale + 0.5f).toInt()
    }

    /**
     * 将sp值转换为px值，保证文字大小不变

     * @param spValue （DisplayMetrics类中属性scaledDensity）
     * *
     * @return
     */
    fun sp2px(context: Context, spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    /**
     * 设置状态栏颜色
     *
     * @param activity       需要设置的activity
     * @param color          状态栏颜色值
     * @param statusBarAlpha 状态栏透明度
     */
    @JvmOverloads
    fun setColor(activity: Activity, color: Int, statusBarAlpha: Int = DEFAULT_STATUS_BAR_ALPHA, isFontColorDark: Boolean = true) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                if (isFontColorDark) {
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                }
                activity.window.statusBarColor = color
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                if (isFontColorDark) {
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                } else {
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                }
                activity.window.statusBarColor = calculateStatusColor(color, statusBarAlpha)
            }
//            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
//                activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//                // 生成一个状态栏大小的矩形
//                val statusView = createStatusBarView(activity, color, statusBarAlpha)
//                // 添加 statusView 到布局中
//                val decorView = activity.window.decorView as ViewGroup
//                decorView.addView(statusView)
//                setRootView(activity)
//            }
        }
    }

    /**
     * 计算状态栏颜色
     *
     * @param color color值
     * @param alpha alpha值
     * @return 最终的状态栏颜色
     */
    private fun calculateStatusColor(color: Int, alpha: Int): Int {
        val a = 1 - alpha / 255f
        var red = color shr 16 and 0xff
        var green = color shr 8 and 0xff
        var blue = color and 0xff
        red = (red * a + 0.5).toInt()
        green = (green * a + 0.5).toInt()
        blue = (blue * a + 0.5).toInt()
        return 0xff shl 24 or (red shl 16) or (green shl 8) or blue
    }
}

