package com.mivideo.mifm.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.mivideo.mifm.R
import com.mivideo.mifm.ui.widget.CustomStatusView
import com.mivideo.mifm.util.ScreenUtil

/**
 * 用户登录选择弹框

 * @author LiYan
 */
class LoginDialog2 private constructor(context: Context,
                                       themeResId: Int,
                                       private val builder: Builder) : Dialog(context, themeResId) {

    companion object {

        fun builder(): LoginDialog2.Builder {
            return Builder()
        }
    }

    /**
     * 加载loading
     */
    fun startLoading() {
        builder.setDialogLoading()
    }

    /**
     * 加载成功状态显示
     */
    fun loadingSuccess() {
        builder.setDialogSuccess()
    }

    /**
     * 加载失败状态显示
     */
    fun loadingFailure() {
    }

    class Builder {
        private var title: String? = null
        private var canTouchOutDismiss = true
        private var mDialog: LoginDialog2? = null
        private val enhanceConfirm = EnhanceBtn.ENHANCE_CONFIRM
        private var listener: LoginListener? = null

        lateinit var ivTitle: ImageView
        lateinit var ivBgHead: ImageView
        lateinit var tvPrivacyClause: TextView
        lateinit var tvUserAgreement: TextView
        lateinit var ivWx: ImageView
        lateinit var ivQQ: ImageView
        lateinit var ivWeibo: ImageView
        lateinit var ivXiaoMi: ImageView
        lateinit var loading: ProgressBar
        lateinit var llChooseArea: LinearLayout
        lateinit var llSuccessList: LinearLayout
        lateinit var llPrivacyClause: LinearLayout
        lateinit var ivClose: ImageView

        private var context: Context? = null


        fun setTouchOutDismiss(canTouchOutDismiss: Boolean): Builder {
            this.canTouchOutDismiss = canTouchOutDismiss
            return this
        }

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setLoginListener(listener: LoginListener): Builder {
            this.listener = listener
            return this
        }

        fun build(context: Context): Dialog {
            this.context = context
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            mDialog = LoginDialog2(context, R.style.publish_dialog, this)
            val initailLayout = InitailLayout(context)
            initailLayout.bind(listener, mDialog!!)
            mDialog!!.setCanceledOnTouchOutside(canTouchOutDismiss)
            mDialog!!.setContentView(initailLayout.contentView)
            mDialog!!.setOnShowListener {
//                context.miStatistics(Statistics.ACTION.LOGIN_DIALOG_SHOW, Statistics.CATEGORY.LOGIN)
            }
            return mDialog!!
        }

        fun setDialogLoading() {
            if (mDialog != null && context != null) {
                val loadingLayout = LoadingLayout(context!!)
                loadingLayout.bind(listener, mDialog!!)
                mDialog?.setContentView(loadingLayout.contentView)
            }
        }

        fun setDialogSuccess() {
            if (mDialog != null && context != null) {
                val successLayout = SuccessLayout(context!!)
                successLayout.bind(listener, mDialog!!)
                mDialog?.setContentView(successLayout.contentView)

                val params = mDialog!!.window.attributes
                params.width = ScreenUtil.dip2px(context,294f)
                mDialog!!.window.attributes = params
            }
        }

        private inner class InitailLayout(val context: Context) {
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_login2_initial, null)

            val ivWeibo: ImageView = contentView.findViewById(R.id.iv_weibo)
            val ivWx: ImageView = contentView.findViewById(R.id.iv_weixin)
            val ivQQ: ImageView = contentView.findViewById(R.id.iv_qq)
            val ivXiaoMi: ImageView = contentView.findViewById(R.id.iv_xiaomi)
            val ivClose: ImageView = contentView.findViewById(R.id.iv_login_close)
            val tvPrivacyClause: TextView = contentView.findViewById(R.id.tv_privacy_clause)
            val tvUserAgreement: TextView = contentView.findViewById(R.id.tv_user_agreement)

            fun bind(listener: LoginListener?, mDialog: LoginDialog2) {
                tvPrivacyClause.paint.flags = Paint.UNDERLINE_TEXT_FLAG
                tvPrivacyClause.paint.isAntiAlias = true
                tvPrivacyClause.setOnClickListener {
                    listener?.onClickPrivacyClause(mDialog)
                }

                tvUserAgreement.paint.flags = Paint.UNDERLINE_TEXT_FLAG
                tvUserAgreement.paint.isAntiAlias = true
                tvUserAgreement.setOnClickListener {
                    listener?.onClickUserAgreement(mDialog)
                }

                ivWeibo.setOnClickListener {
                    listener?.onClickWeibo(mDialog)
                }
                ivQQ.setOnClickListener {
                    listener?.onClickQQ(mDialog)
                }
                ivWx.setOnClickListener {
                    listener?.onClickWx(mDialog)
                }
                ivXiaoMi.setOnClickListener {
                    listener?.onClickXiaoMi(mDialog)
                }
                ivClose.setOnClickListener {
                    listener?.onClickClose(mDialog)
                }
            }
        }

        private inner class LoadingLayout(val context: Context) {
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_login2_loading, null)
            val loading: CustomStatusView = contentView.findViewById(R.id.loading_login)

            fun bind(listener: LoginListener?, mDialog: LoginDialog2) {
                loading.loadLoading()
            }
        }


        private inner class SuccessLayout(val context: Context) {
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_login2_success, null)

            val ivClose: ImageView = contentView.findViewById(R.id.iv_login_close)
            val tvConfirmBtn: TextView = contentView.findViewById(R.id.tv_confirm_btn)

            fun bind(listener: LoginListener?, mDialog: LoginDialog2) {
                ivClose.setOnClickListener {
                    listener?.onClickClose(mDialog)
                }
                tvConfirmBtn.setOnClickListener {
                    listener?.onClickSuccessConfirm(mDialog)
                }
            }
        }

    }

    abstract class LoginListener {
        open fun onClickWx(dialog: LoginDialog2) {

        }

        open fun onClickQQ(dialog: LoginDialog2) {

        }

        open fun onClickWeibo(dialog: LoginDialog2) {

        }

        open fun onClickXiaoMi(dialog: LoginDialog2) {

        }

        open fun onClickClose(dialog: LoginDialog2) {

        }

        open fun onClickPrivacyClause(dialog: LoginDialog2) {

        }

        open fun onClickUserAgreement(dialog: LoginDialog2) {

        }

        open fun onClickSuccessConfirm(dialog: LoginDialog2) {

        }
    }
}