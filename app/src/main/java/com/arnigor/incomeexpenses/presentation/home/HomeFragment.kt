package com.arnigor.incomeexpenses.presentation.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.arnigor.incomeexpenses.R
import com.arnigor.incomeexpenses.databinding.FragmentHomeBinding
import com.arnigor.incomeexpenses.presentation.MainActivity
import com.arnigor.incomeexpenses.presentation.main.HeaderDataChangedListener
import com.arnigor.incomeexpenses.presentation.models.AdapterCategoryModel
import com.arnigor.incomeexpenses.presentation.models.PaymentCategory
import com.arnigor.incomeexpenses.utils.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.SheetsScopes
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject
import kotlin.properties.Delegates

class HomeFragment : Fragment(R.layout.fragment_home) {

    private companion object {
        const val TAG = "HomeFragment"
        const val RQ_GOOGLE_SIGN_IN = 1000
    }

    private var selectedCategory: PaymentCategory? = null
    private var currentMonth: String? = null
    private var headerDataChangedListener: HeaderDataChangedListener? = null
    private var sharedPreferences: SharedPreferences? = null
    private var googleAccountCredential: GoogleAccountCredential? = null
    private var categoriesAdapter: CategoriesAdapter? = null
    private var categoriesDataAdapter: CategoriesDataAdapter? = null
    private var edtState by Delegates.observable(true) { _, _, editState ->
        binding.tilCellData.isVisible = !editState
        if (editState) {
            binding.btnEdt.setText(R.string.edit)
        } else {
            binding.btnEdt.setText(R.string.save)
        }
    }
    private var signedIn by Delegates.observable(false) { _, _, logined ->
        binding.mBtnGetData.isVisible = logined
        binding.rvCategories.isVisible = hasLink && logined
        binding.spinMonths.isVisible = hasLink && logined
        binding.spinSort.isVisible = hasLink && logined
        binding.tvSort.isVisible = hasLink && logined
        binding.tvMonth.isVisible = hasLink && logined
        binding.tvFileData.isVisible = hasLink && logined
        binding.tilCellData.isVisible = hasLink && logined
        binding.spinCategories.isVisible = hasLink && logined
        binding.btnEdt.isVisible = hasLink && logined
        binding.tvCategoriesCaption.isVisible = hasLink && logined
        @DrawableRes
        val icon = if (logined) {
            R.drawable.ic_logout
        } else {
            R.drawable.ic_login
        }
        binding.mBtnSign.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                icon
            )
        )
    }

    private var hasLink by Delegates.observable(false) { _, _, hasLink ->
        binding.rvCategories.isVisible = hasLink
        binding.spinMonths.isVisible = hasLink
        binding.spinSort.isVisible = hasLink
        binding.tvSort.isVisible = hasLink
        binding.tvMonth.isVisible = hasLink
        binding.tilCellData.isVisible = hasLink
        binding.spinCategories.isVisible = hasLink
        binding.btnEdt.isVisible = hasLink
        binding.tvCategoriesCaption.isVisible = hasLink
        @DrawableRes
        val icon = if (hasLink) R.drawable.ic_refresh else R.drawable.ic_insert_link
        binding.mBtnGetData.setImageDrawable(icon.toDrawable(requireContext()))
    }

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            handleActivityResult(it.data)
        }

    private val binding by viewBinding { FragmentHomeBinding.bind(it).also(::initBinding) }

    @Inject
    lateinit var homeViewModel: HomeViewModel

    @SuppressLint("SetTextI18n")
    private fun initBinding(binding: FragmentHomeBinding) = with(binding) {
        tilCellData.setEndIconOnClickListener {
            val toString = tiedtCellData.text.toString()
            if (toString.endsWith("+").not()) {
                tiedtCellData.setText("$toString+")
            }
            tiedtCellData.setSelection(toString.length + 1)
        }
        categoriesAdapter = CategoriesAdapter(requireContext())
        spinCategories.adapter = categoriesAdapter
    }

    private fun selectCategory(item: AdapterCategoryModel) {
        alertDialog(
            title = getString(R.string.edit_question),
            content = "Редактировать сумму категории ${item.title?.toString(requireContext())}?",
            btnCancelText = getString(android.R.string.cancel),
            btnOkText = getString(android.R.string.ok),
            cancelable = true,
            onConfirm = {
                categoriesAdapter?.items?.getIndexBy { isSameCategory(it, item) }?.let {
                    val (selectedCategory, month) = getCategoriesAndMonths(it)
                    homeViewModel.getFullDataOfCategory(selectedCategory, month)
                    this.selectedCategory = selectedCategory
                    this.currentMonth = month
                }
            }
        )
    }

    private fun isSameCategory(
        category: PaymentCategory,
        item: AdapterCategoryModel
    ) = category.categoryTitle?.lowercase() == item.title?.toString(requireContext())
        ?.lowercase()

    private fun getCategoriesAndMonths(position: Int): Pair<PaymentCategory?, String> {
        val spinMonths = binding.spinMonths
        return Pair(
            categoriesAdapter?.getItem(position),
            spinMonths.adapter.getItem(spinMonths.selectedItemPosition).toString()
        )
    }

    private fun btnSingnInClick() {
        if (signedIn) {
            alertDialog(
                getString(R.string.logout),
                onConfirm = {
                    getGoogleClient()?.signOut()?.addOnCompleteListener {
                        signedIn = false
                        hasLink = false
                        headerDataChangedListener?.headerDataChanged(null, null)
                        updatePrefs(
                            MainActivity.PREF_KEY_USER_EMAIl to null,
                            MainActivity.PREF_KEY_USER_NAME to null,
                            getString(R.string.preference_key_doc_link) to ""
                        )
                    }
                },
                btnCancelText = getString(android.R.string.cancel),
                onCancel = {},
                cancelable = true
            )
        } else {
            getResult.launch(getGoogleClient()?.signInIntent)
        }
    }

    private fun initCredential() {
        googleAccountCredential = GoogleAccountCredential
            .usingOAuth2(requireContext(), listOf(SheetsScopes.SPREADSHEETS))
            .setBackOff(ExponentialBackOff())
        updateAccountCredential()
    }

    private fun getGoogleClient(): GoogleSignInClient? {
        return GoogleSignIn.getClient(
            requireContext(),
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
                .requestEmail()
                .build()
        )
    }

    private fun handleActivityResult(data: Intent?) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
            .addOnSuccessListener { googleAccount: GoogleSignInAccount ->
                Toast.makeText(
                    requireContext(),
                    "Signed in as " + googleAccount.email,
                    Toast.LENGTH_SHORT
                ).show()
                updateAccountCredential()
                signedIn = googleAccountCredential?.selectedAccount != null
                homeViewModel.initSheetsApi(googleAccountCredential)
                if (hasLink) {
                    homeViewModel.readSpreadsheet()
                }
            }
            .addOnFailureListener { exception: Exception? ->
                Toast.makeText(requireContext(), "Unable to sign in", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Unable to sign in.", exception)
            }
    }

    private fun updateAccountCredential() {
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        val selectedAccount = account?.account
        if (selectedAccount?.name != googleAccountCredential?.selectedAccount?.name) {
            googleAccountCredential?.selectedAccount = selectedAccount
            val displayName = account?.displayName
            val email = account?.email
            updatePrefs(
                MainActivity.PREF_KEY_USER_EMAIl to email,
                MainActivity.PREF_KEY_USER_NAME to displayName
            )
        }
    }

    private fun updatePrefs(vararg pairs: Pair<String, String?>) {
        val edit = sharedPreferences?.edit()
        for ((key, value) in pairs) {
            edit?.putString(key, value)
        }
        edit?.apply()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        if (context is HeaderDataChangedListener) {
            headerDataChangedListener = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateTitle(getString(R.string.app_name))
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        initCredential()
        updateSignIn()
        observeData()
        uiListeners()
        with(homeViewModel) {
            updateDocLink()
            loadDocTitle()
            readSpreadsheet()
        }
    }

    override fun onResume() {
        super.onResume()
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        headerDataChangedListener?.headerDataChanged(account?.displayName, account?.email)
    }

    private fun uiListeners() = with(binding) {
        mBtnSign.setOnClickListener {
            btnSingnInClick()
        }
        spinMonths.setSelection(0, false)
        spinMonths.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                homeViewModel.getSelectedMonthData(
                    spinMonths.adapter.getItem(position).toString()
                )
                edtState = true
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        spinSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                homeViewModel.getSelectedMonthData(
                    spinMonths.adapter.getItem(spinMonths.selectedItemPosition).toString(),
                    position
                )
                edtState = true
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        spinCategories.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                edtState = true
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        btnEdt.setOnClickListener {
            val (selectedCategory, month) = getCategoriesAndMonths(binding.spinCategories.selectedItemPosition)
            if (edtState) {
                homeViewModel.getFullDataOfCategory(selectedCategory, month)
            } else {
                homeViewModel.writeValue(
                    selectedCategory,
                    month,
                    tiedtCellData.text.toString()
                )
            }
            edtState = !edtState
        }
        mBtnGetData.setOnClickListener {
            if (hasLink) {
                homeViewModel.readSpreadsheet()
            } else {
                binding.root.findNavController()
                    .navigate(HomeFragmentDirections.actionNavHomeToNavSettings())
            }
        }
        categoriesDataAdapter = CategoriesDataAdapter(
            onItemEdit = (::selectCategory)
        )
        rvCategories.apply {
            layoutManager = object : LinearLayoutManager(requireContext()) {
                override fun canScrollVertically(): Boolean = false
            }
            adapter = categoriesDataAdapter
        }
        tiedtCellData.doWhenEnterClicked {
            btnEdt.performClick()
        }
    }

    private fun observeData() {
        homeViewModel.toast.observe(viewLifecycleOwner, {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })
        homeViewModel.categoriesData.observe(viewLifecycleOwner, { data ->
            if (categoriesDataAdapter?.currentList?.isEmpty() == false) {
                categoriesDataAdapter?.submitList(emptyList())
            }
            categoriesDataAdapter?.submitList(data)
            binding.spinCategories.isVisible = true
            binding.btnEdt.isVisible = true
            binding.tvCategoriesCaption.isVisible = true
            edtState = true
        })
        homeViewModel.loading.observe(viewLifecycleOwner, { loading ->
            binding.progressBar.isVisible = loading
            binding.mBtnGetData.isEnabled = !loading
            binding.mBtnSign.isEnabled = !loading
            binding.tilCellData.isEnabled = !loading
            binding.btnEdt.isEnabled = !loading
            binding.spinCategories.isEnabled = !loading
            binding.spinMonths.isEnabled = !loading
        })
        homeViewModel.categories.observe(viewLifecycleOwner, { categories ->
            categoriesAdapter?.clear()
            categoriesAdapter?.addAll(categories)
            categoriesAdapter?.notifyDataSetChanged()
        })
        homeViewModel.cell.observe(viewLifecycleOwner, { cell ->
            findNavController().navigate(
                HomeFragmentDirections.actionNavHomeToNavDetails(
                    categories = categoriesAdapter?.items?.toTypedArray(),
                    category = selectedCategory,
                    month = currentMonth,
                    cellData = cell
                )
            )
        })
        homeViewModel.hasDocLink.observe(viewLifecycleOwner, { hasLink ->
            this.hasLink = hasLink
        })
        homeViewModel.fileData.observe(viewLifecycleOwner, { data ->
            binding.tvFileData.isVisible = data.isNullOrBlank().not()
            binding.tvFileData.text = data
            binding.tvFileData.text = data
        })
        homeViewModel.title.observe(viewLifecycleOwner, { title ->
            updateTitle(title)
        })
        homeViewModel.currentMonth.observe(viewLifecycleOwner, { month ->
            val adapter = binding.spinMonths.adapter as ArrayAdapter<String>
            binding.spinMonths.setSelection(adapter.getPosition(month))
        })
        homeViewModel.modifiedData.observe(viewLifecycleOwner, { data ->
            binding.spinMonths
        })
    }

    private fun updateTitle(title: String?) {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = title
    }

    private fun updateSignIn() {
        signedIn = GoogleSignIn.getLastSignedInAccount(requireContext()) != null
        if (signedIn) {
            updateAccountCredential()
            homeViewModel.initSheetsApi(googleAccountCredential)
            if (hasLink) {
                homeViewModel.readSpreadsheet()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RQ_GOOGLE_SIGN_IN) {
            handleActivityResult(data)
        }
    }
}
