package com.mivideo.mifm.player

/**
 * 播放器播控UI通用接口
 *
 * @author LiYan
 */
interface ControllerView : ControllerViewTransition {

    /**
     * 修改对应尺寸的View
     */
    fun changePlayerSizeTo(state: PlayerSizeMode)

    /**
     * 显示无网情况下的布局
     */
    fun showNoNetworkLayout()

    /**
     * 隐藏无网情况下播放器上的布局
     */
    fun hideNoNetworkLayout()

    /**
     * 使用移动网络时,提示用户使用流量播放布局
     */
    fun showUseMobileNetLayout()

    /**
     * 隐藏流量播放提示布局
     */
    fun hideUseMobileNetLayout()

    /**
     * 显示错误提示界面
     */
    fun showErrorView(message: String)

    /**
     * 隐藏错误提示界面
     */
    fun hideErrorView()
}