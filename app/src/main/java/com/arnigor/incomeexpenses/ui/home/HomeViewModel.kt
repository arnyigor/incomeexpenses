package com.arnigor.incomeexpenses.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnigor.incomeexpenses.data.repository.sheets.SheetsRepository
import com.arnigor.incomeexpenses.ui.models.PaymentCategory
import com.arnigor.incomeexpenses.ui.models.PaymentData
import com.arnigor.incomeexpenses.ui.models.PaymentType
import com.arnigor.incomeexpenses.ui.models.SpreadSheetData
import com.arnigor.incomeexpenses.utils.DateTimeUtils
import com.arnigor.incomeexpenses.utils.mutableLiveData
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.*

class HomeViewModel(
    private val sheetsRepository: SheetsRepository
) : ViewModel() {
    private var link: String? = null
    private var sheetdata: SpreadSheetData? = null
    private var carrentPayment: PaymentData? = null
    val toast = mutableLiveData<String>(null)
    val data = mutableLiveData<String>(null)
    val cell = mutableLiveData<String>(null)
    val categories = mutableLiveData<List<PaymentCategory>>()
    val loading = mutableLiveData(false)

    fun readSpreadsheet(link: String) {
        if (link.isNotBlank()) {
            this.link = link
            startReadingSpreadsheet(link)
        } else {
            toast.value = "Пустая ссылка на документ"
        }
    }


    private fun startReadingSpreadsheet(link: String) {
        viewModelScope.launch {
            flow { emit(sheetsRepository.readSpreadSheet(link)) }
                .flowOn(Dispatchers.IO)
                .onStart { loading.value = true }
                .onCompletion { loading.value = false }
                .catch { handleError(it) }
                .collect {
                    sheetdata = it
                    getSelectedMonthData()
                    showCategories()
                }
        }
    }

    private fun showCategories() {
        viewModelScope.launch {
            flowOf(sheetdata?.categories ?: emptyList())
                .map { list -> list.sortedBy { it.categoryTitle } }
                .flowOn(Dispatchers.IO)
                .catch { handleError(it) }
                .collect {
                    categories.value = it
                }
        }
    }

    fun getFullDataOfCategory(paymentCategory: PaymentCategory?, month: String) {
        viewModelScope.launch {
            flow { emit(sheetsRepository.readCell(link ?: "", paymentCategory, month)) }
                .flowOn(Dispatchers.IO)
                .onStart { loading.value = true }
                .onCompletion { loading.value = false }
                .catch { handleError(it) }
                .collect {
                    carrentPayment = it
                    cell.value = it.value ?: ""
                }
        }
    }

    fun writeValue(paymentCategory: PaymentCategory?, month: String, value: String?) {
        viewModelScope.launch {
            flow {
                emit(sheetsRepository.writeValue(link ?: "", paymentCategory, month, value))
            }
                .flowOn(Dispatchers.IO)
                .onStart { loading.value = true }
                .onCompletion { loading.value = false }
                .catch { handleError(it) }
                .collect { save ->
                    if (save) {
                        toast.value = "Значение сохранено"
                        if (!link.isNullOrBlank()) {
                            link?.let { startReadingSpreadsheet(it) }
                        } else {
                            toast.value = "Пустая ссылка на документ"
                        }
                    } else {
                        toast.value = "Значение не сохранено"
                    }
                }
        }
    }

    fun getSelectedMonthData(monthName: String? = null) {
        viewModelScope.launch {
            flow { emit(getSheetDataByMonth(monthName)) }
                .flowOn(Dispatchers.IO)
                .onStart { loading.value = true }
                .onCompletion { loading.value = false }
                .catch { handleError(it) }
                .collect {
                    data.value = it
                }
        }
    }

    private fun getSheetDataByMonth(monthName: String? = null): String {
        val month = monthName ?: DateTimeUtils.getCurrentMonthRuFull()
        val sb = StringBuilder().apply {
            val md =
                sheetdata?.monthsData?.find { month.toUpperCase(Locale.getDefault()) == it.monthName }
            var totalIncome = BigDecimal.ZERO
            var totalOutcome = BigDecimal.ZERO
            md?.payments?.let { payments ->
                for (payment in payments) {
                    val value = payment.value ?: BigDecimal.ZERO
                    val paymentCategory = payment.paymentCategory
                    if (paymentCategory?.paymentType == PaymentType.INCOME) {
                        totalIncome += value
                    } else {
                        totalOutcome += value
                    }
                    append("Категория:${paymentCategory?.categoryTitle} сумма:$value\n")
                }
            }
            append("Входящие:$totalIncome\n")
            append("Траты:$totalOutcome\n")
            append("Остаток:${totalIncome - totalOutcome}\n")
        }
        return "Месяц:$month,Итого:\n$sb"
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