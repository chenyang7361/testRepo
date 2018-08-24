package com.mivideo.mifm.network.commonurl;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.util.Locale;

/**
 * Created by aaron on 2018/4/25.
 */

public class DeviceParams {
    private static String resolution = null;
    private static String phoneLocale = null;

    /**
     * 获取屏幕分辨率
     */
    static String getResolution(Context mContext) {
        if (TextUtils.isEmpty(resolution)) {
            DisplayMetrics displaymetrics = mContext.getResources().getDisplayMetrics();
            if (displaymetrics.widthPixels == 720) {
                resolution = "hd720";
            } else if (displaymetrics.widthPixels == 1080) {
                resolution = "hd1080";
            } else if (displaymetrics.widthPixels == 1440) {
                resolution = "hd1440";
            } else if (displaymetrics.widthPixels == 2160) {
                resolution = "hd2160";
            } else {
                resolution = displaymetrics.widthPixels + "x" + displaymetrics.heightPixels;
            }
        }

        return resolution;
    }

    /**
     * 获取国家类型码
     */
    static String getLocale(Context mContext) {
        if (TextUtils.isEmpty(phoneLocale)) {
            Locale locale = mContext.getResources().getConfiguration().locale;
            phoneLocale = locale.getLanguage() + "_" + locale.getCountry();
        }
        return phoneLocale;
    }

    static String getDeviceType() {
        return "1";
    }
}
