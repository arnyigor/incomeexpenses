package com.arnigor.incomeexpenses.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.arnigor.incomeexpenses.BuildConfig
import com.arnigor.incomeexpenses.R
import com.arnigor.incomeexpenses.databinding.FragmentHomeBinding
import com.arnigor.incomeexpenses.utils.viewBinding
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

    private var googleAccountCredential: GoogleAccountCredential? = null

    private var signedIn by Delegates.observable(false) { _, _, logined ->
        binding.tvData.isVisible = logined
        binding.mBtnGetData.isVisible = logined
        binding.spinMonths.isVisible = logined
        binding.tilSheetLink.isVisible = logined
        binding.mBtnSign.text = getString(
            if (logined) {
                R.string.sign_out
            } else {
                R.string.sign_in
            }
        )
    }

    @Inject
    lateinit var homeViewModel: HomeViewModel

    private val binding by viewBinding { FragmentHomeBinding.bind(it).also(::initBinding) }

    private fun initBinding(binding: FragmentHomeBinding) = with(binding) {
        mBtnGetData.setOnClickListener {
            homeViewModel.readSpreadsheet(binding.tiedtSheetLink.text.toString())
        }
        spinMonths.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = spinMonths.adapter.getItem(position).toString()
                homeViewModel.getSelectedMonthData(item)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        signedIn = account != null
        if (signedIn) {
            updateAccountCredential()
            homeViewModel.initSheetsApi(googleAccountCredential)
            homeViewModel.readSpreadsheet(binding.tiedtSheetLink.text.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCredential()
        if (BuildConfig.DEBUG) {
            binding.tiedtSheetLink.setText("https://docs.google.com/spreadsheets/d/14IWxF8lv_6ZaX5IUMaBwASSyX-hOV-OuCGs6Dj5MmXE/edit?usp=drivesdk")
        }
        homeViewModel.toast.observe(viewLifecycleOwner, {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })
        homeViewModel.data.observe(viewLifecycleOwner, {
            binding.tvData.text = it
        })
        homeViewModel.loading.observe(viewLifecycleOwner, { loading ->
            binding.progressBar.isVisible = loading
            binding.mBtnGetData.isEnabled = !loading
            binding.mBtnSign.isEnabled = !loading
            binding.tilSheetLink.isEnabled = !loading
        })
       binding.mBtnSign.setOnClickListener {
            btnSingnInClick()
        }
    }

    private fun btnSingnInClick() {
        if (signedIn) {
            getGoogleClient()?.signOut()?.addOnCompleteListener {
                signedIn = false
            }
        } else {
            startActivityForResult(getGoogleClient()?.signInIntent, RQ_GOOGLE_SIGN_IN)
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//                handleActivityResult(result.data)
//            }.launch(getGoogleClient()?.signInIntent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RQ_GOOGLE_SIGN_IN) {
            handleActivityResult(data)
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
                Log.d(TAG, "googleAccount:" + googleAccount.requestedScopes.map { it.scopeUri })
                Log.d(TAG, "googleAccount:" + googleAccount.email)
                Log.d(
                    TAG,
                    "googleAccount:grantedScopes" + googleAccount.grantedScopes.map { it.scopeUri })
                Toast.makeText(
                    requireContext(),
                    "Signed in as " + googleAccount.email,
                    Toast.LENGTH_SHORT
                ).show()
                updateAccountCredential()
                signedIn = googleAccountCredential?.selectedAccount != null
                homeViewModel.initSheetsApi(googleAccountCredential)
                homeViewModel.readSpreadsheet(binding.tiedtSheetLink.text.toString())
            }
            .addOnFailureListener { exception: Exception? ->
                Toast.makeText(requireContext(), "Unable to sign in", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Unable to sign in.", exception)
            }
    }

    private fun updateAccountCredential() {
        googleAccountCredential?.selectedAccount =
            GoogleSignIn.getLastSignedInAccount(requireContext())?.account
    }
}
