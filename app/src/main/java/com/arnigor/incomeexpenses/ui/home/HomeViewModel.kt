package com.arnigor.incomeexpenses.ui.home

import androidx.lifecycle.ViewModel
import com.arnigor.incomeexpenses.data.repository.sheets.SheetsAPIDataSource
import com.arnigor.incomeexpenses.data.repository.sheets.SheetsRepository
import com.arnigor.incomeexpenses.utils.mutableLiveData
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class HomeViewModel : ViewModel() {
    private companion object {
        const val spreadsheetId = "1-DarqouKnAaiJObO1tV9RD_JL5KeCuhsjm3oC1xytzs"
        const val range = "A1:AF15"
    }

    private var sheetsRepository: SheetsRepository? = null
    val toast = mutableLiveData<String>(null)
    val text = mutableLiveData<String>(null)

    fun readSpreadsheet() {
        startReadingSpreadsheet(spreadsheetId, range)
    }

    private fun startReadingSpreadsheet(spreadsheetId: String, range: String) {
        sheetsRepository?.let { repository ->
            repository.readSpreadSheet(spreadsheetId, range)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    println(it)
                    text.value = "Values:$it"
                }, { mLastError ->
                    mLastError.printStackTrace()
                    when (mLastError) {
                        is GooglePlayServicesAvailabilityIOException -> {
                            toast.value =
                                "GooglePlayServicesAvailabilityIOException:${mLastError.connectionStatusCode}"
                        }
                        is UserRecoverableAuthIOException -> {
                            toast.value = "UserRecoverableAuthIOException:${mLastError.message}"
                        }
                        else -> {
                            toast.value = "The following error occurred:${mLastError.message}"
                        }
                    }
                })
        }
    }

    fun initSheets(googleAccountCredential: GoogleAccountCredential?) {
        googleAccountCredential?.let {
            sheetsRepository = SheetsRepository(
                SheetsAPIDataSource(
                    Sheets.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        JacksonFactory.getDefaultInstance(),
                        googleAccountCredential
                    )
                        .setApplicationName("Google Sheets API Android Quickstart")
                        .build()
                )
            )
        }
    }
}