package com.mivideo.mifm.socialize.exceptions

/**
 * 分享app没有安装异常
 * @author LiYan
 */
class SocialAppNotInstallException(val msg: String) : Throwable() {
    override val message: String?
        get() = msg
}