package com.arnigor.incomeexpenses.presentation.home

import android.content.Context
import com.arnigor.incomeexpenses.R
import com.arnigor.incomeexpenses.presentation.models.PaymentCategory
import com.arnigor.incomeexpenses.presentation.models.PaymentType
import com.arnigor.incomeexpenses.utils.AbstractArrayAdapter

class CategoriesAdapter(context: Context) :
    AbstractArrayAdapter<PaymentCategory>(context, android.R.layout.simple_dropdown_item_1line) {
    override fun getItemTitle(item: PaymentCategory?): String {
        val type = if (item?.paymentType == PaymentType.INCOME) {
            context.getString(R.string.income)
        } else {
            context.getString(R.string.outcome)
        }
        val title = item?.categoryTitle ?: ""
        return "$title $type"
    }
}