package com.arnigor.incomeexpenses.presentation.details

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.arnigor.incomeexpenses.R
import com.arnigor.incomeexpenses.databinding.FragmentDetailsBinding
import com.arnigor.incomeexpenses.utils.alertDialog
import com.arnigor.incomeexpenses.utils.hideKeyboard
import com.arnigor.incomeexpenses.utils.viewBinding
import dagger.android.support.AndroidSupportInjection
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.properties.Delegates

class DetailsFragment : Fragment(R.layout.fragment_details) {

    @Inject
    lateinit var vm: DetailsViewModel

    private val args: DetailsFragmentArgs by navArgs()

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
            tiedtCellData.doAfterTextChanged {
                if (it.toString().isBlank()) {
                    tiedtCellData.setText("=")
                    tiedtCellData.setSelection(1)
                }
            }
            tilCellData.setEndIconOnClickListener {
                val data = tiedtCellData.text.toString()
                if (data.length > 1 && data.endsWith("+").not() && data.endsWith("=").not()) {
                    tiedtCellData.setText("$data+")
                    tiedtCellData.setSelection(data.length + 1)
                } else {
                    tiedtCellData.setSelection(data.length)
                }
            }
            fabSave.setOnClickListener {
                val split = tiedtCellData.text.toString().split("+", "=")
                val sumOf =
                    split.map {
                        it.replace(",", ".")
                            .toBigDecimalOrNull() ?: BigDecimal.ZERO
                    }
                        .sumOf { it }
                alertDialog(
                    title = getString(R.string.edit_question),
                    content = "Сохранить сумму ${sumOf}?",
                    btnCancelText = getString(android.R.string.cancel),
                    btnOkText = getString(android.R.string.ok),
                    cancelable = true,
                    onConfirm = {
                        requireActivity().hideKeyboard()
                        vm.save(tiedtCellData.text.toString())
                    }
                )
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
        vm.cell.observe(viewLifecycleOwner) { cellData ->
            val data = cellData.takeIf { it.isNullOrBlank().not() } ?: "="
            editEnable = data.map { it in 'A'..'Z' }.any { it }.not()
            binding.tiedtCellData.setText(data)
        }
        vm.loading.observe(viewLifecycleOwner, { loading ->
            binding.progressBar.isVisible = loading
        })
        vm.categoriesData.observe(viewLifecycleOwner) { (_, currentCategory) ->
            binding.tvCategory.text = currentCategory?.categoryTitle
        }
    }
}
