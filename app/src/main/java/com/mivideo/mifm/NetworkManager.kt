package com.mivideo.mifm

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork
import com.mivideo.mifm.util.NetworkUtil
import com.mivideo.mifm.rx.asyncSchedulers
import rx.Subscription
import timber.log.Timber

class NetworkManager {

    companion object {
        var networkStatus: NetworkInfo.State = NetworkInfo.State.CONNECTED
        var networkType: Int = ConnectivityManager.TYPE_WIFI
        var mSubscription: Subscription? = null

        /**
         * 初始化网络监听
         */
        fun init(context: Context) {
            mSubscription?.unsubscribe()
            mSubscription = ReactiveNetwork
                    .observeNetworkConnectivity(context.applicationContext)
                    .compose(asyncSchedulers())
                    .subscribe { connectivity ->
                        Timber.i({ "网络状态发生变化.........${connectivity.state}   ${connectivity.type}" }.invoke())
                        networkStatus = connectivity.state
                        networkType = connectivity.type
                    }
        }

        /**
         * 判断当前是否联网
         */
        fun isNetworkUnConnected(): Boolean {
            return networkStatus == NetworkInfo.State.DISCONNECTED && networkType == -1
        }

        /**
         *判断手机联网
         */
        fun isNetworkConnected(context: Context): Boolean {
            return NetworkUtil.isNetworkConnected(context)
        }


        /**
         * 判断当前是否使用wifi连接到网络
         */
        fun isUseWifiConnected(context: Context): Boolean {
            return networkType == ConnectivityManager.TYPE_WIFI
                    && isNetworkConnected(context)
        }

        /**
         * 判断当前是否使用手机网络连接到网络
         */
        fun isUseMobileNetConnected(context: Context): Boolean {
            return networkType == ConnectivityManager.TYPE_MOBILE
                    && NetworkManager.isNetworkConnected(context)
        }

        /**
         * 取消网络监听
         */
        fun unsubscribe() {
            mSubscription?.unsubscribe()
        }

    }
}
