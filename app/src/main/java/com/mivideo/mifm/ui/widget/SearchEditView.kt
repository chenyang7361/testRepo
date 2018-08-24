package com.mivideo.mifm.ui.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.EditText
import rx.Observable
import rx.lang.kotlin.BehaviorSubject

/**
 * Created by aaron on 2017/3/29.
 * 自定义组件，搜索页面输入框
 */
class SearchEditView : EditText {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    val subjectFocusChange = BehaviorSubject<Boolean>()
    val subjectKeyEvent = BehaviorSubject<Boolean>()
    val subjectTextChange = BehaviorSubject<String>()

    /**
     * 监听EditText的焦点状态
     */
    fun observerFocusChange() : Observable<Boolean> {
        return subjectFocusChange
    }

    /**
     * 监听软键盘搜索按钮点击
     */
    fun observerKeySearch() : Observable<Boolean> {
        return subjectKeyEvent
    }

    /**
     * 监听输入框文本变化
     */
    fun observerTextChange(): Observable<String> {
        return subjectTextChange
    }

    /**
     * 执行数据初始化操作
     */
    private fun init() {
        setOnTouchListener({ view, motionEvent ->
            focusAble(true)
            false
        })

        setOnFocusChangeListener({ view, b ->
            if (b) {
                focusAble(true)
            } else {
                focusAble(false)
            }

            subjectFocusChange.onNext(b)
        })

        setOnKeyListener { view, keyCode, keyEvent ->
            if (KeyEvent.KEYCODE_ENTER == keyCode && KeyEvent.ACTION_DOWN == keyEvent.action) {
                subjectKeyEvent.onNext(true)
                true
            }
            false
        }

        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                subjectTextChange.onNext(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
    }


    private fun focusAble(able: Boolean) {
        isCursorVisible = able
        isFocusable = able
        isFocusableInTouchMode = able
    }
}