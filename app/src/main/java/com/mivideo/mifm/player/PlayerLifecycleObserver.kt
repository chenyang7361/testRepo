package com.mivideo.mifm.player

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import timber.log.Timber

/**
 * 播放器对Activity生命周期监听处理
 * @author LiYan
 */
class PlayerLifecycleObserver(val videoController: VideoController) : LifecycleObserver {
    //判断播放器是否在生命周期之前已经暂停了，如果暂停了，在onResume时不需要恢复播放
    private var mIsPlayerPausedByOnPause = false

    private val mInterceptors = ArrayList<PlayerLifecycleInterceptor>()

    private val mInterceptOnCreate: Boolean
        get() {
            mInterceptors.forEach {
                if (it.onInterceptCreate()) {
                    return true
                }
            }
            return false
        }

    private val mInterceptOnStart: Boolean
        get() {
            mInterceptors.forEach {
                if (it.onInterceptStart()) {
                    return true
                }
            }
            return false
        }
    private val mInterceptOnResume: Boolean
        get() {
            mInterceptors.forEach {
                if (it.onInterceptResume()) {
                    return true
                }
            }
            return false
        }
    private val mInterceptOnPause: Boolean
        get() {
            mInterceptors.forEach {
                if (it.onInterceptPause()) {
                    return true
                }
            }
            return false
        }
    private val mInterceptOnStop: Boolean
        get() {
            mInterceptors.forEach {
                if (it.onInterceptStop()) {
                    return true
                }
            }
            return false
        }
    private val mInterceptOnDestroy: Boolean
        get() {
            mInterceptors.forEach {
                if (it.onInterceptDestroy()) {
                    return true
                }
            }
            return false
        }


    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        Timber.i("onCreate---->")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        Timber.i("onStart---->")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        Timber.i("onResume---->")
        Timber.i("onInterceptResume---->$mInterceptOnResume")
        if (mInterceptOnResume) return
        if (videoController.isPaused() && mIsPlayerPausedByOnPause) {
            videoController.resume()
            mIsPlayerPausedByOnPause = false
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        Timber.i("onPause---->")
        Timber.i("mInterceptOnPause---->$mInterceptOnPause")
        if (mInterceptOnPause) return
        if (videoController.isPlaying()) {
            videoController.pause()
            mIsPlayerPausedByOnPause = true
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        Timber.i("onStop---->")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        Timber.i("onDestroy---->")
        Timber.i("mInterceptOnDestroy---->$mInterceptOnDestroy")
        if (mInterceptOnDestroy) return
        videoController.stop()
        videoController.reset()
        videoController.release()
    }

    fun addInterceptor(playerLifecycleInterceptor: PlayerLifecycleInterceptor) {
        mInterceptors.add(playerLifecycleInterceptor)
    }

    fun removeInterceptor(playerLifecycleInterceptor: PlayerLifecycleInterceptor) {
        mInterceptors.remove(playerLifecycleInterceptor)
    }
}