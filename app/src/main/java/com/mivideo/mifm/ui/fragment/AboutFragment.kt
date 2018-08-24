package com.mivideo.mifm.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mivideo.mifm.EulaActivity
import com.mivideo.mifm.R

import com.mivideo.mifm.update.UpdateManager
import kotlinx.android.synthetic.main.fragment_about_us.*
import org.jetbrains.anko.onClick

class AboutFragment : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about_us, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        left.onClick {
            pop()
        }
        aboutVersionName.text = getVersionName()
        versionLayout.onClick {
            activity?.let {
                UpdateManager.getInstance(context).checkUpdateByUser(it)
            }
        }
        userAgreement.onClick {
            activity?.let {
                startActivity(EulaActivity.getAgreementIntent(it))
            }
        }
    }

    override fun onSupportVisible() {
        if (mediaManager.isPlaying()) {
            miniPlayer.switchToPlay()
        }
    }

    private fun getVersionName(): String? {
        try {
            val pkName = context.getPackageName()
            val versionName = context.packageManager.getPackageInfo(
                    pkName, 0).versionName
            return "$versionName"
        } catch (e: Exception) {
        }

        return null
    }

    override fun onBackPressedSupport(): Boolean {
        pop()
        return true
    }
}

fun createAboutFragment(): AboutFragment {
    return AboutFragment()
}