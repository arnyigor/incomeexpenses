package com.arnigor.incomeexpenses.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnigor.incomeexpenses.R
import com.arnigor.incomeexpenses.data.model.SpreadsheetModifiedData
import com.arnigor.incomeexpenses.data.repository.prefs.PreferencesDataSource
import com.arnigor.incomeexpenses.data.repository.sheets.SheetsRepository
import com.arnigor.incomeexpenses.presentation.models.*
import com.arnigor.incomeexpenses.utils.*
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.sheets.v4.model.Spreadsheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal

class HomeViewModel(
    private val sheetsRepository: SheetsRepository,
    private val preferencesDataSource: PreferencesDataSource,
) : ViewModel() {
    private var sortTypePosition: Int = 0
    private var docLink: String? = null
    private var sheetdata: SpreadSheetData? = null
    private var carrentPayment: PaymentData? = null
    val toast = mutableLiveData<String>(null)
    val title = mutableLiveData<String>(null)
    val fileData = mutableLiveData<String>(null)
    val categoriesData = mutableLiveData<List<AdapterCategoryModel>>(emptyList())
    val currentMonth = mutableLiveData<String>(null)
    val cell = mutableLiveData<String>(null)
    val hasDocLink = mutableLiveData(false)
    val categories = mutableLiveData<List<PaymentCategory>>()
    val loading = mutableLiveData(false)
    val modifiedData = mutableLiveData<SpreadsheetModifiedData>()

    private fun startReadingSpreadsheet(link: String) {
        viewModelScope.launch {
            loading.value = true
            flow { emit(sheetsRepository.readSpreadSheet(link)) }
                .flowOn(Dispatchers.IO)
                .catch { handleError(it) }
                .collect {
                    sheetdata = it
                    getSelectedMonthData(sortTypePosition = sortTypePosition)
                    showCategories()
                    loading.value = false
                }
        }
    }

    fun loadDocTitle() {
        if (docLink.isNullOrBlank().not()) {
            viewModelScope.launch {
                flow {
                    emit(
                        sheetsRepository.readSpreadSheetData(
                            docLink
                                ?: ""
                        )
                    )
                }.zip(flow {
                    emit(sheetsRepository.getModifiedData(docLink ?: ""))
                }) { spreadsheet: Spreadsheet?, spreadsheetModifiedData: SpreadsheetModifiedData ->
                    spreadsheet to spreadsheetModifiedData
                }
                    .map { (sheet, data) ->
                        ("Документ: ${sheet?.properties?.title}") to StringBuilder().apply {
                            append("Изменён: ").append(data.modifiedTime)
                            if (data.duration != null) {
                                append("(")
                                append(data.duration)
                                append(" мин. назад)")
                            }
                            append("\n")
                        }.toString()
                    }
                    .flowOn(Dispatchers.IO)
                    .catch { handleError(it) }
                    .collect { (titleVal, data) ->
                        title.value = titleVal
                        fileData.value = data
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

    private fun getSheetDataByMonth(
        monthName: String? = null
    ): Pair<String, List<AdapterCategoryModel>> {
        val month = monthName ?: DateTimeUtils.getCurrentMonthRuFull()
        val list = mutableListOf<AdapterCategoryModel>().apply {
            var totalIncome = BigDecimal.ZERO
            var totalOutcome = BigDecimal.ZERO
            sheetdata?.monthsData?.find { month.normalize() == it.monthName?.normalize() }
                ?.payments?.let { payments ->
                    for (payment in sortPayments(payments)) {
                        val value = payment.value ?: BigDecimal.ZERO
                        val paymentCategory = payment.paymentCategory
                        if (paymentCategory?.paymentType == PaymentType.INCOME) {
                            totalIncome += value
                        } else {
                            totalOutcome += value
                        }
                        add(
                            AdapterCategoryModel(
                                SimpleString(paymentCategory?.categoryTitle),
                                value?.formatNumberWithSpaces(),
                                paymentCategory?.paymentType
                            )
                        )
                    }
                }
            add(
                AdapterCategoryModel(
                    ResourceString(R.string.income),
                    totalIncome?.formatNumberWithSpaces(),
                    PaymentType.INCOME_SUM
                )
            )
            add(
                AdapterCategoryModel(
                    ResourceString(R.string.outcome),
                    totalOutcome?.formatNumberWithSpaces(),
                    PaymentType.OUTCOME_SUM
                )
            )
            add(
                AdapterCategoryModel(
                    ResourceString(R.string.balance),
                    (totalIncome - totalOutcome).formatNumberWithSpaces(),
                    PaymentType.BALANCE
                )
            )
        }
        return month to list
    }

    private fun sortPayments(payments: List<Payment>): List<Payment> {
        return getSortedList(payments, sortTypePosition)
            .sortedWith { p1, p2 ->
                val paymentType1 = p1.paymentCategory?.paymentType
                val paymentType2 = p2.paymentCategory?.paymentType
                when {
                    paymentType1 == PaymentType.INCOME && paymentType2 == PaymentType.OUTCOME -> 1
                    paymentType1 == PaymentType.OUTCOME && paymentType2 == PaymentType.INCOME -> -1
                    else -> 0
                }
            }
    }

    private fun getSortedList(
        list: List<Payment>,
        sortTypePosition: Int
    ): List<Payment> {
        return when (sortTypePosition) {
            0 -> {
                list.sortedBy { it.paymentCategory?.categoryTitle }
            }
            1 -> {
                list.sortedByDescending { it.value }
            }
            2 -> {
                list.sortedBy { it.value }
            }
            else -> {
                list.sortedBy { it.paymentCategory?.categoryTitle }
            }
        }
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

    fun readSpreadsheet(delay: Long = 0) {
        val link = docLink
        if (!link.isNullOrBlank()) {
            if (delay != 0L) {
                viewModelScope.launch {
                    delay(delay)
                    loadDocTitle()
                }
            } else {
                loadDocTitle()
            }
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
                        readSpreadsheet(2000)
                    } else {
                        toast.value = "Значение не сохранено"
                    }
                }
        }
    }

    fun getSelectedMonthData(monthName: String? = null, sortTypePosition: Int = 0) {
        this.sortTypePosition = sortTypePosition
        viewModelScope.launch {
            flow { emit(getSheetDataByMonth(monthName)) }
                .flowOn(Dispatchers.IO)
                .catch { handleError(it) }
                .collect { (month, value) ->
                    currentMonth.value = month.toFirstUpperCase()
                    categoriesData.value = value
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