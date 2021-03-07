package com.arnigor.incomeexpenses.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnigor.incomeexpenses.data.repository.sheets.SheetsRepository
import com.arnigor.incomeexpenses.utils.mutableLiveData
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val sheetsRepository: SheetsRepository
) : ViewModel() {
    private companion object {
        const val spreadsheetId = "1-DarqouKnAaiJObO1tV9RD_JL5KeCuhsjm3oC1xytzs"
        const val range = "A1:AZ15"
    }

    val toast = mutableLiveData<String>(null)
    val text = mutableLiveData<String>(null)

    fun readSpreadsheet() {
        startReadingSpreadsheet()
    }

    private fun startReadingSpreadsheet() {
        viewModelScope.launch {
            flow { emit(sheetsRepository.readSpreadSheet(spreadsheetId, range)) }
                .map { values ->
                    if (values.isNotEmpty()) {
                        val list = mutableListOf<String>()
                        for (row in values) {
//                        list.add(String.format("%s, %s\n", row[0], row[4]))
                        }
                        list
                    } else {
                        error("No data found.")
                    }
                }
                .flowOn(Dispatchers.IO)
                .catch { handleError(it) }
                .collect {
                    println(it)
                    text.value = "Values:$it"
                }
        }
    }

    private fun handleError(mLastError: Throwable?) {
        mLastError?.printStackTrace()
        when (mLastError) {
            is GooglePlayServicesAvailabilityIOException -> {
                toast.value =
                    "GooglePlayServicesAvailabilityIOException:${mLastError.connectionStatusCode}"
            }
            is UserRecoverableAuthIOException -> {
                toast.value = "UserRecoverableAuthIOException:${mLastError.message}"
            }
            else -> {
                toast.value = "The following error occurred:${mLastError?.message}"
            }
        }
    }

    fun initSheetsApi(googleAccountCredential: GoogleAccountCredential?) {
        sheetsRepository.initSheetsApi(googleAccountCredential)
    }
}