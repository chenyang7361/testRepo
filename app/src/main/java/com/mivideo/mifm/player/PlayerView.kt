package com.mivideo.mifm.player

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.AudioInfo
import com.mivideo.mifm.ui.widget.player.AudioAlbumView
import com.mivideo.mifm.ui.widget.player.AudioCoverView
import com.mivideo.mifm.ui.widget.player.ControllerListener
import com.mivideo.mifm.ui.widget.player.PlayControllerView
import org.jetbrains.anko.onClick

/**
 * 视频播放控件
 *
 * Create by KevinTu on 2018/8/14
 */
class PlayerView : RelativeLayout, AudioControllerView {

    private val coverView: AudioCoverView by lazy {
        findViewById<AudioCoverView>(R.id.cover)
    }
    private val titleView: TextView by lazy {
        findViewById<TextView>(R.id.audio_title)
    }
    private val playCountView: TextView by lazy {
        findViewById<TextView>(R.id.play_count)
    }
    private val updateTimeView: TextView by lazy {
        findViewById<TextView>(R.id.update_time)
    }
    private val albumView: AudioAlbumView by lazy {
        findViewById<AudioAlbumView>(R.id.album_view)
    }
    private val controllerView: PlayControllerView by lazy {
        findViewById<PlayControllerView>(R.id.controller_layout)
    }

    private var audioController: AudioController? = null
    private var currentAudio: AudioInfo? = null

    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.layout_audio_play, this, true)

        init()
    }

    fun init() {
        albumView.onClick {
            android.widget.Toast.makeText(context, "专辑点击事件", 0.toInt()).show()
        }

        controllerView.setControllerListener(object : ControllerListener {
            override fun autoOff() {
                audioController?.speedUp(1.5f)
            }

            override fun playLast() {
                audioController?.playLast()
            }

            override fun playNext() {
                audioController?.playNext()
            }

            override fun playOrPause() {
                audioController?.let {
                    if (it.isPlaying()) {
                        it.pause()
                    } else {
                        it.start()
                    }
                }
            }

            override fun onSeekStart() {
                audioController?.onSeekStart()
            }

            override fun onSeeking(progress: Int, fromUser: Boolean) {
                audioController?.onSeeking(progress, fromUser)
            }

            override fun onSeekEnd() {
                audioController?.onSeekEnd()
            }
        })
    }

    fun attachController(audioController: AudioController) {
        this.audioController = audioController
        audioController.attachControllerView(this)
    }

    fun onRelease() {
        audioController?.dettachControllerView()
    }

    override fun updateCurrentAudio(audioInfo: AudioInfo) {
        currentAudio = audioInfo
        titleView.text = audioInfo.passageItem.name
        updateTimeView.text = resources.getString(R.string.audio_play_update_time_str, audioInfo.albumInfo.updated_at)
        coverView.refreshView(audioInfo)
        albumView.refreshView(audioInfo)
        controllerView.refreshView(audioInfo)
    }

    override fun updateLastBtnStatus(canClick: Boolean) {
        controllerView.updateLastBtnStatus(canClick)
    }

    override fun updateNextBtnStatus(canClick: Boolean) {
        controllerView.updateNextBtnStatus(canClick)
    }

    override fun showPlayBtn(isPlaying: Boolean) {
        controllerView.updatePlayerView(isPlaying)
    }

    override fun renderPlayTimeText(timeStr: String) {
        controllerView.setCurrentPlayTime(timeStr)
    }

    override fun renderSeekLayout(seekBarProgress: Int, bufferingProgress: Int, durationText: String, positionText: String) {
        controllerView.renderSeekLayout(seekBarProgress, bufferingProgress, durationText, positionText)
    }

    override fun showNoNetworkLayout() {
    }

    override fun hideNoNetworkLayout() {
    }

    override fun showUseMobileNetLayout() {
    }

    override fun hideUseMobileNetLayout() {
    }

    override fun showLoadingView() {
    }

    override fun hideLoadingView() {
    }

    override fun showErrorView(message: String) {
    }

    override fun hideErrorView() {
    }

    override fun onInfo(what: Int, extra: Int): Boolean {
        return false
    }

    override fun onSeekComplete() {
    }

    override fun onPrepared() {
    }

    override fun onCompletion() {
    }
}