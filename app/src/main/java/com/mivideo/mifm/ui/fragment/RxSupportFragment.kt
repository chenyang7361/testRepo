package com.mivideo.mifm.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.support.annotation.CheckResult
import android.view.View
import com.mivideo.mifm.SupportFragment
import com.trello.rxlifecycle.FragmentEvent
import com.trello.rxlifecycle.RxLifecycle
import com.trello.rxlifecycle.components.FragmentLifecycleProvider
import rx.Observable
import rx.subjects.BehaviorSubject

/**
 * 支持 RxJava 生命周期管理的 SupportFragment
 */
open class RxSupportFragment : SupportFragment(), FragmentLifecycleProvider {

    private val lifecycleSubject = BehaviorSubject.create<FragmentEvent>()

    @CheckResult
    override fun lifecycle(): Observable<FragmentEvent> {
        return lifecycleSubject.asObservable()
    }

    @CheckResult
    override fun <T> bindUntilEvent(event: FragmentEvent): Observable.Transformer<T, T> {
        return RxLifecycle.bindUntilFragmentEvent<T>(lifecycleSubject, event)
    }

    @CheckResult
    override fun <T> bindToLifecycle(): Observable.Transformer<T, T> {
        return RxLifecycle.bindFragment<T>(lifecycleSubject)
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        lifecycleSubject.onNext(FragmentEvent.ATTACH)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleSubject.onNext(FragmentEvent.CREATE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleSubject.onNext(FragmentEvent.CREATE_VIEW)
    }

    override fun onStart() {
        super.onStart()
        lifecycleSubject.onNext(FragmentEvent.START)
    }

    override fun onResume() {
        super.onResume()
        lifecycleSubject.onNext(FragmentEvent.RESUME)
    }

    override fun onPause() {
        lifecycleSubject.onNext(FragmentEvent.PAUSE)
        super.onPause()
    }

    override fun onStop() {
        lifecycleSubject.onNext(FragmentEvent.STOP)
        super.onStop()
    }

    override fun onDestroyView() {
        lifecycleSubject.onNext(FragmentEvent.DESTROY_VIEW)
        super.onDestroyView()
    }

    override fun onDestroy() {
        lifecycleSubject.onNext(FragmentEvent.DESTROY)
        super.onDestroy()
    }

    override fun onDetach() {
        lifecycleSubject.onNext(FragmentEvent.DETACH)
        super.onDetach()
    }
}
