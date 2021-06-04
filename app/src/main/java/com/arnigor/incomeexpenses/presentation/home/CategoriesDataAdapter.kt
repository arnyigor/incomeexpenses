package com.arnigor.incomeexpenses.presentation.home

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arnigor.incomeexpenses.R
import com.arnigor.incomeexpenses.databinding.ItemCategorySumBinding
import com.arnigor.incomeexpenses.presentation.models.AdapterCategoryModel
import com.arnigor.incomeexpenses.presentation.models.PaymentType
import com.arnigor.incomeexpenses.utils.toColorInt
import com.arnigor.incomeexpenses.utils.toFirstUpperCase

class CategoriesDataAdapter(
    private val onItemEdit: (item: AdapterCategoryModel) -> Unit
) :
    ListAdapter<AdapterCategoryModel, CategoriesDataAdapter.AdapterViewholder>(
        object : DiffUtil.ItemCallback<AdapterCategoryModel>() {
            override fun areItemsTheSame(
                oldItem: AdapterCategoryModel,
                newItem: AdapterCategoryModel
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: AdapterCategoryModel,
                newItem: AdapterCategoryModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewholder {
        return AdapterViewholder(
            ItemCategorySumBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: AdapterViewholder, position: Int) {
        holder.bind(getItem(holder.adapterPosition))
    }

    inner class AdapterViewholder(private val itemBinding: ItemCategorySumBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: AdapterCategoryModel) {
            val root = itemBinding.root
            with(itemBinding) {
                when (item.type) {
                    PaymentType.INCOME -> R.color.greenDark
                    PaymentType.OUTCOME -> R.color.redRark
                    PaymentType.BALANCE -> R.color.blue
                    PaymentType.INCOME_SUM -> R.color.green
                    PaymentType.OUTCOME_SUM -> R.color.red
                    else -> null
                }?.let { colorRes ->
                    tvCategory.setTextColor(colorRes.toColorInt(root.context))
                    if (item.type == PaymentType.BALANCE) {
                        tvCategory.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                        tvCatSum.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                    }
                }
                tvCategory.text = item.title?.toString(root.context)?.toFirstUpperCase()
                tvCatSum.text = item.sum
                ivEdit.isVisible = item.type !in listOf(
                    PaymentType.INCOME_SUM,
                    PaymentType.OUTCOME_SUM,
                    PaymentType.BALANCE
                )
                ivEdit.setOnClickListener { onItemEdit(item) }
            }
        }
    }
}
