package com.arnigor.incomeexpenses.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
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
    private val onItemSelect: (item: AdapterCategoryModel) -> Unit
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
            root.setOnClickListener { onItemSelect(item) }
            with(itemBinding) {
                when (item.type) {
                    PaymentType.INCOME -> R.color.greenDark
                    PaymentType.OUTCOME -> R.color.redRark
                    PaymentType.BALANCE -> R.color.blue
                    PaymentType.INCOME_SUM -> R.color.green
                    PaymentType.OUTCOME_SUM -> R.color.red
                    else -> null
                }?.let {
                    tvCategory.setTextColor(it.toColorInt(root.context))
                }
                tvCategory.text = item.title?.toString(root.context)?.toFirstUpperCase()
                tvCatSum.text = item.sum
            }
        }
    }
}
