package com.mivideo.mifm.data.api

import com.mivideo.mifm.BuildConfig
import java.util.*

object APIUrl {

    var BASE_URL = " http://qh-mitv-mvideo-rd01.bj:9000/" //线上地址

    const val ONLINE_URL = " http://qh-mitv-mvideo-rd01.bj:9000/"

    const val TEST_URL = " http://qh-mitv-mvideo-rd01.bj:9000/"

    const val PREVIEW_URL = " http://qh-mitv-mvideo-rd01.bj:9000/"


    /**
     * 账户认证域名地址
     */
    var DOMAIN_ACCOUNT = BuildConfig.ACCOUNT_URL

    const val DOMAIN_ACCOUNT_ONLINE = "http://duoshou.pandora.xiaomi.com"

    const val DOMAIN_ACCOUNT_PREVIEW = "http://preview.duoshou.pandora.xiaomi.com"

    const val DOMAIN_ACCOUNT_TEST = "http://preview.duoshou.pandora.xiaomi.com"

    /**
     * H5地址：协议-用户协议
     */
    var H5_AGREEMENT_USER = BASE_URL + "/videodaily-h5/#/agreement/user"
        get() = BASE_URL + "/videodaily-h5/#/agreement/user"
}

class APIMap : HashMap<String, String>() {

    companion object {

        fun getGetMap(): HashMap<String, String> {
            val paramsMap = HashMap<String, String>()
            paramsMap.put(APIParams.NETWORK_TYPE, APIParams.NETWORK_GET.toString())

            return paramsMap
        }

        fun getPostMap(): HashMap<String, String> {
            val paramsMap = HashMap<String, String>()
            paramsMap.put(APIParams.NETWORK_TYPE, APIParams.NETWORK_POST.toString())

            return paramsMap
        }
    }
}

object APIParams {
    // 网络请求类型-GET请求、POST请求
    val NETWORK_GET = 2
    val NETWORK_POST = 1
    val NETWORK_TYPE = "network_type"
}
