package com.mivideo.mifm.util

import android.util.Base64

/**
 * 加密解密工具类

 * @author LiYan
 */
class SecurityUtil {


    companion object {
        /**
         * Base64加密内容
         */
        fun enBase64(content: String): String {
            val result = String(Base64.encode(content.toByteArray(), Base64.DEFAULT))
            return result
        }

        /**
         * Base64解密内容
         */
        fun deBase64(content: String): String {
            val result = String(Base64.decode(content, Base64.DEFAULT))
            return result
        }
    }

}
