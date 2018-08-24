package com.mivideo.mifm.player

/**
 * 播控界面错误界面操作外部回调
 * @author LiYan
 */
interface  ErrorHandleListener{
    /**
     * 当出现播放无网或其他错误导致的问题是，重试按钮点击监听
     */
    fun onClickRetry()

    /**
     * 使用移动网络时，询问界面 “继续”按钮点击监听
     */
    fun onClickUseMobileContinue()
}