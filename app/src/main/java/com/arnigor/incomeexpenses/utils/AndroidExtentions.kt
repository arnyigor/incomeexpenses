package com.arnigor.incomeexpenses.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

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

fun <T> Fragment.autoClean(init: () -> T): ReadOnlyProperty<Fragment, T> = AutoClean(init)

private class AutoClean<T>(private val init: () -> T) : ReadOnlyProperty<Fragment, T>,
    LifecycleEventObserver {

    private var cached: T? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        return cached ?: init().also { newValue ->
            cached = newValue
            thisRef.viewLifecycleOwner.lifecycle.addObserver(this)
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            cached = null
            source.lifecycle.removeObserver(this)
        }
    }
}