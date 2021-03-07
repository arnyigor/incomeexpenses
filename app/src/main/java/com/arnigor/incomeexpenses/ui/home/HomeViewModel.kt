package com.arnigor.incomeexpenses.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnigor.incomeexpenses.data.repository.sheets.SheetsRepository
import com.arnigor.incomeexpenses.data.repository.sheets.utils.getLastColumnNameFromColumnPosition
import com.arnigor.incomeexpenses.data.repository.sheets.utils.getSpeadsheetIdFromLink
import com.arnigor.incomeexpenses.ui.models.PaymentCategory
import com.arnigor.incomeexpenses.ui.models.PaymentType
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
        //        const val spreadsheetId = "1-DarqouKnAaiJObO1tV9RD_JL5KeCuhsjm3oC1xytzs"
//        const val range = "A1:AZ15"
    }

    val toast = mutableLiveData<String>(null)

    fun readSpreadsheet(link: String) {
        if (link.isNotBlank()) {
            startReadingSpreadsheet(link)
        } else {
            toast.value = "Пустая ссылка на документ"
        }
    }

    private fun startReadingSpreadsheet(link: String) {
        viewModelScope.launch {
            flow {
                val spreadsheetId = getSpeadsheetIdFromLink(link)
                val readSpreadSheetData = sheetsRepository.readSpreadSheetData(spreadsheetId)
                val gridProperties = readSpreadSheetData?.gridProperties
                val lastColumnNameFromColumnCount =
                    getLastColumnNameFromColumnPosition(gridProperties?.columnCount)
                val rowCount = gridProperties?.rowCount
                val range = "A1:$lastColumnNameFromColumnCount$rowCount"
                emit(sheetsRepository.readSpreadSheet(spreadsheetId, range))
            }
                .map { values ->
                    if (values.isNotEmpty()) {
                        val list = mutableListOf<String>()
                        val categories = mutableListOf<PaymentCategory>()
                        var outComeIndex = -1
                        var startMonths = -1
                        var outComeTotalIndex = -1
                        var inComeTotalIndex = -1
                        for (valuesIndexed in values.withIndex()) {
                            val valueIndex = valuesIndexed.index
                            val rows = valuesIndexed.value
                            list.add(rows.toString() + "\n")
                            for (rowsIndexed in rows.withIndex()) {
                                val rowIndex = rowsIndexed.index
                                val row = rowsIndexed.value
                                val outCome = rowIndex >= outComeIndex
                                when {
                                    valueIndex == 1 && rowIndex > 0 -> {
                                        val category = PaymentCategory(
                                            row.toString(),
                                            if (outCome) PaymentType.OUTCOME else PaymentType.INCOME,
                                            getLastColumnNameFromColumnPosition(rowIndex + 1)
                                        )
                                        if (category.categoryTitle == "СУММА" &&
                                            category.paymentType == PaymentType.INCOME
                                        ) {
                                            inComeTotalIndex = rowIndex
                                        }
                                        if (category.categoryTitle == "СУММА" &&
                                            category.paymentType == PaymentType.OUTCOME
                                        ) {
                                            outComeTotalIndex = rowIndex
                                        }
                                        categories.add(category)
                                    }
                                    row == "РАСХОД" -> {
                                        outComeIndex = rowIndex
                                    }
                                    row == "ЯНВАРЬ" -> {
                                        startMonths = valueIndex
                                    }
                                }
                            }
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