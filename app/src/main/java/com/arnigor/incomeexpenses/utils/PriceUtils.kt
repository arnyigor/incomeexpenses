package com.arnigor.incomeexpenses.utils

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat

fun Number.formatNumberWithSpaces(): String? {
    val formatter: DecimalFormat = NumberFormat.getInstance() as DecimalFormat
    val symbols: DecimalFormatSymbols = formatter.decimalFormatSymbols
    symbols.groupingSeparator = ' '
    return formatter.format(this)
}

fun String.parseDataValues() =
    "[\\-+]?\\d+\\.?\\d+".toRegex()
    .findAll(this.replace("=", "").replace(",", "."))
    .map { it.groupValues[0].toBigDecimalOrNull() ?: BigDecimal.ZERO }
    .toMutableList()