package com.arnigor.incomeexpenses.utils

import android.content.Context

class SimpleString(val string: String?) : WrappedString {
    override fun toString(context: Context): String? = string
}