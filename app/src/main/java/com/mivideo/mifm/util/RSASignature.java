package com.mivideo.mifm.util;

import android.util.Base64;
import android.util.Log;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Created by xingchang on 16/8/2.
 */
public class RSASignature {
    private static final String SIGN_ALGORITHM = "SHA1WithRSA";
    private static final String ALGORITHM_OF_KEY = "RSA";
    private static final String ENCODING = "UTF-8";

    private static final String KEY = "MIICXQIBAAKBgQDbHSMo3eLaPI+W69Nwr4WsKg2aaR+HPjYI22oCvfQfG2HvRFnm\n" +
            "Huoj/66dODpq1SIREwXpa2h+vy35SwJmKI5td67tvLXaLfMURMlygI6Aiu4I0/CR\n" +
            "Q0Vih1DjQv6+XaDfDcs+Utdv7xkK2AoPP0cNXYCZr3X18LlXJR2l2WxIpwIDAQAB\n" +
            "AoGANkvSGp5hSJMZQdzOWG4mQbNqC4lhNrJD0Y4NhwBrLgorCo91d4DRpHoHWw5D\n" +
            "65VFC9Ya8OycyEu3qL7dqVMDd8d+QJ2ggQDyRdqynJNWX/P0pdiptl9lGSk0Sudd\n" +
            "OrsalT2yMJffy3TDgnW69/1nrvpSWpL8L+FVQIP2kDgLm4ECQQDvET0JV/358RwM\n" +
            "9fBelpWeTbZjbPND9RuaQqRPgP/6uI6SY+HZ8zPv5qL7w+gnBFD6vF5iY4GKZJNX\n" +
            "y32QnomrAkEA6qIaMTSEqTyg/7Nctfkge9AWtN8v4yL+0ZqqdsSVZViLn1qGbwyJ\n" +
            "WwUlISt0JB651P/uCeA36CRPYoWLLN+Y9QJBAMWHks0TnVBVPf4ff7pH5dvlDhxc\n" +
            "uyudbG8rPSRLzDXXJEYkrUouPu2rvWMU3c7PLDHgToQw/6nfrDbQdjF2LP0CQEWh\n" +
            "b7Z0iH4U4Q6hMv1J/orf2S06IuL8SrT3emkes9tMqLrUyZqBFO4vG7K5S5FAkROW\n" +
            "FBVYNh8tT2XEjeX2QVUCQQCppgXNs/aUMd1FnEaEQS0uuc7UkbrN9MLWIeZJZdsb\n" +
            "76qpo2ssQto2qC4huW51z7MrPKvjNpRzgoPfNb/Se6G7";

    public static String signRSA(String token, String ts) throws Exception {
        String plainText = "_token=" + token + "&_ts=" + ts;
        return sign(plainText, KEY);
    }

    /**
     * RSA签名
     *
     * @param plainText  待签名数据
     * @param privateKey 私钥
     * @return 签名值
     * @throws Exception
     */
    private static String sign(String plainText, String privateKey) throws Exception {
//        if (AppConfigModule.DEBUG) {
//            Log.d("RSASignature", "sign: plainText = " + plainText + "; privateKey = " + privateKey);
//        }
        PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decode(privateKey.getBytes(ENCODING), Base64.DEFAULT));

        KeyFactory keyf = KeyFactory.getInstance(ALGORITHM_OF_KEY, "BC");
//        KeyFactory keyf = KeyFactory.getInstance(ALGORITHM_OF_KEY);
        PrivateKey priKey = keyf.generatePrivate(priPKCS8);

        Signature signature = Signature.getInstance(SIGN_ALGORITHM);
        signature.initSign(priKey);
        signature.update(plainText.getBytes(ENCODING));
        byte[] signed = signature.sign();
        return new String(Base64.encode(signed, Base64.DEFAULT), ENCODING);
    }
}
