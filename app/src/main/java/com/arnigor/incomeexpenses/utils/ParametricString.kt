package com.arnigor.incomeexpenses.utils

import android.content.Context

class ParametricString(private val format: String, private vararg val params: Any?) :
    WrappedString {
    override fun toString(context: Context): String? {
        return String.format(format, *params)
    }
}
