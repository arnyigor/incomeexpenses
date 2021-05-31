package com.arnigor.incomeexpenses.data.repository.prefs

import androidx.annotation.StringRes

interface PreferencesDataSource {
    fun getPrefString(@StringRes prefKey: Int): String?
    fun getPrefInt(@StringRes prefKey: Int): Int?
    fun getPrefBool(@StringRes prefKey: Int): Boolean?
    fun put(@StringRes prefKey: Int, value: Any?)
}