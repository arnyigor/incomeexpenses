package com.arnigor.incomeexpenses.utils

fun <T> Collection<T>?.getIndexBy(predicate: (T) -> Boolean): Int? {
    return this?.indexOfFirst(predicate).takeIf { it != -1 && it != null }
}