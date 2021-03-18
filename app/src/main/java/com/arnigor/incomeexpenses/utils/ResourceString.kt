package com.arnigor.incomeexpenses.utils

import android.content.Context
import androidx.annotation.StringRes

class ResourceString(@StringRes val resString: Int) : WrappedString {

    override fun toString(context: Context): String? {
        return context.getString(resString)
    }
}