package com.mivideo.mifm.data.models.jsondata.common

import com.mivideo.mifm.data.models.AudioInfo

class CommonVideoCache() {
    private var key: String = ""
    private var url: String? = ""
    private var path: String? = ""
    private var progress = 0
    private var completeSize: Long = 0L
    private var totalSize: Long = 0L
    private var appendSize: Long = 0L
    private var state = 0
    private var commonVideo: CommonVideo? = null
    private var audioInfo: AudioInfo? = null
    private var autoStart = false
    private var clicked = false
    private var failReason: String = ""
    private var manage = false
    private var delete = false
    private var errorcode = 0

    constructor(video: CommonVideo): this() {
        commonVideo = video
    }

    constructor(audioInfo: AudioInfo): this() {
        this.audioInfo = audioInfo
    }

    fun getKey(): String {
        return key
    }

    fun setKey(k: String) {
        key = k
    }

    fun getUrl(): String? {
        return url
    }

    fun setUrl(u: String?) {
        url = u
    }

    fun getPath(): String? {
        return path
    }

    fun setPath(p: String?) {
        path = p
    }

    fun getProgress(): Int {
        return progress
    }

    fun setProgress(pro: Int) {
        progress = pro
    }

    fun getCompleteSize(): Long {
        return completeSize
    }

    fun setCompleteSize(size: Long) {
        completeSize = size
    }

    fun getTotalSize() : Long {
        return totalSize
    }

    fun setTotalSize(size: Long) {
        totalSize = size
    }

    fun getAppendSize(): Long {
        return appendSize
    }

    fun setAppendSize(size: Long) {
        appendSize = size
    }

    fun getState(): Int {
        return state
    }

    fun setState(st: Int) {
        state = st
    }

    fun getCommonVideo(): CommonVideo? {
        return commonVideo
    }

    fun setCommonVideo(video: CommonVideo) {
        commonVideo = video
    }

    fun getAudioInfo(): AudioInfo? {
        return audioInfo
    }

    fun setAudioInfo(audioInfo: AudioInfo) {
        this.audioInfo = audioInfo
    }

    fun getAutoStart(): Boolean {
        return autoStart
    }

    fun setAutoStart(auto: Boolean) {
        this.autoStart = auto
    }

    fun getClicked(): Boolean {
        return clicked
    }

    fun setClicked(click: Boolean) {
        this.clicked = click
    }

    fun getFailReason(): String {
        return failReason
    }

    fun setFailReason(reason: String) {
        this.failReason = reason
    }

    fun getManage(): Boolean {
        return manage
    }

    fun setManage(man: Boolean) {
        manage = man
    }

    fun getDelete(): Boolean {
        return delete
    }

    fun setDelete(del: Boolean) {
        delete = del
    }

    private var msg: String = ""

    fun getMsg(): String? {
        return msg
    }

    fun setMsg(m: String) {
        this.msg = m
    }

    private var title: String = ""

    fun getTitle(): String? {
        return title
    }

    fun setTitle(t: String) {
        this.title = t
    }

    fun getErrorCode(): Int {
        return errorcode
    }

    fun setErrorCode(code: Int) {
        errorcode = code
    }

    fun getVid(): String {
        if (commonVideo != null && commonVideo!!.video_id != null) {
            return commonVideo!!.video_id
        }
        return ""
    }
}