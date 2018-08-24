package com.mivideo.mifm.socialize

import android.app.Activity
import android.content.Intent
import com.mivideo.mifm.socialize.share.NewShareInfo
import rx.Observable

/**
 * 第三方社交化(如微信，QQ,微博)SDK抽象接口,主要包括登录，分享等
 *
 * Created by yamlee on 15/05/2017.
 * @author LiYan
 */
interface Socializer {

    companion object {
        const val DEFAULT_REQUEST_CODE = 11
    }

    /**
     * 判断指定第三方社交化平台app是否已经安装
     */
    fun isAppInstalled(): Observable<Boolean>

    /**
     * 登录授权
     */
    fun loginAuth(): Observable<AuthResult>

    /**
     * 分享信息
     */
    fun share(info: NewShareInfo): Observable<ShareResult>

    /**
     * 处理各种操作之后sdk对app的回调
     */
    fun handleActivityResult(requestCode: Int = DEFAULT_REQUEST_CODE,
                             resultCode: Int = Activity.RESULT_OK,
                             data: Intent?)

    /**
     * 处理onNewIntent回调
     */
    fun handleNewIntent(intent: Intent?)


    /**
     * 清除引用资源，释放内存，避免内存泄露
     */
    fun release()


}