package com.mivideo.mifm.extensions

import android.os.Handler
import android.os.Looper
import com.mivideo.mifm.BuildConfig
import com.mivideo.mifm.rx.asyncSchedulers
import rx.Observable
import rx.Subscriber
import timber.log.Timber

/**
 * 异步任务方法，此内联方法执行提会被异步加载，如果初始化对象异步加载
 * 对后续没有影响，可通过此内联方法做异步加载
 */
inline fun asyncTask(crossinline body: () -> Unit) {
    Observable.create<Boolean> { body() }
            .compose(asyncSchedulers())
            .subscribe(object : Subscriber<Boolean>() {
                override fun onError(e: Throwable?) {
                    if (BuildConfig.DEBUG) {
                        e?.printStackTrace()
                    }
                }

                override fun onCompleted() {
                }

                override fun onNext(t: Boolean?) {
                }
            })
}

inline fun uiThread(crossinline body: () -> Unit) {
    val handler = Handler(Looper.getMainLooper())
    handler.post { body() }
}


/**
 * 捕获异常
 */
inline fun tryWith(crossinline body: () -> Unit) {
    try {
        body()
    } catch (e: Exception) {
        Timber.w(e.message)
    }
}
