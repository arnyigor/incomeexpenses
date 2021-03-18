package com.arnigor.incomeexpenses.utils

import android.content.Context

interface WrappedString {
    fun toString(context: Context): String?
}