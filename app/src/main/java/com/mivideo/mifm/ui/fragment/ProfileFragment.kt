package com.mivideo.mifm.ui.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mivideo.mifm.R
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.viewmodel.UserViewModel
import com.xiaomi.accountsdk.account.data.Gender
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.fragment_profile.*
import me.yokeyword.fragmentation.ISupportFragment
import org.jetbrains.anko.onClick
import java.util.*

/**
 * Created by Jiwei Yuan on 18-8-21.
 */
class ProfileFragment : BaseFragment() {

    companion object {
        const val REQUEST_CODE_PICK = 0
    }

    val userViewModel: UserViewModel = UserViewModel(context)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        avater.onClick {
            val intent = Intent()
            intent.action = Intent.ACTION_PICK
            startActivityForResult(intent, REQUEST_CODE_PICK)
        }
        name.onClick {

        }

        userViewModel.getXiaomiCoreUserInfo().compose(asyncSchedulers())
                .subscribe({
                    tvName.text = it.nickName
                    tvGender.text = if (it.gender.type.equals(Gender.MALE)) getString(R.string.male) else getString(R.string.female)
                    tvBirth.text = it.birthday.get(Calendar.YEAR).toString() + "-" + it.birthday.get(Calendar.MONTH).toString() + "-" + it.birthday.get(Calendar.DAY_OF_MONTH).toString()
                    Glide.with(context).load(it.avatarAddress)
                            .placeholder(Color.GRAY)
                            .crossFade(600)
                            .priority(Priority.HIGH)
                            .bitmapTransform(RoundedCornersTransformation(context, 5, 0))
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(ivAvater)
                }, {})
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_PICK && requestCode == ISupportFragment.RESULT_OK) {

        }
    }

    override fun onSupportVisible() {
        if (mediaManager.isPlaying()) {
            miniPlayer.switchToPlay()
        }
    }

}