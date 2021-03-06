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
import com.arnigor.incomeexpenses.data.repository.sheets.SheetsAPIDataSource
import com.arnigor.incomeexpenses.data.repository.sheets.SheetsRepository
import com.arnigor.incomeexpenses.databinding.FragmentHomeBinding
import com.arnigor.incomeexpenses.utils.dump
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.SheetsScopes.DRIVE_FILE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.*


class HomeFragment : Fragment() {
    private var mAuth: FirebaseAuth? = null
    companion object {
        const val RQ_GOOGLE_SIGN_IN = 1000
        const val TAG = "HomeFragment"
    }

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var mGoogleSignInClient: GoogleSignInClient? = null

    private fun initAuth() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(requireContext(), gso)
        mGoogleSignInClient = client
        val googleAccountCredential = GoogleAccountCredential
            .usingOAuth2(requireContext(), listOf(*AuthenticationManager.SCOPES))
            .setBackOff(ExponentialBackOff())
        val authManager = AuthenticationManager(
            lazyOf(requireContext()),
            client,
            googleAccountCredential
        )
        val sheetsApiDataSource = SheetsAPIDataSource(
            authManager,
            AndroidHttp.newCompatibleTransport(),
            JacksonFactory.getDefaultInstance()
        )
        homeViewModel.initSheets(authManager, SheetsRepository(sheetsApiDataSource))
        homeViewModel.tryToAuth()
    }

    private fun requestSignIn() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(SheetsScopes.DRIVE))
            .requestScopes(Scope(SheetsScopes.DRIVE_FILE))
            .requestScopes(Scope(SheetsScopes.SPREADSHEETS_READONLY))
            .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
            .build()
        val client = GoogleSignIn.getClient(requireContext(), signInOptions)
        startActivityForResult(client.signInIntent, RQ_GOOGLE_SIGN_IN)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        initAuth()
        binding.siButton.setOnClickListener {
            signIn()
        }
        homeViewModel.text.observe(viewLifecycleOwner, {
            binding.tvInfo.text = it
        })
        homeViewModel.manager.observe(viewLifecycleOwner, {
            startActivityForResult(it.googleSignInClient.signInIntent, RQ_GOOGLE_SIGN_IN)
        })
    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth?.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
         binding.tvInfo.text = currentUser?.displayName
    }

    private fun signIn() {
        val intent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(intent, RQ_GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println(data.dump())
        if (requestCode == RQ_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                Log.d(TAG, "firebaseAuthWithGoogleidToken:" + account.idToken!!)
                homeViewModel.loginSuccessful()
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // ...
            }
        }

        if (requestCode == RQ_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener { googleAccount: GoogleSignInAccount ->
                    Toast.makeText(
                        requireContext(),
                        "Signed in as " + googleAccount.email,
                        Toast.LENGTH_SHORT
                    ).show()

                    // Use the authenticated account to sign in to the Drive service.
                    val credential = GoogleAccountCredential.usingOAuth2(
                        requireContext(), Collections.singleton(DRIVE_FILE)
                    )
//                    credential.selectedAccount = googleAccount.account
//                    val googleDriveService: Drive = Builder(
//                        AndroidHttp.newCompatibleTransport(),
//                        GsonFactory(),
//                        credential
//                    )
//                        .setApplicationName("Drive API Migration")
//                        .build()
//                    mDriveServiceHelper = DriveServiceHelper(googleDriveService)
                }
                .addOnFailureListener { exception: Exception? ->
                    Log.e(TAG, "Unable to sign in.", exception)
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}