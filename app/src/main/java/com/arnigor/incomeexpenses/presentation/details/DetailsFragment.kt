package com.arnigor.incomeexpenses.presentation.details

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.arnigor.incomeexpenses.R
import com.arnigor.incomeexpenses.databinding.FragmentDetailsBinding
import com.arnigor.incomeexpenses.utils.*
import dagger.android.support.AndroidSupportInjection
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.properties.Delegates


class DetailsFragment : Fragment(R.layout.fragment_details) {

    @Inject
    lateinit var vm: DetailsViewModel

    private val args: DetailsFragmentArgs by navArgs()

    private val adapter by autoClean { PaymentsAdapter(::removeItem, ::changedItem) }

    private var editEnable by Delegates.observable(true) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            binding.tilCellData.isEnabled = newValue
        }
    }

    private val binding by viewBinding { FragmentDetailsBinding.bind(it) }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                requireActivity().onBackPressed()
                true
            }
            else -> false
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
        with(binding) {
            val linearLayoutManager = LinearLayoutManager(requireContext())
            rvPayments.layoutManager = linearLayoutManager
            rvPayments.adapter = adapter
            rvPayments.addItemDecoration(
                DividerItemDecoration(
                    rvPayments.context,
                    linearLayoutManager.orientation
                )
            )
            tiedtCellData.doAfterTextChanged { s ->
                if (s.toString().contains(".")) {
                    s?.replace(
                        0,
                        s.length,
                        SpannableStringBuilder(s.toString().replace(".", ","))
                    )
                }
            }
            fabSave.setOnClickListener {
                vm.confirmSave(adapter.currentList)
            }

            fabAdd.setOnClickListener {
                vm.addPayment(tiedtCellData.text.toString())
                tiedtCellData.setText("")
                requireActivity().hideKeyboard()
            }
        }
        vm.initUI(
            args.categories,
            args.category,
            args.month,
            args.cellData
        )
    }

    private fun observeData() {
        vm.currentMonth.observe(viewLifecycleOwner) { month ->
            binding.tvMonth.text = month
        }
        vm.toast.observe(viewLifecycleOwner, {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })
        vm.onBackPress.observe(viewLifecycleOwner, { backPress ->
            if (backPress) {
                requireActivity().onBackPressed()
            }
        })
        vm.editEnable.observe(viewLifecycleOwner) { editEnable ->
            this.editEnable = editEnable
        }
        vm.adapterModels.observe(viewLifecycleOwner) { payments ->
            adapter.submitList(payments)
            (binding.rvPayments.layoutManager as LinearLayoutManager).scrollToPosition(0)
        }
        vm.loading.observe(viewLifecycleOwner, { loading ->
            binding.progressBar.isVisible = loading
        })
        vm.categoriesData.observe(viewLifecycleOwner) { (_, currentCategory) ->
            binding.tvCategory.text = currentCategory?.categoryTitle
        }
        vm.paymentsSum.observe(viewLifecycleOwner) { sum ->
            if (sum != null) {
                binding.tvCategorySum.text = String.format("%s", sum.toString())
            }
        }
        vm.sumData.observe(viewLifecycleOwner) { sum ->
            if (sum != null && sum.added.compareTo(BigDecimal.ZERO) != 0) {
                alertDialog(
                    title = getString(R.string.edit_question),
                    content = getString(
                        R.string.sum_data,
                        sum.firstLoadSum.formatNumberWithSpaces(),
                        sum.added.formatNumberWithSpaces(),
                        sum.totalSum.formatNumberWithSpaces()
                    ),
                    btnCancelText = getString(android.R.string.cancel),
                    btnOkText = getString(android.R.string.ok),
                    cancelable = true,
                    onConfirm = {
                        requireActivity().hideKeyboard()
                        vm.save(adapter.currentList)
                    }
                )
            }
        }
    }

    private fun removeItem(position: Int) {
        vm.removeSum(position)
    }

    private fun changedItem(position: Int, sum: String) {
        vm.itemChanged(position, sum)
    }
}
