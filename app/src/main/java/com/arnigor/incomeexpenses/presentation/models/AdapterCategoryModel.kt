package com.arnigor.incomeexpenses.presentation.models

import com.arnigor.incomeexpenses.utils.WrappedString
import java.math.BigDecimal

data class AdapterCategoryModel(
    val title: WrappedString?,
    val sum: BigDecimal?,
    val type: PaymentType?,
    val catSum: Boolean = false
)
