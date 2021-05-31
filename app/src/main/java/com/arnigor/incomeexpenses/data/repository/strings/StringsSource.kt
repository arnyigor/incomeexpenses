package com.arnigor.incomeexpenses.data.repository.strings

import androidx.annotation.StringRes

interface StringsSource {
    fun getString(@StringRes res: Int): String
}