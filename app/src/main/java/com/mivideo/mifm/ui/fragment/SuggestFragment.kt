package com.mivideo.mifm.ui.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.R
import com.mivideo.mifm.data.repositories.MainRepository
import com.mivideo.mifm.network.request.FeedBackRequest
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.util.app.showToast
import kotlinx.android.synthetic.main.suggest_fragment.*
import org.jetbrains.anko.onClick

/**
 * Created by aaron on 17/2/8.
 * 意见反馈-Fragment
 */
class SuggestFragment : BaseFragment() {

    private val mainRepository: MainRepository by instance()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.suggest_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initLayoutView()
    }

    private fun initLayoutView() {

        ll_suggest_back.onClick {
            pop()
        }

        sendSuggestButton.onClick {
            val msgParams = suggestContentEdit.text.toString()
            val isBugBack = cardBugBack.isSelect
            val isProjectBack = cardProjectSuggest.isSelect
            val isOtherBack = cardOtherBack.isSelect
            val contractParams = contractContent.text.toString()
            var backParams = ""

            val sb = StringBuffer()
            if (isBugBack) {
                sb.append(context.resources.getString(R.string.suggest_fragment_back)).append(",")
            }
            if (isProjectBack) {
                sb.append(context.resources.getString(R.string.suggest_fragment_product)).append(",")
            }
            if (isOtherBack) {
                sb.append(context.resources.getString(R.string.suggest_fragment_other)).append(",")
            }

            if (!TextUtils.isEmpty(sb.toString())) {
                backParams = sb.substring(0, sb.length - 1)
            }

            if (msgParams.length <= 3) {
                showToast(context, context.resources.getString(R.string.suggest_fragment_msg_too_short))
                return@onClick
            }
            if (msgParams.length >= 120) {
                showToast(context, context.resources.getString(R.string.suggest_fragment_msg_too_long))
                return@onClick
            }

            val request = FeedBackRequest(msgParams, backParams, contractParams)
            mainRepository.feedback(request)
                    .compose(asyncSchedulers())
                    .subscribe({}, {})

            showToast(context, context.resources.getString(R.string.feedback_result_msg))
            pop()
        }
    }

    override fun onBackPressedSupport(): Boolean {
        pop()
        return true
    }

    override fun onSupportVisible() {
        if (mediaManager.isPlaying()) {
            mini_player_suggest.switchToPlay()
        }
    }

}

fun createSuggestFragment(): SuggestFragment {
    return SuggestFragment()
}