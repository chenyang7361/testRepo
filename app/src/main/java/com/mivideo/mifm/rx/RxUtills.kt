package com.mivideo.mifm.rx

import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.Executors

private val executors = Executors.newFixedThreadPool(128)

fun <T> asyncSchedulers(): Observable.Transformer<T, T> {
    return Observable.Transformer { observable ->
        observable.subscribeOn(Schedulers.from(executors))
                .observeOn(AndroidSchedulers.mainThread())
    }
}
