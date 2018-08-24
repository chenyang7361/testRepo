package com.mivideo.mifm.ui.fragment

import android.compact.utils.IntentCompactUtil
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.R
import com.mivideo.mifm.player.manager.MediaManager
import kotlinx.android.synthetic.main.fragment_eula.*
import org.jetbrains.anko.onClick

class PlayerTestFragment : BaseFragment() {

    companion object {
        const val ARG_URL = "url"
    }


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        simpleTitleBar.backLayout!!.onClick {
            activity?.finish()
        }
        val intent = arguments?.get("intent") as Intent
        val playUrl = IntentCompactUtil.getSegment(intent, ARG_URL)
        Log.d("PPP", "PlayerTestFragment|playUrl|" + playUrl)
        play(playUrl)
    }

    fun play(url: String) {
//        mediaManager.playNewUrl(url)
//        mediaManager.testVideoPlay()
    }
}
