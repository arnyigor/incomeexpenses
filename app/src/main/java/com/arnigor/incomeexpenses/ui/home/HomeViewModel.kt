package com.arnigor.incomeexpenses.ui.home

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arnigor.incomeexpenses.data.manager.AuthenticationManager
import com.arnigor.incomeexpenses.data.repository.sheets.SheetsRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class HomeViewModel : ViewModel() {
    private var sheetsRepository: SheetsRepository? = null
    private var authManager: AuthenticationManager? = null
    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text
    private val _manager = MutableLiveData<AuthenticationManager>()
    val manager: LiveData<AuthenticationManager> = _manager

    fun loginSuccessful() {
        println(authManager?.getLastSignedAccount()?.displayName)
        val upGoogleAccountCredential = authManager?.setUpGoogleAccountCredential()
        println(upGoogleAccountCredential)
        startReadingSpreadsheet("1D_zB9lCMLaHXF4b9-GaNOLJz7k72umxpGh-fJ6Gb6w4", range)
    }

    fun loginFailed() {

    }

    @SuppressLint("CheckResult")
    private fun startReadingSpreadsheet(spreadsheetId: String, range: String) {
        sheetsRepository?.readAllSpreadSheets()
            ?.map {
              it.get(spreadsheetId).keys
            }
            ?.subscribeOn(Schedulers.computation())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                println(it)
            }, {
                it.printStackTrace()
            })
    }

    fun initSheets(authManager: AuthenticationManager, sheetsRepository: SheetsRepository) {
        this.authManager = authManager
        this.sheetsRepository = sheetsRepository
    }

    fun tryToAuth() {
        _manager.value = authManager
    }

    companion object {
        val spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms"
        val range = "Class Data!A2:E"
    }
}