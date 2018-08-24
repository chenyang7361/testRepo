package com.mivideo.mifm.player

import android.content.Context
import android.view.MotionEvent
import com.mivideo.mifm.util.app.DisplayUtil.density
import com.mivideo.mifm.util.app.DisplayUtil.screenWidthPx

/**
 * Created by yamlee on 30/10/2017.
 */
class PlayerGestureDetector(val context: Context) {
    private var mTouchStartY = -1f
    private var mX = 0f
    private var mY = 0f
    private var mMoved = false
    private var mMovedLeft = false
    private var mMovedRight = false
    private var mMovedCenter = false
    private var mDownRightRegion = false
    private var mDownLeftRegion = false
    private var Y_TOLERANCE = 0f
    private var X_TOLERANCE = 0f

    init {
        X_TOLERANCE = 10 * density
        Y_TOLERANCE = 10 * density
    }

    private var mGestureListener: OnPlayerGestureListener? = null

    fun setOnGestureListener(listener: OnPlayerGestureListener) {
        this.mGestureListener = listener
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        return handleGestureTouch(event)
    }

    private fun handleGestureTouch(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            mTouchStartY = event.y
            touchStart(event.x, event.y)
        } else {
            val statusBarTolerance = 10
            if (mTouchStartY >= 0 && mTouchStartY < statusBarTolerance) {
                // drag status bar.
                return false
            }
            if (event.action == MotionEvent.ACTION_MOVE) {
                mTouchStartY = -1f
                touchMove(event.x, event.y)
            } else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                touchUp(event.x, event.y)
                mTouchStartY = -1f
            }
        }
        return true
    }


    private fun touchStart(x: Float, y: Float) {
        val screenWidth = screenWidthPx
        mX = x
        mY = y
        mMovedLeft = false
        mMovedRight = false
        mMovedCenter = false
        if (mX <= screenWidth / 2) {
            mDownLeftRegion = true
        } else if (mX >= screenWidth - screenWidth / 2) {
            mDownRightRegion = true
        }
    }

    private fun touchMove(x: Float, y: Float) {
        val distanceX = x - mX
        val distanceY = y - mY
        val dx = Math.abs(distanceX)
        val dy = Math.abs(distanceY)
        if (dy > Y_TOLERANCE && (dy > dx || mMovedLeft || mMovedRight) && !mMovedCenter) {
            if (mDownLeftRegion) {
                mMovedLeft = true
                mGestureListener?.onTouchMove(KPlayerView.REGION_LEFT, distanceX, distanceY)
                mMoved = true
                mX = x
                mY = y
            }
            if (mDownRightRegion) {
                mMovedRight = true
                mGestureListener?.onTouchMove(KPlayerView.REGION_RIGHT, distanceX, distanceY)
                mMoved = true
                mX = x
                mY = y
            }
        } else if (dx > X_TOLERANCE && (dx >= dy || mMovedCenter) && !mMovedLeft && !mMovedRight) {
            mMovedCenter = true
            mGestureListener?.onTouchMove(KPlayerView.REGION_CENTER, distanceX, distanceY)
            mMoved = true
            mX = x
            mY = y
        }
    }

    private fun touchUp(x: Float, y: Float) {
        if (!mMoved) {
            mGestureListener?.onTab(KPlayerView.REGION_CENTER)
        } else {
            if (mMovedLeft) {
                mGestureListener?.onTouchUp(KPlayerView.REGION_LEFT)
            } else if (mMovedRight) {
                mGestureListener?.onTouchUp(KPlayerView.REGION_RIGHT)
            } else if (mMovedCenter) {
                mGestureListener?.onTouchUp(KPlayerView.REGION_CENTER)
            }
        }
        mMoved = false
        mDownLeftRegion = false
        mDownRightRegion = false
    }


    interface OnPlayerGestureListener {
        fun onTouchMove(region: Int, movementX: Float, movementY: Float)

        fun onTab(region: Int)

        fun onTouchUp(region: Int)
    }
}