package com.mivideo.mifm.network.commonurl;

import android.content.Context;

import java.io.File;
import java.util.Map;

/**
 * Created by aaron on 2016/11/25.
 */
public class NetworkParams {

    public static String getCommonParamsByString(Context context) {
        return CommonParams.getCommonParamsByString(context, false);
    }

    public static String getCommonParamsByString(Context mContext, boolean sign) {
        return CommonParams.getCommonParamsByString(mContext, sign);
    }

    public static Map<String, String> getCommonParamsByMap(Context context) {
        return CommonParams.getCommonParamsByMap(context, false);
    }

    public static Map<String, String> getCommonParamsByMap(Context context,boolean sign) {
        return CommonParams.getCommonParamsByMap(context, sign);
    }

    public static String getIMEI(Context context) {
        return CommonParams.getIMEI(context);
    }

    public static String getDeviceMd5Id(Context context) {
        return CommonParams.getDeviceMd5Id(context);
    }

    public static String getMD5(String message) {
        return CommonParams.getMD5(message);
    }

    public static String getMD5(File file) {
        return CommonParams.getMD5(file);
    }

    public static String getCryptIMEI(Context context) {
        return CommonParams.getCryptIMEI(context);
    }

    public static String getCryptDeviceId(Context context) {
        return CommonParams.getCryptDeviceId(context);
    }

    public static String getCryptUid(Context context) {
        return CommonParams.getCryptDeviceId(context);
    }

    public static String getCryptUtk(Context context) {
        return CommonParams.getCryptUtk(context);
    }

    public static String getAppVerName(Context context) {
        return SystemParams.getAppVerName(context);
    }

    public static String getAppVer(Context context) {
        return SystemParams.getAppVer(context);
    }

    public static String getResolution(Context context) {
        return DeviceParams.getResolution(context);
    }

    public static int getNetworkType(Context context) {
        return SystemParams.getNetworkType(context);
    }

    public static void updateUser(String userId, String userToken) {
        CommonParams.updateUser(userId, userToken);
    }
}
