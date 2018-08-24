package com.mivideo.mifm

import android.compact.utils.IntentCompactUtil
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.mivideo.mifm.ui.fragment.PlayerTestFragment
import com.mivideo.mifm.util.SystemUtil
import com.mivideo.mifm.util.app.DisplayUtil

class PlayerTestActivity : BaseActivity() {

    private var playerTestFragment : PlayerTestFragment? = null

    companion object {

        const val ARG_URL = "url"

        fun getLaunchIntent(ctx: Context, url: String): Intent {
            val intent = Intent(ctx, PlayerTestActivity::class.java)
            intent.putExtra(ARG_URL, url)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            Log.d("PPP", "PlayerTestActivity|getLaunchIntent|" + IntentCompactUtil.convertIntentToString(intent))
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SystemUtil.isCanChangeStatusBarSystem()) {
            // 默认MIUI系统StatusBar字的颜色为黑色
            DisplayUtil.setStatusBarLightMode(this, true)
        }

        val routerUrl = IntentCompactUtil.getSegment(intent, ARG_URL)

        Log.d("PPP", "PlayerTestActivity|routerUrl|" + routerUrl)

        playerTestFragment = createPlayerTestFragment()
        if (playerTestFragment?.arguments == null) {
            playerTestFragment?.arguments = Bundle()
        }
        playerTestFragment?.arguments?.putParcelable("intent", intent)
        loadRootFragment(android.R.id.content, playerTestFragment!!)
    }
}

fun createPlayerTestFragment(): PlayerTestFragment {
    return PlayerTestFragment()
}