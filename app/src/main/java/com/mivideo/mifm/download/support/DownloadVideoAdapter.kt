package com.mivideo.mifm.download.support

import com.mivideo.mifm.data.models.AudioInfo

abstract class DownloadVideoAdapter(var audioInfo: AudioInfo) : DownloadAdapter() {
    fun get(): AudioInfo {
        return audioInfo
    }
}