package com.mivideo.mifm.socialize

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import java.io.File
import java.util.*

/**
 * 社会化分享工具:微信好友,微信朋友圈,微博,qq空间(必须已经安装qq空间客户端),qq好友
 */
object ShareUtils {

    /**
     * 微信主包名
     */
    private val WEIXIN_PACKAGE = "com.tencent.mm"
    /**
     * 发送图片给微信好友
     */
    private val WEIXIN_SHARE_IMAGE_FRIEND = "com.tencent.mm.ui.tools.ShareImgUI"
    /**
     * 微信朋友圈
     */
    private val WEIXIN_TIME_LINE = "com.tencent.mm.ui.tools.ShareToTimeLineUI"
    /**
     * 图片MIME
     */
    private val MIME_IMAGE = "image/*"
    /**
     * 微信朋友圈图片
     */
    private val WEIXIN_TIME_LINE_CONTENT = "Kdescription"
    /**
     * 文本MIME
     */
    private val MIME_TEXT = "text/plain"
    /**
     * 新浪主包名
     */
    private val SINA_PACKAGE = "com.sina.weibo"
    /**
     * 新浪多图分享
     */
    private val SINA_DISPATCH = "com.sina.weibo.composerinde.ComposerDispatchActivity"
    /**
     * qq空间主包名
     */
    private val QZONE_PACKAGE = "com.qzone"
    /**
     * qq空间图片分享
     */
    private val QZONE_IMAGE = "com.qzonex.module.operation.ui.QZonePublishMoodActivity"
    /**
     * qq主包名
     */
    private val QQ_PACKAGE = "com.tencent.mobileqq"
    /**
     * qq发送到分享
     */
    private val QQ_JUMP = "com.tencent.mobileqq.activity.JumpActivity"

    private val QQ_ZONE_JUMP = "com.tencent.mobileqq.cooperation.qzone.share.QZoneShareActivity"


    /**
     * 检测apk是否已安装
     *
     * @param pkgName 检查的主包名
     * @param context 当前上下文
     * @return true 表示已经安装；false表示未安装
     */
    fun isPkgInstalled(pkgName: String, context: Context): Boolean {
        synchronized(ShareUtils::class.java) {
            // 分析 Package manager has died http://www.lai18.com/content/2402015.html
            try {
                val packageInfo = context.packageManager.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                return false
            }

        }
    }

    /**
     * 分享图片给微信好友
     *
     * @param content 内容
     * @param context 当前上下文
     */
    fun shareToWeiXinFriend(content: String, context: Context) {
        val intent = Intent()
        val comp = ComponentName(WEIXIN_PACKAGE, WEIXIN_SHARE_IMAGE_FRIEND)
        intent.component = comp
        intent.action = Intent.ACTION_SEND
        intent.type = MIME_TEXT
        //        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        intent.putExtra(Intent.EXTRA_TEXT, content)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.applicationContext.startActivity(intent)
    }

    /**
     * 分享图片给微信好友
     *
     * @param file    文件
     * @param context 当前上下文
     */
    fun shareToWeiXinFriend(file: File, context: Context) {
        val intent = Intent()
        val comp = ComponentName(WEIXIN_PACKAGE, WEIXIN_SHARE_IMAGE_FRIEND)
        intent.component = comp
        intent.action = Intent.ACTION_SEND
        intent.type = MIME_IMAGE
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.applicationContext.startActivity(intent)
    }

    /**
     * 分享多图到朋友圈,多张图片加文字
     *
     * @param content 分享图片描述
     * @param uris    文件
     * @param context 当前上下文
     */
    fun shareToWeiXinTimeLine(content: String, uris: ArrayList<Uri>, context: Context) {
        val intent = Intent()
        val comp = ComponentName(WEIXIN_PACKAGE, WEIXIN_TIME_LINE)
        intent.component = comp
        intent.action = Intent.ACTION_SEND_MULTIPLE
        intent.type = MIME_IMAGE
        intent.putExtra(WEIXIN_TIME_LINE_CONTENT, content)
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.applicationContext.startActivity(intent)
    }

    /**
     * 分享到新浪微博文本
     *
     * @param context 当前上下文
     * @param content 分享内容
     */
    fun shareToSinaText(content: String, context: Context) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, content)
        sendIntent.type = MIME_TEXT
        // 直接去新浪
        sendIntent.`package` = SINA_PACKAGE
        sendIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.applicationContext.startActivity(sendIntent)
    }


    /**
     * 分享多图到新浪微博,多张图片加文字
     *
     * @param content 分享内容
     * @param uris    文件
     * @param context 当前上下文
     */
    fun shareToSinaImage(content: String, uris: ArrayList<Uri>, context: Context) {
        val intent = Intent()
        val comp = ComponentName(SINA_PACKAGE, SINA_DISPATCH)
        intent.component = comp
        intent.action = Intent.ACTION_SEND_MULTIPLE
        intent.type = MIME_IMAGE
        intent.putExtra(Intent.EXTRA_TEXT, content)
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            context.applicationContext.startActivity(intent)
        } catch (e: ClassCastException) {
            // Key android.intent.extra.TEXT expected ArrayList<CharSequence> but value was a java.lang.String.  The default value <null> was returned.
            e.printStackTrace()
        }

    }

    /**
     * qq空间文本分享
     *
     * @param content 分享文本
     * @param context 当前上下文
     */
    fun shareToQzoneText(content: String, context: Context) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, content)
        sendIntent.type = MIME_TEXT
        // 直接去qq空间
        sendIntent.`package` = QQ_ZONE_JUMP
        sendIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.applicationContext.startActivity(sendIntent)
    }

    /**
     * 分享多图到qq空间,多张图片加文字
     *
     * @param content 分享文本
     * @param uris    文件
     * @param context 当前上下文
     */
    fun shareToQzoneImage(content: String, uris: ArrayList<Uri>, context: Context) {
        val intent = Intent()
        val comp = ComponentName(QZONE_PACKAGE, QZONE_IMAGE)
        intent.component = comp
        intent.action = Intent.ACTION_SEND_MULTIPLE
        intent.type = MIME_IMAGE
        intent.putExtra(Intent.EXTRA_TEXT, content)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        try {
            context.applicationContext.startActivity(intent)
        } catch (e: ClassCastException) {
            // Key android.intent.extra.TEXT expected ArrayList<CharSequence> but value was a java.lang.String.  The default value <null> was returned.
            e.printStackTrace()
        }

    }

    /**
     * 分享到qq多张图片
     *
     * @param uris    分享多图片
     * @param context 当前上下文
     */
    fun shareToQQImage(uris: ArrayList<Uri>, context: Context) {
        val intent = Intent()
        val comp = ComponentName(QQ_PACKAGE, QQ_JUMP)
        intent.component = comp
        intent.action = Intent.ACTION_SEND_MULTIPLE
        intent.type = MIME_IMAGE
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        try {
            context.applicationContext.startActivity(intent)
        } catch (e: ClassCastException) {
            // Key android.intent.extra.TEXT expected ArrayList<CharSequence> but value was a java.lang.String.  The default value <null> was returned.
            e.printStackTrace()
        }

    }

    /**
     * 分享到qq文本
     *
     * @param content 分享文本内容
     * @param context 当前上下文
     */
    fun shareToQQText(content: String, context: Context) {
        val sendIntent = Intent()
        val comp = ComponentName(QQ_PACKAGE, QQ_JUMP)// 发送到
        sendIntent.component = comp
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, content)
        sendIntent.type = MIME_TEXT
        sendIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.applicationContext.startActivity(sendIntent)
    }
}
