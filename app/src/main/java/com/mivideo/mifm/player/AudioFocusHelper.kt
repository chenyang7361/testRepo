package com.mivideo.mifm.player

import android.annotation.SuppressLint
import android.arch.lifecycle.Lifecycle
import android.content.Context
import android.media.AudioManager
import timber.log.Timber

/**
 * 声音流焦点获取工具类
 * @author LiYan
 */
class AudioFocusHelper private constructor(private val mContext: Context) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: AudioFocusHelper? = null

        fun get(context: Context): AudioFocusHelper {
            if (instance == null) {
                synchronized(AudioFocusHelper::class.java) {
                    if (instance == null) {
                        instance = AudioFocusHelper(context.applicationContext)
                    }
                }
            }
            return instance!!

        }
    }

    private var mAm: AudioManager? = null
    private var mVideoPausedByAudoFocusLoss = false

    init {
        mAm = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
    }

    /**
     *  AUDIOFOCUS_GAIN：你已经获得音频焦点；
     *
     *  AUDIOFOCUS_LOSS：你已经失去音频焦点很长时间了，必须终止所有的音频播放。
     *  因为长时间的失去焦点后，不应该在期望有焦点返回，这是一个尽可能清除不用资源的好位置。例如，应该在此时释放MediaPlayer对象；
     *
     *  AUDIOFOCUS_LOSS_TRANSIENT：这说明你临时失去了音频焦点，但是在不久就会再返回来。
     *  此时，你必须终止所有的音频播放，但是保留你的播放资源，因为可能不久就会返回来。
     *
     *  AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK：这说明你已经临时失去了音频焦点，但允许你安静的播放音频（低音量），
     *  而不是完全的终止音频播放。目前所有的情况下，oFocusChange的时候停止mediaPlayer
     *
     */
    private var afChangeListener: AudioManager.OnAudioFocusChangeListener = object : AudioManager.OnAudioFocusChangeListener {
        override fun onAudioFocusChange(focusChange: Int) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                Timber.i("短时间失去声音焦点")
                //失去声音焦点，自动暂停，暂时没有这个需求
                //pause()
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                Timber.i("获取声音焦点")
                val controller = KPlayerManager.get(mContext).getCurrentPlayer()?.getController()
                val isResumed = controller?.mLifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true
                //获取声音焦点且当前界面正在显示，恢复播放
                if (isResumed && controller?.isPlaying() == true && mVideoPausedByAudoFocusLoss) {
                    //todo 音乐audio focus恢复监听
                    controller.resume()
                    mVideoPausedByAudoFocusLoss = false
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                Timber.i("长时间失去声音焦点")
                val controller = KPlayerManager.get(mContext).getCurrentPlayer()?.getController()
                //失去声音焦点，恢复播放
                if (controller?.isPlaying() == true) {
                    controller.pause()
                    mVideoPausedByAudoFocusLoss = true
                }
            }
        }
    }

    /**
     * 获取声音焦点
     */
    fun requestAudioFocus(): Boolean {
        Timber.i("requestAudioFocus")
        if (mAm == null) {
            mAm = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        }
        // Request audio focus for playback
        val result = mAm?.requestAudioFocus(afChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN)
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    /**
     * 释放声音焦点
     */
    fun releaseAudioFocus() {
        mAm?.abandonAudioFocus(afChangeListener)
    }
}