package com.mivideo.mifm.data.viewmodel

import android.content.Context

class ConfigViewModel(val context: Context) : BaseViewModel(context) {

    /**
     * 执行数据初始化操作
     */
    fun initConfig() {
        // TODO:
//        observeCpConfig()
//        observeTaskConfig()
//        observeLaunchConfig()
    }

    /**
     * 清除配置数据
     */
    fun destroyConfig() {
    }

    private fun observeLaunchConfig() {
//        if (configInfo == null) {
//            mainRepository.launchConfiguration()
//                    .compose(asyncSchedulers())
//                    .subscribe({ info ->
//                        configInfo = info
//                        if (configInfo != null) {
//                            cacheRedPacketImg(configInfo!!.data.inspire_config.red_packet_img)
//                            signCloseSubject.onNext(configInfo!!.data.inspire_config.show_close != 0)
//                            pullImageSubject.onNext(configInfo!!.data.image.url)
//                        } else {
//                            signCloseSubject.onNext(false)
//                            pullImageSubject.onNext("")
//                        }
//
//                        updateExportRate(configInfo!!.data.exposure_rate.value)
//                    }, {})
//        }
    }

    fun getTopPullImage(): String? {
//        if (configInfo != null) {
//            return configInfo!!.data.image.url
//        }
        return null
    }
}
