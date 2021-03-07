package com.arnigor.incomeexpenses.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.arnigor.incomeexpenses.R
import com.arnigor.incomeexpenses.data.manager.AuthenticationManager
import com.arnigor.incomeexpenses.databinding.FragmentHomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.firebase.auth.FirebaseAuth
import kotlin.properties.Delegates

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var googleAccountCredential: GoogleAccountCredential? = null
    private var mAuth: FirebaseAuth? = null

    private var signedIn by Delegates.observable(false) { _, oldValue, newValue ->
        binding.mBtnSign.text = getString(
            if (newValue) {
                R.string.sign_out
            } else {
                R.string.sign_in
            }
        )
    }

    private companion object {
        const val RQ_GOOGLE_SIGN_IN = 1000
        const val TAG = "HomeFragment"
    }

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        signedIn = account != null
        if (signedIn) {
            updateAccountCredential()
            homeViewModel.initSheets(googleAccountCredential)
            homeViewModel.readSpreadsheet()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        initCredential()
        homeViewModel.toast.observe(viewLifecycleOwner, {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })
        homeViewModel.text.observe(viewLifecycleOwner, {
            binding.tvInfo.text = it
        })
        binding.mBtnSign.setOnClickListener {
            if (signedIn) {
                getGoogleClient()?.signOut()?.addOnCompleteListener {
                    signedIn = false
                }
            } else {
                startActivityForResult(getGoogleClient()?.signInIntent, RQ_GOOGLE_SIGN_IN)
            }
        }
    }

    private fun initCredential() {
        googleAccountCredential = GoogleAccountCredential
            .usingOAuth2(requireContext(), listOf(*AuthenticationManager.SCOPES))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RQ_GOOGLE_SIGN_IN) {
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
                    homeViewModel.initSheets(googleAccountCredential)
                    homeViewModel.readSpreadsheet()
                }
                .addOnFailureListener { exception: Exception? ->
                    Toast.makeText(requireContext(), "Unable to sign in", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Unable to sign in.", exception)
                }
        }
    }

    private fun updateAccountCredential() {
        googleAccountCredential?.selectedAccount =
            GoogleSignIn.getLastSignedInAccount(requireContext())?.account
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}