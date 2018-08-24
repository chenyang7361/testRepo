package com.mivideo.mifm.socialize.internel

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.text.TextUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.hwangjr.rxbus.RxBus
import com.hwangjr.rxbus.annotation.Subscribe
import com.mivideo.mifm.BuildConfig
import com.mivideo.mifm.R
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.share.ShareHelper
import com.mivideo.mifm.socialize.AuthResult
import com.mivideo.mifm.socialize.BaseSocializer
import com.mivideo.mifm.socialize.ShareResult
import com.mivideo.mifm.socialize.SocializeManager
import com.mivideo.mifm.socialize.exceptions.*
import com.mivideo.mifm.socialize.share.NewShareInfo
import com.mivideo.mifm.util.ApkUtil
import com.mivideo.mifm.util.app.showToast
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.*
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.BehaviorSubject
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/**
 * 微信社交化处理类
 * Created by yamlee on 17/05/2017.
 *
 * @author LiYan
 */
class WxSocializer(val context: Context) : BaseSocializer() {

    companion object {
        private const val MAX_SIZE_THUMBNAIL_BYTE = 1 shl 14
        private const val APP_ID = BuildConfig.WX_APPID
        private const val SCOPE = "snsapi_userinfo"
        private const val STATE = "video_daily"
        var api: IWXAPI? = null
    }

    private var authSubject: BehaviorSubject<AuthResult>? = null


    init {
        RxBus.get().register(this)
        api = WXAPIFactory.createWXAPI(context.applicationContext, APP_ID, true)
        api?.registerApp(APP_ID)
    }

    override fun isAppInstalled(): Observable<Boolean> {
        val applicationContext = context.applicationContext
        val observable = Observable.unsafeCreate<Boolean> {
            val wxInstalled = api?.isWXAppInstalled ?: false
            if (wxInstalled) {
                it.onNext(wxInstalled)
            } else {
                val apkResult = ApkUtil.isInstalled(applicationContext, ApkUtil.WX_PACKAGE)
                it.onNext(apkResult)
            }
            it.onCompleted()
        }
        return observable.compose(asyncSchedulers())
    }

    override fun loginAuth(): Observable<AuthResult> {
        return isAppInstalled()
                .flatMap { installed ->
                    if (!installed) {
                        val msg = context.getString(R.string.need_install_wx)
                        Observable.error<AuthResult>(SocialAppNotInstallException(msg))
                    } else {
                        authSubject = BehaviorSubject()
                        val req = SendAuth.Req()
                        req.scope = SCOPE
                        req.state = STATE
                        api?.sendReq(req)
                        authSubject!!
                    }
                }


    }

    override fun share(info: NewShareInfo): Observable<ShareResult> {
        return isAppInstalled()
                .flatMap { installed ->
                    if (!installed) {
                        val msg = context.getString(R.string.need_install_wx)
                        Observable.error<ShareResult>(SocialAppNotInstallException(msg))
                    } else {
                        shareSubject = BehaviorSubject()
                        mShareInfo = info
                        val isWX = (info.shareTo == NewShareInfo.WX)
                        var mediaObj: WXMediaMessage.IMediaObject? = null
                        if (info.shareContentType == NewShareInfo.SHARE_CONTENT_TYPE_TEXT) {
                            shareText(info)
                            return@flatMap shareSubject
                        } else if (!TextUtils.isEmpty(info.targetUrl)) {
                            mediaObj = WXWebpageObject()
                            if (isWX) {
                                mediaObj.webpageUrl = appendFromToTargetUrl(info.targetUrl, SocializeManager.FROM_WX)
                            } else {
                                mediaObj.webpageUrl = appendFromToTargetUrl(info.targetUrl, SocializeManager.FROM_WF)
                            }
                        } else if (!TextUtils.isEmpty(info.shareFilePath)) {
                            mediaObj = WXFileObject()
                            mediaObj.filePath = info.shareFilePath
                        }
                        val msg = WXMediaMessage(mediaObj)
                        msg.title = info.title
                        msg.description = info.description
                        val req = SendMessageToWX.Req()
                        if (isWX) {
                            req.scene = SendMessageToWX.Req.WXSceneSession
                        } else {
                            req.scene = SendMessageToWX.Req.WXSceneTimeline
                        }
                        req.transaction = buildTransaction("video")
                        if (TextUtils.isEmpty(info.shareImageUrl)) {
                            req.message = msg
                            api?.sendReq(req)
                        } else {
                            val videoId = info.shareExtensions.getString(ShareHelper.SHARE_EXT_ARG_VIDEO_ID)
                            Glide.with(context)
                                    .load(info.shareImageUrl)
                                    .asBitmap()
                                    .into(object : SimpleTarget<Bitmap>() {
                                        override fun onResourceReady(resource1: Bitmap, glideAnimation: GlideAnimation<in Bitmap>?) {

                                            val thumbBmp1 = Bitmap.createScaledBitmap(clipBitmap(resource1), 150, 150, true)
                                            var resultByte: ByteArray? = null
                                            if (TextUtils.isEmpty(videoId)) {
                                                resultByte = bmpToByteArray(thumbBmp1, true)
                                            } else {
                                                //videoId不为空，添加播放视频的水印
                                                val resource2 = BitmapFactory.decodeResource(context.resources, R.drawable.share_float_icon)
                                                val thumbBmp2 = Bitmap.createScaledBitmap(resource2, 70, 70, true)
                                                resultByte = bmpToByteArray(composeBitmap(thumbBmp1, thumbBmp2), true)
                                            }
                                            Timber.i("wx thumb result byte: ${resultByte.size}")
                                            msg.thumbData = resultByte

                                            req.message = msg
                                            api?.sendReq(req)
                                        }
                                    })
                        }
                        shareSubject
                    }
                }
    }

    private fun shareText(info: NewShareInfo) {
        val textObj = WXTextObject()
        val text = info.textContent
        val isWX = (info.shareTo == NewShareInfo.WX)

        textObj.text = text

        val msg = WXMediaMessage()
        msg.mediaObject = textObj
        msg.description = text

        val req = SendMessageToWX.Req()
        req.transaction = buildTransaction("text")

        req.message = msg
        if (isWX) {
            req.scene = SendMessageToWX.Req.WXSceneSession
        } else {
            req.scene = SendMessageToWX.Req.WXSceneTimeline
        }
        api?.sendReq(req)

    }


    /**
     * 裁剪图片
     */
    private fun clipBitmap(bitmap: Bitmap): Bitmap {
        var startX: Int
        var startY: Int
        var width: Int
        var height: Int
        if (bitmap.width > SocializeManager.SHARE_IMAGE_WIDTH) {
            startX = (bitmap.width - SocializeManager.SHARE_IMAGE_WIDTH) / 2
            width = SocializeManager.SHARE_IMAGE_WIDTH
        } else {
            startX = 0
            width = bitmap.width
        }
        if (bitmap.height > SocializeManager.SHARE_IMAGE_HEIGHT) {
            startY = (bitmap.height - SocializeManager.SHARE_IMAGE_HEIGHT) / 2
            height = SocializeManager.SHARE_IMAGE_HEIGHT
        } else {
            startY = 0
            height = bitmap.height
        }

        return Bitmap.createBitmap(bitmap, startX, startY, width, height)
    }

    private fun composeBitmap(bitmap1: Bitmap, bitmap2: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap1.width, bitmap1.height, bitmap1.config)
        val canvas = Canvas(result)
        canvas.drawBitmap(bitmap1, Matrix(), null)
        canvas.drawBitmap(bitmap2, 40.toFloat(), 40.toFloat(), null)
        canvas.drawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        return result
    }

    private fun buildTransaction(type: String?): String {
        return if (type == null) System.currentTimeMillis().toString() else type + System.currentTimeMillis()
    }

    private fun bmpToByteArray(bmp: Bitmap, needRecycle: Boolean): ByteArray {
        var resultBitmap: Bitmap = bmp
        Timber.i("bitmap count:" + resultBitmap.byteCount)
        if (bmp.byteCount > MAX_SIZE_THUMBNAIL_BYTE) {
            val scale = Math.sqrt(1.0 * bmp.byteCount / MAX_SIZE_THUMBNAIL_BYTE)
            val scaledW = (bmp.width / scale).toInt()
            val scaledH = (bmp.height / scale).toInt()
            resultBitmap = Bitmap.createScaledBitmap(bmp, scaledW, scaledH, true)
        }
        Timber.i("scaled bitmap count:" + resultBitmap.byteCount)

        val output = ByteArrayOutputStream()
        resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, output as OutputStream?)
        if (needRecycle) {
            bmp.recycle()
        }

        val result = output.toByteArray()
        try {
            output.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //微信分享不走onActivityResult,所以不用在此方法中实现
    }


    data class WxEntryOnReqEvent(val baseReq: BaseReq)

    /**
     * WxEntryActivity的onReq方法发送事件
     */
    @Subscribe
    fun onReq(baseReqEvent: WxEntryOnReqEvent) {
        //暂时没有需求
    }


    data class WxEntryOnRespEvent(val baseResp: BaseResp)

    /**
     * WxEntryActivity的onResp方法发送事件
     */
    @Subscribe
    fun onResp(respEvent: WxEntryOnRespEvent) {
        val resp = respEvent.baseResp
        if (resp is SendAuth.Resp) {
            handleAuthResp(resp)
        } else {
            handleShareResp(resp)
        }
    }

    private fun handleAuthResp(resp: SendAuth.Resp) {
        Timber.d("wxEntry auth resp:code: ${resp.code}," +
                "state:${resp.state},url:${resp.url}")

        when (resp.errCode) {
            BaseResp.ErrCode.ERR_OK -> {
                val authResult = AuthResult("", "", "", 0L, resp.code)
                authSubject?.onNext(authResult)
                authSubject?.onCompleted()
            }
            BaseResp.ErrCode.ERR_USER_CANCEL -> {
                authSubject?.onError(AuthCancelException(context.applicationContext))
            }
            BaseResp.ErrCode.ERR_AUTH_DENIED,
            BaseResp.ErrCode.ERR_SENT_FAILED,
            BaseResp.ErrCode.ERR_UNSUPPORT -> {
                val authErrorException = AuthErrorException()
                authErrorException.errMsg = resp.errStr
                authErrorException.errCode = resp.errCode.toString()
                authSubject?.onError(authErrorException)
            }
        }
    }

    private fun handleShareResp(resp: BaseResp) {
        var result = ""
        when (resp.errCode) {
            BaseResp.ErrCode.ERR_OK -> {
                result = context.resources.getString(R.string.share_toast_success)
                shareSubject.onNext(ShareResult(mShareInfo, NewShareInfo.WX))
                shareSubject.onCompleted()
            }
            BaseResp.ErrCode.ERR_USER_CANCEL -> {
                result = context.resources.getString(R.string.share_toast_cancel)
                shareSubject.onError(ShareCancelException(context.applicationContext, mShareInfo, NewShareInfo.WX))
            }
            BaseResp.ErrCode.ERR_AUTH_DENIED,
            BaseResp.ErrCode.ERR_SENT_FAILED,
            BaseResp.ErrCode.ERR_UNSUPPORT -> {
                result = context.resources.getString(R.string.share_toast_error)
                val shareError = ShareErrorException(mShareInfo, NewShareInfo.WX)
                shareError.errMsg = resp.errStr
                shareError.errCode = resp.errCode.toString()
                shareSubject.onError(shareError)
            }
        }
        showToast(context, result)
    }


    override fun release() {
        RxBus.get().unregister(this)
    }

}