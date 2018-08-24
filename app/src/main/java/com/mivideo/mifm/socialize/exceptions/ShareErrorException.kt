package com.mivideo.mifm.socialize.exceptions

import com.mivideo.mifm.socialize.share.NewShareInfo


/**
 * 第三方分享失败会抛出此异常，需要捕获
 * Created by yamlee on 22/05/2017.
 *
 * @author LiYan
 */
class ShareErrorException(val errorInfo: NewShareInfo?, private val target: String) : Throwable() {
    var errMsg: String = "Default share error msg!"
    var errCode: String = "Default share error code!"
    var errDetail: String = "Default share error detail!"
}