package com.arnigor.incomeexpenses.utils

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat

fun Intent?.dump(): String? {
    if (this != null) {
        val bundle = this.extras
        if (bundle != null) {
            val stringBuilder = StringBuilder()
            for (key in bundle.keySet()) {
                val value = bundle[key]
                if (value != null) {
                    stringBuilder.append(
                        String.format(
                            "\nkey:%s  val:%s  classname:(%s)",
                            key,
                            value.toString(),
                            value.javaClass.name
                        )
                    )
                }
            }
            return stringBuilder.toString()
        }
    }
    return null
}

fun Bundle?.dump(): String? {
    if (this != null) {
        val stringBuilder = StringBuilder()
        for (key in this.keySet()) {
            val value = this[key]
            if (value != null) {
                stringBuilder.append(
                    String.format(
                        "\nkey:%s  val:%s  classname:(%s)",
                        key,
                        value.toString(),
                        value.javaClass.name
                    )
                )
            }
        }
        return stringBuilder.toString()
    }
    return null
}

@ColorInt
fun Int.toColorInt(context: Context): Int {
    return ContextCompat.getColor(context, this)
}

fun Int.toDrawable(context: Context): Drawable? {
    return ContextCompat.getDrawable(context, this)
}