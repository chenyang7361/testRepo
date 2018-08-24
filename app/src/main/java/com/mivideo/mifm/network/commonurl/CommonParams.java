package com.mivideo.mifm.network.commonurl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by aaron on 2016/11/25.
 * 主要用于获取通用参数，对外提供两个接口:
 *
 * @getCommonParamsByString
 * @getCommonParamsByMap
 */
public class CommonParams {
    private static String API_USER_ID = "";
    private static String API_USER_TOKEN = "";
    private static String deviceId = null;
    private static String cryptDeviceId = null;
    private static String imei = "";
    private static String cryptImei = "";
    private static String cryptUid = "";
    private static String cryptUtk = "";

    /**
     * 获取通用参数字符串
     */
    static String getCommonParamsByString(Context mContext, boolean sign) {
        StringBuffer sb = new StringBuffer("?");
        Map<String, String> commonParamsMap = getCommonParamsByMap(mContext, sign);
        Iterator<Map.Entry<String, String>> it = commonParamsMap.entrySet().iterator();
        int count = 0;
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            if (count == 0) {
                sb.append(entry.getKey()).append("=").append(entry.getValue());
            } else {
                sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
            count++;
        }

        return sb.toString();
    }

    /**
     * 获取通用参数Map对象
     *
     * @param mContext
     * @param sign
     * @return
     */
    static Map<String, String> getCommonParamsByMap(Context mContext, boolean sign) {
        Map<String, String> paramsMap = new HashMap<>();
        if (TextUtils.isEmpty(API_USER_TOKEN)) {
            API_USER_TOKEN = "TOKEN";
        }
        paramsMap.put("token", API_USER_TOKEN); // 用户Token
        if (TextUtils.isEmpty(API_USER_ID)) {
            API_USER_ID = "UID";
        }
        paramsMap.put("uid", API_USER_ID);
        return paramsMap;
    }

    public static String getMiuiVideoIMEI(Context mContext) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        String mimd = sp.getString("mimd", "");
        return mimd;
    }

    /**
     * 更新用户信息
     *
     * @param userId
     * @param userToken
     */
    public static void updateUser(String userId, String userToken) {
        API_USER_ID = userId;
        API_USER_TOKEN = userToken;
    }

    public static String getIMEI(Context context) {
        if (TextUtils.isEmpty(imei)) {
            String mvimei = getMiuiVideoIMEI(context);
            if (TextUtils.isEmpty(mvimei)) {
                String id = AccountUtils.getImeiId(context);
                if (!TextUtils.isEmpty(id)) {
                    imei = getMD5(id);
                }
            } else {
                imei = mvimei;
            }
        }

        return imei;
    }

    public static String getCryptIMEI(Context context) {
        if (TextUtils.isEmpty(cryptImei)) {
            String mvimei = getMiuiVideoIMEI(context);
            if (TextUtils.isEmpty(mvimei)) {
                cryptImei = encrypt(AccountUtils.getImeiId(context));
            } else {
                cryptImei = encrypt(mvimei);
            }
        }
        return cryptImei;
    }

    /**
     * 获取设备ID
     *
     * @param con
     * @return
     */
    public static String getDeviceMd5Id(Context con) {
        if (TextUtils.isEmpty(deviceId)) {
            String sDeviceMD5Id = "";
            String deviceID = getDeviceId(con);
            if (!TextUtils.isEmpty(deviceID)) {
                sDeviceMD5Id = getMD5(deviceID);
            }

            deviceId = sDeviceMD5Id;
        }

        return deviceId;
    }


    private static String getDeviceId(Context con) {
        return AccountUtils.getUid(con);
    }

    public static String getCryptDeviceId(Context con) {
        if (TextUtils.isEmpty(cryptDeviceId)) {
            String sDeviceId = "";
            String deviceID = getDeviceId(con);
            if (!TextUtils.isEmpty(deviceID)) {
                sDeviceId = encrypt(deviceID);
            }
            cryptDeviceId = sDeviceId;
        }
        return cryptDeviceId;
    }

    public static String getCryptUid(Context context) {
        if (!TextUtils.isEmpty(cryptUid)) {
            return cryptUid;
        }
        if (!TextUtils.isEmpty(API_USER_ID)) {
            cryptUid = encrypt(API_USER_ID);
        }
        return cryptUid;
    }

    public static String getCryptUtk(Context context) {
        if (!TextUtils.isEmpty(cryptUtk)) {
            return cryptUtk;
        }
        if (!TextUtils.isEmpty(API_USER_TOKEN)) {
            cryptUtk = encrypt(API_USER_TOKEN);
        }
        return cryptUtk;
    }

    public static String getMD5(File file) {
        if (file == null) {
            return "";
        }
        try {
            return getMD5(new FileInputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getMD5(InputStream is) {
        if (is == null) {
            return "";
        }
        try {
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = is.read(buffer);
                if (numRead > 0) {
                    digest.update(buffer, 0, numRead);
                }
            }
            return toHexString(digest.digest());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static String getMD5(String message) {
        if (TextUtils.isEmpty(message))
            return "";

        return getMD5(message.getBytes());
    }

    private static String getMD5(byte[] bytes) {
        if (bytes == null)
            return "";

        String digest = "";
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(bytes);
            digest = toHexString(algorithm.digest());
        } catch (Exception e) {
        }
        return digest;
    }

    public static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String str = Integer.toHexString(0xFF & b);
            while (str.length() < 2) {
                str = "0" + str;
            }
            hexString.append(str);
        }
        return hexString.toString();
    }

    public static byte[] hexStringToBytes(String hexString) {
        if ((hexString == null) || (hexString.equals(""))) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[(pos + 1)]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    private static final String DES_KEY = "wap&1wtbs2*threedes^5key";
    private static final String Algorithm = "DESede";

    public static String encrypt(String input) {
        String result = null;
        if (input == null || input.length() == 0) {
            return result;
        }
        try {
            byte[] encoded = encryptMode(DES_KEY.getBytes(), input.getBytes());
            result = toHexString(encoded);
        } catch (Throwable t) {
            t.printStackTrace();
            result = null;
        }
        return result;
    }

    public static byte[] encryptMode(byte[] keybyte, byte[] src) {
        byte[] result = null;
        try {
            SecretKey deskey = new SecretKeySpec(keybyte, Algorithm);

            Cipher c1 = Cipher.getInstance(Algorithm);
            c1.init(1, deskey);
            result = c1.doFinal(src);
        } catch (Throwable t) {
            t.printStackTrace();
            result = null;
        }
        return result;
    }

    private static boolean isEmulator(Context context) {
        try {
            String url = "tel:" + "123456";
            Intent intent = new Intent();
            intent.setData(Uri.parse(url));
            intent.setAction(Intent.ACTION_DIAL);
            // 是否可以处理跳转到拨号的 Intent
            boolean canResolveIntent = intent.resolveActivity(context.getPackageManager()) != null;

            return Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.toLowerCase().contains("vbox")
                    || Build.FINGERPRINT.toLowerCase().contains("test-keys")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.SERIAL.equalsIgnoreCase("unknown")
                    || Build.SERIAL.equalsIgnoreCase("android")
                    || Build.MODEL.contains("Android SDK built for x86")
                    || Build.MANUFACTURER.contains("Genymotion")
                    || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                    || "google_sdk".equals(Build.PRODUCT)
                    || ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                    .getNetworkOperatorName().toLowerCase().equals("android")
                    || !canResolveIntent;
        } catch (Exception e) {
            return false;
        }
    }
}
