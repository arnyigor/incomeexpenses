package com.arnigor.incomeexpenses.presentation.details

import com.arnigor.incomeexpenses.presentation.models.PaymentCategory

data class CurrentCategoryData(
    val categories: List<PaymentCategory>? = emptyList(),
    val currentCategory: PaymentCategory?
)
