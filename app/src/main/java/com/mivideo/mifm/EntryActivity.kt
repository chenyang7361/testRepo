package com.mivideo.mifm

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import timber.log.Timber

/**
 * API 入口
 */
class EntryActivity : BaseActivity(), KodeinInjected {

    override val injector = KodeinInjector()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("PPP", "EntryActivity.onCreate|" + savedInstanceState)
        inject(appKodein())
        if (!handleIntent(intent)) {
            finish()
        }
    }

    private fun handleIntent(intent: Intent?): Boolean {
        if (intent == null) return false

        val data = intent.data ?: return false

        val scheme = data.scheme
        val host = data.host
        if (!TextUtils.equals(scheme, BuildConfig.SCHEME)) return false

        if (TextUtils.equals(host, "play")) {
            return handlePlay(data)
        } else if (TextUtils.equals(host, "launch")) {
            return handleLaunch(data)
        }

        return false
    }

    private fun handleLaunch(data: Uri): Boolean {
        return true
    }

    private fun handlePlay(data: Uri): Boolean {
        val url = data.getQueryParameter("url")
        startActivity(PlayerTestActivity.getLaunchIntent(applicationContext, url))
        finish()
        return true
    }
}