package com.arnigor.incomeexpenses.ui.models

import java.math.BigDecimal

data class SpreadSheetData(
    val categories: List<PaymentCategory>,
    val monthsData: List<MonthData>
)

data class MonthData(
    val monthName: String? = null,
    val payments: List<Payment>? = emptyList(),
    val totalIncome: BigDecimal? = null,
    val totalOutcome: BigDecimal? = null
)

data class Payment(
    val paymentCategory: PaymentCategory? = null,
    val formula: String? = null,
    val value: BigDecimal? = null
)

data class PaymentData(val value: String?, val position: String?)

data class PaymentCategory(
    val categoryTitle: String? = null,
    val paymentType: PaymentType,
    val sheetPosition: String? = null
)

enum class PaymentType {
    INCOME, OUTCOME
}
