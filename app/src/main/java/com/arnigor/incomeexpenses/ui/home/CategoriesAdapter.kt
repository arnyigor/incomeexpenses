package com.arnigor.incomeexpenses.ui.home

import android.content.Context
import com.arnigor.incomeexpenses.ui.models.PaymentCategory
import com.arnigor.incomeexpenses.ui.models.PaymentType
import com.arnigor.incomeexpenses.utils.AbstractArrayAdapter

class CategoriesAdapter(context: Context) : AbstractArrayAdapter<PaymentCategory>(context) {
    override fun getItemTitle(item: PaymentCategory?): String {
        val type = if (item?.paymentType == PaymentType.INCOME) {
            "Входящие"
        } else {
            "Исходящие"
        }
        val title = item?.categoryTitle ?: ""
        return "$title $type"
    }
}