package com.mivideo.mifm.player

/**
 * Created by yamlee on 23/03/2018.
 */
interface ControllerViewTransition {
    /**
     * 播控view进入显示时执行动作
     */
    fun onEnter(orientation: Int)

    /**
     * 播控view退出时是执行动作
     */
    fun onExit()
}