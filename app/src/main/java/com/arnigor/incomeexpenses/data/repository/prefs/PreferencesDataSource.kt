package com.arnigor.incomeexpenses.data.repository.prefs

import androidx.annotation.StringRes

interface PreferencesDataSource {
    fun getPref(@StringRes prefName: Int): String?
}