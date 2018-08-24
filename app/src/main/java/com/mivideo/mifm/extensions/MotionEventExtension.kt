package com.mivideo.mifm.extensions

import android.view.MotionEvent


/**
 * Clone given motion e and set specified action. This method is useful, when we want to
 * cancel e propagation in child views by sending e with [ ][MotionEvent.ACTION_CANCEL]
 * action.
 *
 * @param action new action
 * @return cloned motion e
 */
fun MotionEvent.cloneWithNewAction(action: Int): MotionEvent {
    return MotionEvent.obtain(downTime, eventTime, action, x, y, metaState)
}
