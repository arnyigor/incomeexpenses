package com.arnigor.incomeexpenses.presentation.models

import com.arnigor.incomeexpenses.utils.WrappedString

data class AdapterCategoryModel(
    val title: WrappedString?,
    val sum: String?,
    val type: PaymentType?,
    val catSum: Boolean = false
)
