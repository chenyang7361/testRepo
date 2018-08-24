package com.mivideo.mifm.ui.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.salomonbrys.kodein.instance
import com.hwangjr.rxbus.annotation.Subscribe
import com.mivideo.mifm.R
import com.mivideo.mifm.RouterConf
import com.mivideo.mifm.account.UserAccountManager
import com.mivideo.mifm.events.MediaCompleteEvent
import com.mivideo.mifm.events.MediaPreparedEvent
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.mine_fragment.*
import kotlinx.android.synthetic.main.subscribe_title_layout.*
import org.jetbrains.anko.onClick

@Route(path = RouterConf.PATH_MINE)
class MineFragment : TabFragment() {

    companion object {
        val MINE_FRAGMENT = "mineFragment"
        val MINE_TAB = "mine"
    }

    private val userAccountManager: UserAccountManager by instance()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.mine_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ARouter.getInstance().inject(this)
        left.setImageResource(R.drawable.icon_mine_notify)
        updateAccountView()
        loginView.onClick {
            if (!userAccountManager.userLoggedIn()) {
                userAccountManager.startLogin(activity!!)
            }
        }
        wifiPlaySwitch.setOnClickListener {

        }

        aboutUs.onClick {
            start(createAboutFragment())
        }

        feedBack.onClick {
            start(createSuggestFragment())
        }

        clearCache.onClick {
            //TODO
        }
    }

    private fun updateAccountView() {
        if (!userAccountManager.userLoggedIn()) {
            userName.text = getString(R.string.click_to_login)
            avatarView.setImageResource(R.drawable.home_users1)
        } else {
            val user = userAccountManager.user()
            userName.text = user?.getNickName()
            if (TextUtils.isEmpty(user?.getAvatarUrl())) {
                avatarView.setImageResource(R.drawable.home_users1)
            } else {
                if (context != null)
                    Glide.with(context)
                            .load(user?.getAvatarUrl())
                            .crossFade(600)
                            .bitmapTransform(CropCircleTransformation(context))
                            .placeholder(R.drawable.my_avatar)
                            .priority(Priority.LOW)
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(avatarView)
            }
        }

    }

    override fun onSupportVisible() {
        if (mediaManager.isPlaying()) {
            miniPlayer.switchToPlay()
        }
    }

    @Subscribe
    fun onMediaPrepared(event: MediaPreparedEvent) {
        miniPlayer?.switchToPlay()
    }

    @Subscribe
    fun onMediaComplete(event: MediaCompleteEvent) {
        miniPlayer?.switchToPause()
    }
}


fun createMineFragment(external: Boolean, sub: String): MineFragment {
    val fragment = MineFragment()
    val bundle = Bundle()
    bundle.putBoolean("external", external)
    bundle.putString("sub", sub)
    fragment.arguments = bundle
    return fragment
}
