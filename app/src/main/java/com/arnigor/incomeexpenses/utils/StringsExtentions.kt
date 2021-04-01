package com.arnigor.incomeexpenses.utils

import java.util.*

fun String.toFirstUpperCase() =
    this.substring(0, 1).toUpperCase(Locale.getDefault()) + this.substring(1)
        .toLowerCase(Locale.getDefault())


fun String.normalize() = this.toLowerCase(Locale.getDefault()).trim()