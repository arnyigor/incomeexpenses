package com.arnigor.incomeexpenses.utils

import android.view.KeyEvent
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
