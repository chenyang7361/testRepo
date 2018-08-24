package com.mivideo.mifm.util

import rx.Subscription
import rx.subscriptions.CompositeSubscription

fun Subscription.addTo(compositeSubscription: CompositeSubscription) = apply { compositeSubscription.add(this) }
