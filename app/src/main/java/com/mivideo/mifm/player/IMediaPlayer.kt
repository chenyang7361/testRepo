package com.mivideo.mifm.player

import android.view.Surface
import android.view.SurfaceHolder
import java.io.IOException

/**
 * 播放器通用规范接口
 * Created by xingchang on 16/11/30.
 * @author LiYan
 */

interface IMediaPlayer {

    /**
     * 该视频源是否支持pause操作
     */
    val canPause: Boolean

    /**
     * 该视频源是否支持向后Seek操作
     */
    val canSeekBackward: Boolean

    /**
     * 该视频源是否支持向前seek操作
     */
    val canSeekForward: Boolean

    /**
     * 该视频源是否支持缓冲，默认true
     */
    val canBuffering: Boolean

    /**
     * 当前视频播放所在的进度，单位毫秒（ms）
     */
    val currentPosition: Int

    /**
     * 视频总时长，单位毫秒（ms）
     */
    val duration: Int

    /**
     * 视频高度
     */
    val videoHeight: Int

    /**
     * 视频宽度
     */
    val videoWidth: Int

    /**
     * 播放器是否正在播放
     */
    val isPlaying: Boolean

    /**
     * 播放器缓存进度,0-100
     */
    val bufferPercentage: Int

    /**
     * 循环播放视频
     */
    var loopPlayVideo:Boolean

    /**
     * 设置播放器数据源
     */
    @Throws(IOException::class,
            IllegalArgumentException::class,
            SecurityException::class,
            IllegalStateException::class)
    fun setDataSource(dataSource: DataSource)

    fun setDisplay(surfaceHolder: SurfaceHolder)

    fun setSurface(surface: Surface?)

    /**
     * 设置音量,范围从0.0-1.0
     *
     */
    fun setVolume(leftVolume: Float, rightVolume: Float)

    /**
     * 播放器异步准备方法
     *
     * 在设置了dataSource和surface后需要调用prepare()或者prepareAsync()方法
     * prepare()方会阻塞当前线程，如果源文件是File文件类型可以直接调用prepare，
     * 而如果dataSource是一个网络连接，是一个流，建议使用prepareAsync方法，
     * prepareAsync()是异步方法
     */
    @Throws(IllegalStateException::class)
    fun prepareAsync()

    /**
     * 播放器准备方法
     *
     * 在设置了dataSource和surface后需要调用prepare()或者prepareAsync()方法
     * prepare()方会阻塞当前线程，如果源文件是File文件类型可以直接调用prepare，
     * 而如果dataSource是一个网络连接，是一个流，建议使用prepareAsync方法，
     * prepareAsync()是异步方法
     */
    @Throws(IOException::class, IllegalStateException::class)
    fun prepare()


    /**
     * 开始播放
     */
    @Throws(IllegalStateException::class)
    fun start()

    /**
     * 停止播放
     */
    @Throws(IllegalStateException::class)
    fun stop()

    /**
     * 暂停播放
     */
    @Throws(IllegalStateException::class)
    fun pause()

    /**
     *将播放器重置为初始状态
     *
     * 调用此方法后，需要再次调用setDataSource和prepare方法才能再播放视频
     */
    fun reset()

    /**
     * 跳转到视频指定时间点位置处播放
     */
    @Throws(IllegalStateException::class)
    fun seekTo(ms: Int)

    /**
     * 设置当播放视频时是否一直保持屏幕常亮
     */
    fun setScreenOnWhilePlaying(screenOn: Boolean)

    /**
     * 设置播放器监听器
     */
    fun setMediaPlayerListener(listener: MediaPlayerListener)

    /**
     * 播放器资源释放方法，建议在对应Android组件生命周期销毁时调用
     * 如 Activity.onDestroy()方法
     */
    fun release()

    /**
     * 错误码转换器，用于统一不同播放器错误码
     */
    fun errorCodeMapper():ErrorCodeMapper

    /**
     * 倍速播放功能
     */
    fun speedUp(speed:Float)
}