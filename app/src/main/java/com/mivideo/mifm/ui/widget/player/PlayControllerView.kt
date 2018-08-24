package com.mivideo.mifm.ui.widget.player

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.AudioInfo
import com.mivideo.mifm.ui.OnClickFastListener
import com.mivideo.mifm.ui.widget.DownloadView
import com.mivideo.mifm.ui.widget.MiniPlayerView

/**
 * Create by KevinTu on 2018/8/14
 */
class PlayControllerView : RelativeLayout {

    companion object {
        const val SEEK_BAR_MAX_PROGRESS = 1000
    }

    private val seekBar: SeekBar by lazy {
        findViewById<SeekBar>(R.id.audio_controller_seek_bar)
    }
    private val currentTimeView: TextView by lazy {
        findViewById<TextView>(R.id.audio_controller_current_time_view)
    }
    private val durationView: TextView by lazy {
        findViewById<TextView>(R.id.audio_controller_duration_view)
    }
    private val autoOffBtn: ImageView by lazy {
        findViewById<ImageView>(R.id.auto_off_btn)
    }
    private val downloadView: DownloadView by lazy {
        findViewById<DownloadView>(R.id.download_btn)
    }
    private val audioLastBtn: ImageView by lazy {
        findViewById<ImageView>(R.id.audio_last_btn)
    }
    private val audioNextBtn: ImageView by lazy {
        findViewById<ImageView>(R.id.audio_next_btn)
    }
    private val audioPlayAndPauseBtn: MiniPlayerView by lazy {
        findViewById<MiniPlayerView>(R.id.audio_play_pause_btn)
    }

    private var audioInfo: AudioInfo? = null
    private var listener: ControllerListener? = null

    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.layout_audio_controller, this, true)

        init()
    }

    fun init() {
        seekBar.max = SEEK_BAR_MAX_PROGRESS
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                listener?.onSeekStart()
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                listener?.onSeeking(progress, fromUser)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                listener?.onSeekEnd()
            }
        })

        autoOffBtn.setOnClickListener(object : OnClickFastListener() {
            override fun onFastClick(view: View?) {
                listener?.autoOff()
            }
        })
        audioLastBtn.setOnClickListener(object : OnClickFastListener() {
            override fun onFastClick(view: View?) {
                listener?.playLast()
            }
        })
        audioNextBtn.setOnClickListener(object : OnClickFastListener() {
            override fun onFastClick(view: View?) {
                listener?.playNext()
            }
        })
        audioPlayAndPauseBtn.setCanControl()
        audioPlayAndPauseBtn.setOnClickListener(object : OnClickFastListener() {
            override fun onFastClick(view: View?) {
                listener?.playOrPause()
            }
        })
    }

    fun setControllerListener(listener: ControllerListener) {
        this.listener = listener
    }

    fun refreshView(audioInfo: AudioInfo) {
        this.audioInfo = audioInfo
        durationView.text = audioInfo.passageItem.duration
        downloadView.setCurrentAudioInfo(audioInfo)
    }

    fun updateLastBtnStatus(canClick: Boolean) {
        audioLastBtn.isClickable = canClick
        if (canClick) {
            audioLastBtn.alpha = 1F
        } else {
            audioLastBtn.alpha = 0.4F
        }
    }

    fun updateNextBtnStatus(canClick: Boolean) {
        audioNextBtn.isClickable = canClick
        if (canClick) {
            audioNextBtn.alpha = 1F
        } else {
            audioNextBtn.alpha = 0.4F
        }
    }

    fun updatePlayerView(isPlaying: Boolean) {
        if (isPlaying) {
            audioPlayAndPauseBtn.switchToPlay()
        } else {
            audioPlayAndPauseBtn.switchToPause()
        }
    }

    fun setCurrentPlayTime(timeStr: String) {
        currentTimeView.text = timeStr
    }

    fun renderSeekLayout(seekBarProgress: Int, bufferingProgress: Int, durationText: String, positionText: String) {
        seekBar.progress = seekBarProgress
        seekBar.secondaryProgress = bufferingProgress
        durationView.text = durationText
        currentTimeView.text = positionText
    }
}

interface ControllerListener {
    /**
     * 定时关闭
     */
    fun autoOff()

    /**
     * 播放上一个音频
     */
    fun playLast()

    /**
     * 播放上一个音频
     */
    fun playNext()

    /**
     * 播放 / 暂停
     */
    fun playOrPause()

    /**
     * 滑动开始
     */
    fun onSeekStart()

    /**
     * 滑动
     */
    fun onSeeking(progress: Int, fromUser: Boolean)

    /**
     * 滑动结束
     */
    fun onSeekEnd()
}