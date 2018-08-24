package com.mivideo.mifm.player

/**
 * 播放器状态监听器
 * @author LiYan
 */
interface MediaPlayerListener {
    /**
     * 当播放器准备好播放后会回调此方法
     */
    fun onPrepared()

    /**
     * 当播放器接近播放结束时会回调此方法
     */
    fun onCompletion()

    /**
     * 视频缓存的进度回调
     *
     * @param percent 缓存进度
     */
    fun onBufferingUpdate(percent: Int)

    /**
     * 当seek到播放资源结尾时回调此方法
     */
    fun onSeekComplete()

    /**
     *视频资源尺寸信息改变回调
     * *
     * @param width     视频宽度
     * *
     * @param height    视频高度
     */
    fun onVideoSizeChanged(width: Int, height: Int)

    /**
     * 播放器错误回调方法
     *
     * @param what    错误对应的代码
     *        如      [.MEDIA_ERROR_UNKNOWN]
     *              [.MEDIA_ERROR_SERVER_DIED]
     * @param extra  辅助错误码，用于具体播放器实现中定义的错误码
     *
     * @return 如果onError处理了对应的错误会返回true，否则会返回false
     */
    fun onError(what: Int, extra: Int): Boolean

    /**
     * 当有打印或警告信息时回调此方法
     * @param what    信息类型
     *          * *  * [.MEDIA_INFO_UNKNOWN]
     * *  * [.MEDIA_INFO_VIDEO_TRACK_LAGGING]
     * *  * [.MEDIA_INFO_BUFFERING_START]
     * *  * [.MEDIA_INFO_BUFFERING_END]
     * *  * [.MEDIA_INFO_BAD_INTERLEAVING]
     * *  * [.MEDIA_INFO_NOT_SEEKABLE]
     * *  * [.MEDIA_INFO_METADATA_UPDATE]
     * *  * [.MEDIA_INFO_HARDWARE_DECODER]
     * *  * [.MEDIA_INFO_SOFTWARE_DECODER]
     *  @param extra  辅助错误码，用于具体播放器实现中定义
     *
     *  @return 如果onInfo处理了对应的信息会返回true，否则会返回false
     */
    fun onInfo(what: Int, extra: Int): Boolean
}