package com.mivideo.mifm.ui.widget

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.mivideo.mifm.MainConfig
import com.mivideo.mifm.R
import com.mivideo.mifm.util.app.showToast
import org.jetbrains.anko.find
import java.text.DecimalFormat

/**
 * Created by xingchang on 17/4/25.
 */
class CustomDialog : Dialog {
    companion object {
        val ONLY_CONFIRM_BUTTON_MODE = 0
        val NORMAL_MODE = 1
        val PROGRESS_MODE = 2
    }

    private lateinit var titleView: TextView
    private lateinit var textView: TextView
    private lateinit var sizeView: TextView
    private lateinit var verisonView: TextView
    private lateinit var progressTextView: TextView
    private lateinit var cancelBtn: Button
    private lateinit var updateBtn: Button
    private lateinit var progressGroup: View
    private lateinit var progressBar: ProgressBar

    constructor(context: Context) : super(context) {
        init(context)
    }


    constructor(context: Context, theme: Int) : super(context, theme) {
        init(context)
    }

    private fun init(context: Context) {
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.update_dialog, null)
        titleView = layout.find<TextView>(R.id.update_dialog_title_view)
        textView = layout.find<TextView>(R.id.update_dialog_content_view)
        sizeView = layout.find<TextView>(R.id.update_dialog_size_view)
        verisonView = layout.find<TextView>(R.id.update_dialog_version_view)
        progressTextView = layout.find<TextView>(R.id.update_dialog_progress_text_view)
        cancelBtn = layout.find<Button>(R.id.close)
        updateBtn = layout.find<Button>(R.id.update_dialog_update_btn)
        progressGroup = layout.find<View>(R.id.update_dialog_progress_group)
        progressBar = layout.find<ProgressBar>(R.id.update_dialog_progress)
        addContentView(layout, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    fun changeUIMode(mode: Int) {
        if (mode == ONLY_CONFIRM_BUTTON_MODE) {
            updateBtn.visibility = View.VISIBLE
            progressGroup.visibility = View.GONE
            cancelBtn.visibility = View.GONE
        } else if (mode == NORMAL_MODE) {
            updateBtn.visibility = View.VISIBLE
            progressGroup.visibility = View.GONE
            cancelBtn.visibility = View.VISIBLE
        } else if (mode == PROGRESS_MODE) {
            updateBtn.visibility = View.GONE
            progressGroup.visibility = View.VISIBLE
            cancelBtn.visibility = View.GONE
        }
    }

    fun setProgress(progress: Int, max: Int) {
        progressBar.max = max
        progressBar.progress = progress
        val decimalFormat = DecimalFormat("0.0")
        progressTextView.text = "" + decimalFormat.format(progress * 100f / max) + "%"
    }

    class Builder {
        private var context: Context
        private var title: String = ""
        private var message: String = ""
        private var size: String = ""
        private var versionName = ""
        private var cancelOutside = true
        private var positiveButtonText: String? = null
        private var negativeButtonText: String? = null
        private var positiveButtonClickListener: View.OnClickListener? = null
        private var negativeButtonClickListener: View.OnClickListener? = null
        private var keyBackTime: Long = 0

        constructor(context: Context) {
            this.context = context
        }

        fun setMessage(message: String): Builder {
            this.message = message
            return this
        }

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setSize(size: String): Builder {
            this.size = size
            return this
        }

        fun setVersionName(versionName: String): Builder {
            this.versionName = versionName
            return this
        }

        fun setCanceledOnTouchOutside(cancel: Boolean): Builder {
            this.cancelOutside = cancel
            return this
        }

        fun setPositiveButtonClickListener(listener: View.OnClickListener): Builder {
            this.positiveButtonClickListener = listener
            return this
        }

        fun setNegativeButtonClickListener(listener: View.OnClickListener): Builder {
            this.negativeButtonClickListener = listener
            return this
        }

        fun create(): CustomDialog {
            val dialog = CustomDialog(context, R.style.Dialog)
            dialog.titleView.text = title
            dialog.textView.text = message
            dialog.sizeView.text = "大小: " + size + "MB"
            dialog.verisonView.text = "版本: " + versionName
            dialog.textView.movementMethod = ScrollingMovementMethod.getInstance()
            if (this.negativeButtonClickListener == null) {
                dialog.changeUIMode(ONLY_CONFIRM_BUTTON_MODE)
                dialog.updateBtn.setOnClickListener(positiveButtonClickListener)
            } else {
                dialog.changeUIMode(NORMAL_MODE)
                dialog.updateBtn.setOnClickListener(positiveButtonClickListener)
                dialog.cancelBtn.setOnClickListener(negativeButtonClickListener)
            }
            if (!cancelOutside) {
                dialog.setOnKeyListener { dialog, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount === 0 && event.action == KeyEvent.ACTION_DOWN) {
                        if (System.currentTimeMillis() - keyBackTime > MainConfig.STANDARD_BACK_INTERVAL_TIME || keyBackTime == 0.toLong()) {
                            keyBackTime = System.currentTimeMillis()
                            showToast(context, "再按一次退出")
                        } else {
                            dialog.dismiss()
                            (context as Activity).finish()
                        }
                        true
                    }
                    false
                }
            }
            dialog.setCancelable(cancelOutside)
            dialog.setCanceledOnTouchOutside(false)
            return dialog
        }
    }
}