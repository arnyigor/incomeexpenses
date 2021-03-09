package com.arnigor.incomeexpenses.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnigor.incomeexpenses.R
import com.arnigor.incomeexpenses.data.repository.prefs.PreferencesDataSource
import com.arnigor.incomeexpenses.data.repository.sheets.SheetsRepository
import com.arnigor.incomeexpenses.presentation.models.PaymentCategory
import com.arnigor.incomeexpenses.presentation.models.PaymentData
import com.arnigor.incomeexpenses.presentation.models.PaymentType
import com.arnigor.incomeexpenses.presentation.models.SpreadSheetData
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
    private val sheetsRepository: SheetsRepository,
    private val preferencesDataSource: PreferencesDataSource,
) : ViewModel() {
    private var docLink: String? = null
    private var sheetdata: SpreadSheetData? = null
    private var carrentPayment: PaymentData? = null
    val toast = mutableLiveData<String>(null)
    val title = mutableLiveData<String>(null)
    val data = mutableLiveData<String>(null)
    val currentMonth = mutableLiveData<String>(null)
    val cell = mutableLiveData<String>(null)
    val hasDocLink = mutableLiveData(false)
    val categories = mutableLiveData<List<PaymentCategory>>()
    val loading = mutableLiveData(false)

    private fun startReadingSpreadsheet(link: String) {
        viewModelScope.launch {
            loading.value = true
            flow { emit(sheetsRepository.readSpreadSheet(link)) }
                .flowOn(Dispatchers.IO)
                .catch { handleError(it) }
                .collect {
                    sheetdata = it
                    getSelectedMonthData()
                    showCategories()
                    loading.value = false
                }
        }
    }

    fun loadDocTitle() {
        if (docLink.isNullOrBlank().not()) {
            viewModelScope.launch {
                flow { emit(sheetsRepository.readSpreadSheetData(docLink ?: "")) }
                    .flowOn(Dispatchers.IO)
                    .catch { handleError(it) }
                    .collect {
                        title.value = it?.properties?.title
                    }
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

    private fun getSheetDataByMonth(monthName: String? = null): Pair<String, String> {
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
        return month to sb.toString()
    }

    private fun handleError(mLastError: Throwable?) {
        loading.value = false
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
                toast.value = mLastError?.message
            }
        }
    }

    fun readSpreadsheet() {
        val link = docLink
        if (!link.isNullOrBlank()) {
            loadDocTitle()
            startReadingSpreadsheet(link)
        } else {
            toast.value = "Пустая ссылка на документ"
        }
    }

    fun getFullDataOfCategory(paymentCategory: PaymentCategory?, month: String) {
        viewModelScope.launch {
            flow { emit(sheetsRepository.readCell(docLink ?: "", paymentCategory, month)) }
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
                emit(sheetsRepository.writeValue(docLink ?: "", paymentCategory, month, value))
            }
                .flowOn(Dispatchers.IO)
                .onStart { loading.value = true }
                .onCompletion { loading.value = false }
                .catch { handleError(it) }
                .collect { save ->
                    if (save) {
                        toast.value = "Значение сохранено"
                        readSpreadsheet()
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
                .catch { handleError(it) }
                .collect { (month, value) ->
                    currentMonth.value = month[0].toUpperCase() +
                            month.substring(1).toLowerCase(Locale.getDefault())
                    data.value = value
                }
        }
    }

    fun initSheetsApi(googleAccountCredential: GoogleAccountCredential?) {
        sheetsRepository.initSheetsApi(googleAccountCredential)
    }

    fun updateDocLink() {
        preferencesDataSource.getPref(R.string.preference_key_doc_link).let { docLink ->
            if (this.docLink != docLink) {
                hasDocLink.value = docLink.isNullOrBlank().not()
                this.docLink = docLink
            }
        }
    }
}