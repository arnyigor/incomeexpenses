package com.arnigor.incomeexpenses.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

fun Activity.hideKeyboard(flags: Int = 0) {
    try {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        if (imm != null) {
            val focus = this.window.decorView.rootView
            if (focus != null) {
                imm.hideSoftInputFromWindow(focus.windowToken, flags)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}