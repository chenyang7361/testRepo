package com.mivideo.mifm.socialize.exceptions

/**
 * 第三方登录失败会抛出此异常，需要捕获
 * Created by yamlee on 22/05/2017.
 *
 * @author LiYan
 */
class AuthErrorException : Throwable() {
    var errMsg: String = "Default auth error msg!"
    var errCode: String = "Default auth error code!"
    var errDetail: String = "Default auth error detail!"
}