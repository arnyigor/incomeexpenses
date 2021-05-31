package com.arnigor.incomeexpenses.utils

import android.annotation.SuppressLint
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView

fun EditText?.doWhenEnterClicked(onChanged: () -> Unit) {
    nonNullOrSkip {
        this.setOnEditorActionListener { _: TextView?, actionId: Int, event: KeyEvent? ->
            when {
                actionId == EditorInfo.IME_ACTION_DONE ||
                        event != null && event.action == KeyEvent.ACTION_DOWN &&
                        event.keyCode == KeyEvent.KEYCODE_ENTER -> {
                    onChanged()
                    true
                }
                else -> false
            }
        }
    }
}

inline fun <T, R> T?.nonNullOrSkip(block: T.() -> R) {
    this?.run(block)
}

@SuppressLint("ClickableViewAccessibility")
fun TextView.setOnRightDrawerClickListener(onClick: () -> Unit) {
    this.setOnTouchListener(object : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (event != null && event.action == MotionEvent.ACTION_DOWN) {
                if (event.rawX >= (this@setOnRightDrawerClickListener.right - this@setOnRightDrawerClickListener.compoundDrawables[2].bounds.width())) {
                    onClick()
                    return true
                }
            }
            return false
        }
    })
}

