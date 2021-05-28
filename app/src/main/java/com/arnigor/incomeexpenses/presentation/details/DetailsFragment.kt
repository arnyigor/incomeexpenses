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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.arnigor.incomeexpenses.R
import com.arnigor.incomeexpenses.databinding.FragmentDetailsBinding
import com.arnigor.incomeexpenses.presentation.home.CategoriesAdapter
import com.arnigor.incomeexpenses.utils.getIndexBy
import com.arnigor.incomeexpenses.utils.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


class DetailsFragment : Fragment(R.layout.fragment_details) {
    private var categoriesAdapter: CategoriesAdapter? = null

    @Inject
    lateinit var vm: DetailsViewModel

    private val args: DetailsFragmentArgs by navArgs()

    private val binding by viewBinding { FragmentDetailsBinding.bind(it).also(::initUI) }

    private fun initUI(binding: FragmentDetailsBinding) {
        with(binding) {
            categoriesAdapter = CategoriesAdapter(requireContext())
            spinCategories.adapter = categoriesAdapter
        }
    }

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
            tilCellData.setEndIconOnClickListener {
                val data = tiedtCellData.text.toString()
                if (data.endsWith("+").not()) {
                    tiedtCellData.setText("$data+")
                }
                tiedtCellData.setSelection(data.length + 1)
            }
            fabSave.setOnClickListener {
                vm.save(tiedtCellData.text.toString())
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
        vm.onBackPress.observe(viewLifecycleOwner, {
             if(it){
                 requireActivity().onBackPressed()
             }
        })
        vm.cell.observe(viewLifecycleOwner) { cellData ->
            binding.tiedtCellData.setText(cellData.takeIf { it.isNullOrBlank().not() } ?: "=")
        }
        vm.loading.observe(viewLifecycleOwner, { loading ->
            binding.progressBar.isVisible = loading
        })
        vm.categoriesData.observe(viewLifecycleOwner) { (categories, currentCategory) ->
            categoriesAdapter?.clear()
            categoriesAdapter?.addAll(categories)
            categoriesAdapter?.notifyDataSetChanged()
            categories.getIndexBy { it.categoryTitle?.lowercase() == currentCategory?.categoryTitle?.lowercase() }
                ?.let {
                    binding.spinCategories.setSelection(it)
                }
        }
    }
}
