package com.mivideo.mifm.account

import rx.Observable

/**
 * 用户登录认证器抽象接口
 * Created by yamlee on 24/05/2017.
 *
 * @author LiYan
 */
interface UserAuthenticator {
    /**
     * 发起登录验证
     */
    fun startLoginAuth(): Observable<AccountInfo>

    /**
     * 发起登出
     */
    fun logout(): Observable<Boolean>
}