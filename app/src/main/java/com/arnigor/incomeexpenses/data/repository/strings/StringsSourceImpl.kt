package com.arnigor.incomeexpenses.data.repository.strings

import android.content.Context
import androidx.annotation.StringRes
import javax.inject.Inject

class StringsSourceImpl @Inject constructor(
    private val context: Context
) : StringsSource {
    override fun getString(@StringRes res: Int): String =
        context.getString(res)
}