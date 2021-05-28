package com.arnigor.incomeexpenses.utils

fun String.toFirstUpperCase() =
    this.substring(0, 1).uppercase() + this.substring(1)
        .lowercase()


fun String.normalize() = this.lowercase().trim()