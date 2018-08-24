package com.mivideo.mifm.util.app

import com.hwangjr.rxbus.RxBus

fun postEvent(event: Any) {
    RxBus.get().post(event)
}
