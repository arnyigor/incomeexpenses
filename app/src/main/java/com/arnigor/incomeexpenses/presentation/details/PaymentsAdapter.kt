package com.arnigor.incomeexpenses.presentation.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arnigor.incomeexpenses.databinding.IPaymentItemBinding
import com.arnigor.incomeexpenses.presentation.models.PaymentsAdapterModel
import com.arnigor.incomeexpenses.utils.setOnRightDrawerClickListener

class PaymentsAdapter(
    private val onItemRemove: (position: Int) -> Unit,
    private val onItemChanged: (position: Int, sum: String) -> Unit
) : ListAdapter<PaymentsAdapterModel, PaymentsAdapter.ItemViewholder>(
    object : DiffUtil.ItemCallback<PaymentsAdapterModel>() {
        override fun areItemsTheSame(
            oldItem: PaymentsAdapterModel,
            newItem: PaymentsAdapterModel
        ): Boolean = oldItem == newItem

        override fun areContentsTheSame(
            oldItem: PaymentsAdapterModel,
            newItem: PaymentsAdapterModel
        ): Boolean = oldItem == newItem
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
        fun bind(item: PaymentsAdapterModel) {
            with(itemBinding) {
                val sum = item.sum.toString()
                if (sum.isNotBlank()) {
                    tvSum.text = String.format("%s", sum)
                }
                tvSum.setOnRightDrawerClickListener {
                    tiedtPayment.setText(tvSum.text.toString())
                    tvSum.isVisible = false
                    tilPayment.isVisible = true
                    itemBinding.root.requestLayout()
                }
                tiedtPayment.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        updateItem()
                    }
                }
                tiedtPayment.setOnRightDrawerClickListener {
                    updateItem()
                }
                tiedtPayment.doAfterTextChanged { s ->
                    s.toString().toBigDecimalOrNull()?.let { decimal ->
                        item.sum = decimal
                    }
                }
                ivRemove.setOnClickListener { onItemRemove(adapterPosition) }
            }
        }

        private fun IPaymentItemBinding.updateItem() {
            tvSum.text = tiedtPayment.text.toString()
            tiedtPayment.setText(tvSum.text.toString())
            tvSum.isVisible = true
            tilPayment.isVisible = false
            itemBinding.root.requestLayout()
            onItemChanged(adapterPosition, tiedtPayment.text.toString())
        }
    }
}
