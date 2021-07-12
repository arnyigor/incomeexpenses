package com.arnigor.incomeexpenses.presentation.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arnigor.incomeexpenses.databinding.IPaymentItemBinding
import java.math.BigDecimal

class PaymentsAdapter(
    private val onItemRemove: (position: Int) -> Unit
) : ListAdapter<BigDecimal, PaymentsAdapter.ItemViewholder>(
    object : DiffUtil.ItemCallback<BigDecimal>() {
        override fun areItemsTheSame(oldItem: BigDecimal, newItem: BigDecimal): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: BigDecimal, newItem: BigDecimal): Boolean =
            oldItem == newItem
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewholder {
        return ItemViewholder(
            IPaymentItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewholder, position: Int) {
        holder.bind(getItem(holder.adapterPosition))
    }

    inner class ItemViewholder(private val itemBinding: IPaymentItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: BigDecimal) {
            with(itemBinding) {
                tiedtPayment.setText(item.toString())
                ivRemove.setOnClickListener { onItemRemove(adapterPosition) }
            }
        }
    }
}
