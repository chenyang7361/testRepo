package com.mivideo.mifm.player

/**
 * 播放器生命周期拦截器
 *
 * 外部可通过添加生命周期拦截器，不让播放器处理对应生命周期函数回调，例如：在播放详情
 * 界面视频正在播放，弹出登录弹框，跳转微信登录成功回来，onResume方法需要被拦截，登录弹框没有消失
 * 登录弹框消失后才能恢复播放
 *
 * @author LiYan
 */
open class PlayerLifecycleInterceptor {
    open fun onInterceptCreate(): Boolean {
        return false
    }

    open fun onInterceptStart(): Boolean {
        return false
    }

    open fun onInterceptResume(): Boolean {
        return false
    }

    open fun onInterceptPause(): Boolean {
        return false
    }

    open fun onInterceptStop(): Boolean {
        return false
    }

    open fun onInterceptDestroy(): Boolean {
        return false
    }

}