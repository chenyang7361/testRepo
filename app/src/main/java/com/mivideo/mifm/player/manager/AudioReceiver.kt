package com.mivideo.mifm.player.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Created by Jiwei Yuan on 18-8-20.
 */
class AudioReceiver : BroadcastReceiver() {
    val mediaManager: MediaManager = MediaManager.getInstance()

    companion object {
        const val ACTION_PAUSE: String = "com.mivideo.mifm.notify.pause"
        const val ACTION_PLAY: String = "com.mivideo.mifm.notify.play"
        const val ACTION_NEXT: String = "com.mivideo.mifm.notify.next"
        const val ACTION_LAST: String = "com.mivideo.mifm.notify.last"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            return
        }
        val action = intent.action
        when (action) {
            ACTION_NEXT -> {
                mediaManager.playNext()
            }
            ACTION_LAST -> {
                mediaManager.playLast()
            }
            ACTION_PAUSE -> {
                mediaManager.pause()
            }
            ACTION_PLAY -> {
                mediaManager.start()
            }
        }
    }

}