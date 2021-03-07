package com.arnigor.incomeexpenses.ui.models

import java.math.BigDecimal

data class SpreadSheetData(
    val categories: List<PaymentCategory>,
    val monthsData: List<MonthData>
)

data class MonthData(
    val monthName: String,
    val payments: List<Payment>,
    val totalIncome: BigDecimal? = null,
    val totalOutcome: BigDecimal? = null
)

data class Payment(
    val paymentCategory: PaymentCategory,
    val formula: String? = null,
    val value: BigDecimal? = null
)

data class PaymentCategory(
    val categoryTitle: String? = null,
    val paymentType: PaymentType,
    val sheetPosition: String? = null
)

enum class PaymentType {
    INCOME, OUTCOME, CORRECTION, UNKNOWN
}
