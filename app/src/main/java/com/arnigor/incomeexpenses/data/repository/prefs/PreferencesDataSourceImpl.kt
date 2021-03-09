package com.arnigor.incomeexpenses.data.repository.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import javax.inject.Inject

class PreferencesDataSourceImpl @Inject constructor(
    private val context: Context
) : PreferencesDataSource {
    private var sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    fun getAll(): Map<String, *>? {
        return sharedPreferences.all
    }

    fun put(key: String?, value: Any?) {
        sharedPreferences.edit().put(key, value).apply()
    }

    fun remove(vararg key: String) {
        val edit = sharedPreferences.edit()
        for (k in key) {
            edit?.remove(k)
        }
        edit?.apply()
    }

    private fun SharedPreferences.Editor.put(key: String?, value: Any?): SharedPreferences.Editor {
        when (value) {
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            is Double -> putFloat(key, value.toFloat())
            is String -> putString(key, value)
            is Boolean -> putBoolean(key, value)
        }
        return this
    }

    override fun getPref(prefName: Int): String? {
        return sharedPreferences.all[context.getString(prefName)] as? String
    }
}