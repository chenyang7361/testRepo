package com.mivideo.mifm.manager

import android.app.Activity
import android.content.Context
import android.provider.Settings
import android.view.OrientationEventListener
import timber.log.Timber
import java.util.ArrayList

/**
 * Created by xingchang on 16/12/11.
 */
class OrientationManager() {
    private val TAG = javaClass.simpleName
    private var mLockedLandOnce = false
    private var mLockedPortOnce = false
    private var mIsLand = false
    private var mList: ArrayList<OrientationChangedListener> = ArrayList()
    private var mRegisterList: ArrayList<Activity> = ArrayList()
    private var mLockOrientation = false
    private var mLock = false
    private var mGuideShowing = false
    private var mInputShowing = false
    private var mLastRotate = 0L
    private var mCallbackList: ArrayList<OrientationLockedCallback> = ArrayList()

    private lateinit var mOrientationEventListener: OrientationEventListener

    constructor(context: Context) : this() {
        mOrientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                Timber.i("orientation: $orientation")
                if (!hasRegister() || isSystemLocked(context) || mLock || mGuideShowing
                        || mLockOrientation || mInputShowing || locked()
                        || orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                    return
                }

                // 设置竖屏
                if (isPortrait(orientation) || isPortraitReverse(orientation)) {
                    if (mIsLand) {
                        if (mLockedLandOnce) {
                            return
                        } else {
                            portrait(false, orientation)
                        }
                    } else {
                        mLockedPortOnce = false
                    }
                }
                // 设置横屏
                else if (isLandscape(orientation) || isLandscapeReverse(orientation)) {
                    if (!mIsLand) {
                        if (mLockedPortOnce) {
                            return
                        } else {
                            landscape(false, orientation)
                        }
                    } else {
                        mLockedLandOnce = false
                    }
                }
            }
        }
    }

    private fun isPortrait(orientation: Int): Boolean {
        return orientation > 350 || orientation < 10
    }

    private fun isPortraitReverse(orientation: Int): Boolean {
        return orientation in 171..189
    }

    private fun isLandscapeReverse(orientation: Int): Boolean {
        return (orientation in 81..99)
    }

    private fun isLandscape(orientation: Int): Boolean {
        return (orientation > 260) && (orientation < 280)
    }

    private fun isSystemLocked(context: Context): Boolean {
        var rotation = 0
        try {
            rotation = Settings.System.getInt(
                    context.contentResolver, Settings.System.ACCELEROMETER_ROTATION)
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }
        return rotation == 0
    }

    private fun hasRegister(): Boolean {
        return !mRegisterList.isEmpty()
    }

    fun setCallback(callback: OrientationLockedCallback) {
        mCallbackList.add(callback)
    }

    fun removeCallback(callback: OrientationLockedCallback) {
        mCallbackList.remove(callback)
    }

    fun setMInputShowing(mInputShowing: Boolean) {
        this.mInputShowing = mInputShowing
    }

    private fun locked(): Boolean {
        return mCallbackList.any { it.isLocked() }
    }

    fun register(activity: Activity) {
        if (!mRegisterList.contains(activity)) {
            mRegisterList.add(activity)
        }
        mOrientationEventListener.enable()
    }

    fun unregister(activity: Activity) {
        mRegisterList.remove(activity)
        if (mRegisterList.isEmpty()) {
            mOrientationEventListener.disable()
        }
    }

    fun lockOrientation() {
        mLockOrientation = true
    }

    fun unlockOrientation() {
        mLockOrientation = false
    }

    fun lock() {
        mLock = true
    }

    fun unlock() {
        mLock = false
    }

    fun lockedScreen(): Boolean {
        return mLock
    }

    fun isGuideShowing(showing: Boolean) {
        mGuideShowing = showing
    }

    fun landscape(lock: Boolean) {
        landscape(lock, 270)
    }

    private fun landscape(lock: Boolean, orientation: Int) {
        if (!hasRegister()) {
            return
        }
        if (System.currentTimeMillis() - mLastRotate < 1000)
            return

        mIsLand = true
        mLastRotate = System.currentTimeMillis()
        if (lock)
            mLockedLandOnce = true
        for (listener in mList) {
            if (isLandscape(orientation)) {
                listener.onOrientationChanged(OrientationChangedListener.ORIENTATION_LANDSCAPE)
            } else {
                listener.onOrientationChanged(OrientationChangedListener.ORIENTATION_LANDSCAPE_REVERSE)
            }
        }
    }

    fun portrait(lock: Boolean) {
        portrait(lock, 0)
    }

    private fun portrait(lock: Boolean, orientation: Int) {
        if (!hasRegister()) {
            return
        }
        if (System.currentTimeMillis() - mLastRotate < 1000)
            return

        mIsLand = false
        mLastRotate = System.currentTimeMillis()

        if (lock)
            mLockedPortOnce = true

        for (listener in mList) {
            if (isPortrait(orientation)) {
                listener.onOrientationChanged(OrientationChangedListener.ORIENTATION_PORTRAIT)
            } else {
                listener.onOrientationChanged(OrientationChangedListener.ORIENTATION_PORTRAIT_REVERSE)
            }
        }
    }

    fun addOrientationChangedListener(listener: OrientationChangedListener) {
        mList.add(listener)
    }

    fun removeOrientationChangedListener(listener: OrientationChangedListener) {
        mList.remove(listener)
    }

    interface OrientationChangedListener {
        companion object {
            /**
             * 横向方向，手机头朝左，270度，
             */
            val ORIENTATION_LANDSCAPE = 0

            /**
             * 横向方向,手机头朝右 90度
             */
            val ORIENTATION_LANDSCAPE_REVERSE = 2

            /**
             * 手机正常方向，竖直方向，0度
             */
            val ORIENTATION_PORTRAIT = 1
            /**
             * 竖直方向，只是上下翻转了， 180度
             */
            val ORIENTATION_PORTRAIT_REVERSE = 3

        }

        fun onOrientationChanged(orientation: Int)
    }

    interface OrientationLockedCallback {
        fun isLocked(): Boolean
    }
}