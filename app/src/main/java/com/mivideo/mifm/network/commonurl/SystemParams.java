package com.mivideo.mifm.network.commonurl;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.mivideo.mifm.BuildConfig;

import java.net.URLEncoder;

/**
 * Created by aaron on 2018/4/25.
 */

public class SystemParams {

    private static String appVerCode = null;
    private static String appVerName = null;
    private static String ipAddress = null;
    private static int netType = -1;

    static String getAndver() {
        return String.valueOf(Build.VERSION.SDK_INT);
    }

    static String getMiuiver() {
        return Build.VERSION.INCREMENTAL;
    }

    static String getModel() {
        return Build.MODEL;
    }

    static String getTs() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

    /**
     * 获取App版本号
     */
    static String getAppVer(Context mContext) {
        if (TextUtils.isEmpty(appVerCode)) {
            int code = 0;
            try {
                code = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            appVerCode = String.valueOf(code);
        }
        return appVerCode;
    }

    /**
     * 获取App版本名称
     */
    static String getAppVerName(Context mContext) {
        if (TextUtils.isEmpty(appVerName)) {
            try {
                appVerName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
                appVerName = URLEncoder.encode(appVerName, "utf-8");
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return appVerName;
    }

    /**
     * 获取MIUI版本类型
     */
    static String getMIUIVersionType() {
        String version_type = "stable";
        if (BuildV5.IS_STABLE_VERSION) {
            version_type = "stable";
        } else if (BuildV5.IS_ALPHA_BUILD) {
            version_type = "alpha";
        } else if (BuildV5.IS_DEVELOPMENT_VERSION) {
            version_type = "dev";
        }

        return version_type;
    }

    static String getPlyver() {
        return "20161212";
    }

    /**
     * wap网络
     * wifi网络
     * 2G网络
     * 3G和3G以上网络，或统称为快速网络
     * 4G网络
     */
    private static final int NETWORKTYPE_WAP = 0;
    private static final int NETWORKTYPE_WIFI = 1;
    private static final int NETWORKTYPE_2G = 2;
    private static final int NETWORKTYPE_3G = 3;
    private static final int NETWORKTYPE_4G = 4;

    private static long lastNetworkTypeTime = 0;

    /**
     * 测试当前网络类型wifi？2G？3G？4G？
     */
    static int getNetworkType(Context context) {
        if (netType == -1 || SystemClock.elapsedRealtime() - lastNetworkTypeTime > 5000) {
            lastNetworkTypeTime = SystemClock.elapsedRealtime();

            int strNetworkType = NETWORKTYPE_WAP;
            if (context == null)
                return strNetworkType;

            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    strNetworkType = NETWORKTYPE_WIFI;
                } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    String _strSubTypeName = networkInfo.getSubtypeName();
                    int networkType = networkInfo.getSubtype();
                    switch (networkType) {
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                            strNetworkType = NETWORKTYPE_2G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                        case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                        case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                            strNetworkType = NETWORKTYPE_3G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                            strNetworkType = NETWORKTYPE_4G;
                            break;
                        default:
                            if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                                strNetworkType = NETWORKTYPE_3G;
                            } else {
                                strNetworkType = NETWORKTYPE_2G;
                            }
                            break;
                    }
                }
            }
            netType = strNetworkType;
        }

        return netType;
    }

    static long lastIpTime = 0;

    /**
     * 获取当前设备IP地址
     */
    static String getLocalIpAddress(Context mContext) {
        try {
            if (TextUtils.isEmpty(ipAddress) || SystemClock.elapsedRealtime() - lastIpTime > 5000) {
                lastIpTime = SystemClock.elapsedRealtime();
                WifiManager wifiManager = (WifiManager) mContext.getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int i = wifiInfo.getIpAddress();
                ipAddress = int2ip(i);
            }

            return ipAddress;
        } catch (Exception ex) {
            return " 获取IP出错鸟!!!!请保证是WIFI,或者请重新打开网络!\n" + ex.getMessage();
        }
    }

    static String getRef() {
        return "minivideo";
    }

    static String getChannel() {
        return BuildConfig.CONFIG_CHANNEL_VERSION;
    }

    /**
     * 获取MIUI版本名称
     */
    static String getMIUIVersion() {
        return WLReflect.getSystemProperties("ro.miui.ui.version.name");
    }

    /**
     * 将ip的整数形式转换成ip形式
     */
    private static String int2ip(int ipInt) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ipInt & 0xFF).append(".");
        stringBuilder.append((ipInt >> 8) & 0xFF).append(".");
        stringBuilder.append((ipInt >> 16) & 0xFF).append(".");
        stringBuilder.append((ipInt >> 24) & 0xFF);
        return stringBuilder.toString();
    }
}
