package com.mivideo.mifm.socialize

import android.content.Intent
import com.mivideo.mifm.socialize.share.NewShareInfo
import rx.Observable
import rx.lang.kotlin.BehaviorSubject

/**
 * 社交工具父类
 * Created by yamlee on 19/05/2017.
 * @author LiYan
 */
abstract class BaseSocializer : Socializer {
    protected var shareSubject = BehaviorSubject<ShareResult>()

    protected var mShareInfo: NewShareInfo? = null

    override fun share(info: NewShareInfo): Observable<ShareResult> {
        return shareSubject
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    }

    override fun handleNewIntent(intent: Intent?) {
    }

    override fun release() {

    }

    protected fun appendFromToTargetUrl(targetUrl: String, from: String): String {
        var result = targetUrl
        if (targetUrl.contains("?")) {
            result = targetUrl + "&" + from
        } else {
            result = targetUrl + "?" + from
        }
        return result
    }
}