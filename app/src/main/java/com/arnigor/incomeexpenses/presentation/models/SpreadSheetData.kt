package com.arnigor.incomeexpenses.presentation.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class SpreadSheetData(
    val categories: List<PaymentCategory>,
    val monthsData: List<MonthData>,
) : Parcelable

@Parcelize
data class MonthData(
    val monthName: String? = null,
    val payments: List<Payment>? = emptyList(),
    val totalIncome: BigDecimal? = null,
    val totalOutcome: BigDecimal? = null
) : Parcelable

@Parcelize
data class Payment(
    val paymentCategory: PaymentCategory? = null,
    val formula: String? = null,
    val value: BigDecimal? = null
) : Parcelable

@Parcelize
data class PaymentData(val value: String?, val position: String?) : Parcelable

@Parcelize
data class PaymentCategory(
    val categoryTitle: String? = null,
    val paymentType: PaymentType,
    val sheetPosition: String? = null
) : Parcelable

enum class PaymentType {
    INCOME, OUTCOME, INCOME_SUM, OUTCOME_SUM, BALANCE
}
