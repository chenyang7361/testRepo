package com.mivideo.mifm.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.mivideo.mifm.network.commonurl.WLReflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * 屏幕工具类--获取手机屏幕信息
 *
 * @author zihao
 */
@SuppressWarnings("HardCodedStringLiteral")
public class ScreenUtil {

    /**
     * 获取屏幕的宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getWidth();
    }

    /**
     * 获取屏幕的高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getHeight();
    }

    /**
     * 获取屏幕中控件顶部位置的高度--即控件顶部的Y点
     *
     * @return
     */
    public static int getScreenViewTopHeight(View view) {
        return view.getTop();
    }

    /**
     * 获取屏幕中控件底部位置的高度--即控件底部的Y点
     *
     * @return
     */
    public static int getScreenViewBottomHeight(View view) {
        return view.getBottom();
    }

    /**
     * 获取屏幕中控件左侧的位置--即控件左侧的X点
     *
     * @return
     */
    public static int getScreenViewLeftHeight(View view) {
        return view.getLeft();
    }

    /**
     * 获取屏幕中控件右侧的位置--即控件右侧的X点
     *
     * @return
     */
    public static int getScreenViewRightHeight(View view) {
        return view.getRight();
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        if (context == null) {
            return 0;
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        if (context == null) {
            return 0;
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static DisplayMetrics displayMetrics(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    /**
     * 获取屏幕高度
     */
    public static int getScreenHeight(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }

    /**
     * 获取状态栏高度
     *
     * @param activity
     * @return
     */
    public static int getStatusHeight(Activity activity) {
        int stausHeight;
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        stausHeight = frame.top;
        return stausHeight;
    }

    /**
     * 获取状态栏高度
     *
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 修改状态栏为全透明
     *
     * @param activity
     */
    @TargetApi(19)
    public static boolean transparencyStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    // | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            );
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
//            window.setNavigationBarColor(Color.TRANSPARENT);

            return true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = activity.getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            return true;
        }
        return false;
    }

    /**
     * 设置状态栏背景与界面布局顶部连接
     *
     * @param activity
     * @param topLayout
     */
    public static boolean setStatusBarTransparentWithLayoutTop(Activity activity, View topLayout) {
        if (activity == null || topLayout == null) {
            return false;
        }
        //设置状态栏关联
        boolean transparencyResult = transparencyStatusBar(activity);
        if (transparencyResult) {
            int paddingBottom = topLayout.getPaddingBottom();
            int paddingRight = topLayout.getPaddingRight();
            int paddingLeft = topLayout.getPaddingLeft();
            int paddingTop = topLayout.getPaddingTop();
            int statusBarHeight = getStatusBarHeight(activity.getApplicationContext());
            topLayout.setPadding(paddingLeft, paddingTop + statusBarHeight, paddingRight, paddingBottom);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 如果是魅族flyme系统，获取smart bar高度
     */
    public static int getSmartBarHeight(Context context) {
        try {
            Class c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("mz_action_button_min_height");
            int height = Integer.parseInt(field.get(obj).toString());
            return context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 通过反射，获取包含虚拟键的整体屏幕高度
     */
    public static int getFullScreenHeight(Activity activity) {
        int screenHeight = 0;
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            screenHeight = dm.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenHeight;
    }

    /**
     * 获取底部虚拟按键高度
     */
    public static int getNavigationBarHeight(Context context) {
        if (!deviceHasNavigationBar(context)) return 0;
        //set padding distance to bottom navigation bar if device has bottom navigation bar
        int identifier = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelSize(identifier);
    }

    private static Boolean HAS_NAVIGATION_BAR = null;

    private static boolean isNavigationBarShowing = true;
    private static int originSystemUi = -1;

    public static boolean deviceHasNavigationBar(Context appContext) {
        if (HAS_NAVIGATION_BAR == null && appContext != null) {
            Resources res = appContext.getResources();
            int id = res.getIdentifier("config_showNavigationBar", "bool", "android");
            if (id > 0) {
                HAS_NAVIGATION_BAR = res.getBoolean(id);
            }
            String navBarOverride = WLReflect.getSystemProperties("qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                HAS_NAVIGATION_BAR = false;
            } else if ("0".equals(navBarOverride)) {
                HAS_NAVIGATION_BAR = true;
            }
        }
        return HAS_NAVIGATION_BAR == null ? false : HAS_NAVIGATION_BAR;
    }


    /**
     * 隐藏底部导航栏
     */
    public static void hideNavigationBar(Activity activity) {
        if (!isNavigationBarShowing || !deviceHasNavigationBar(activity.getApplicationContext()))
            return;
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        int newViewOption = View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        View decorView = activity.getWindow().getDecorView();
        originSystemUi = decorView.getSystemUiVisibility();
        decorView.setSystemUiVisibility(newViewOption);
        isNavigationBarShowing = false;
    }

    /**
     * 隐藏底部导航栏
     */
    public static void showNavigationBar(Activity activity) {
        if (isNavigationBarShowing || !deviceHasNavigationBar(activity.getApplicationContext()))
            return;
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (originSystemUi != -1) {
            activity.getWindow().getDecorView().setSystemUiVisibility(originSystemUi);
        } else {
            int newViewOption = View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            activity.getWindow().getDecorView().setSystemUiVisibility(newViewOption);
        }
        isNavigationBarShowing = true;
    }

    /**
     * 设置底部导航栏背景颜色
     */
    public static void setNavigationBarColor(Window window, int color)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (Build.VERSION.SDK_INT >= 23) {
            Class<?>[] paramType = new Class<?>[1];
            paramType[0] = int.class;
            Object[] values = new Object[1];
            values[0] = color;
            Method targetMethod = window.getClass().getMethod("setNavigationBarColor", paramType);
            targetMethod.setAccessible(true);
            targetMethod.invoke(window, values);
        }
    }
}
