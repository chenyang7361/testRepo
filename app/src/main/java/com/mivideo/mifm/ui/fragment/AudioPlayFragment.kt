package com.mivideo.mifm.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.mivideo.mifm.R
import com.mivideo.mifm.player.PlayerView
import com.mivideo.mifm.util.app.DisplayUtil
import com.mivideo.mifm.util.getColor
import kotlinx.android.synthetic.main.fragment_audio_play.*
import org.jetbrains.anko.onClick
import org.jetbrains.anko.support.v4.find
import qiu.niorgai.StatusBarCompat

/**
 * 音频播放Fragment
 *
 * Create by KevinTu on 2018/8/13
 */
class AudioPlayFragment : BaseFragment() {

    private lateinit var audioContainerView: FrameLayout
    private var audioControllerView: PlayerView? = null

    @JvmOverloads
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_audio_play, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioContainerView = find(R.id.audio_container)

        close_fragment.onClick {
            pop()
        }

        audioControllerView = PlayerView(context)
        audioContainerView.addView(audioControllerView)
        audioControllerView?.attachController(mediaManager)
    }

    override fun onEnterAnimationEnd(savedInstanceState: Bundle?) {
        super.onEnterAnimationEnd(savedInstanceState)
        activity?.let {
            DisplayUtil.setColor(activity as Activity, getColor(context, R.color.alpha_0_5_black), 0, true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.let {
            DisplayUtil.setStatusBarLightMode(it, true)
        }
        audioControllerView?.let {
            it.onRelease()
            audioContainerView.removeView(it)
        }
    }
}

fun createAudioPlayFragment(): AudioPlayFragment {
    return AudioPlayFragment()
}