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
import javax.inject.Inject
import kotlin.properties.Delegates

class HomeViewModel @Inject constructor(
    private val sheetsRepository: SheetsRepository,
    private val preferencesDataSource: PreferencesDataSource,
) : ViewModel() {
    private var docLink: String? = null
    private var sheetdata: SpreadSheetData? = null
    private var carrentPayment: PaymentData? = null
    private var sortTypePosition by Delegates.observable(0, { _, oldValue, newValue ->
        if (oldValue != newValue) {
            if (isSaveSortPosition()) {
                preferencesDataSource.put(
                    R.string.pref_key_sort_position,
                    newValue
                )
            }
        }
    })
    val toast = mutableLiveData<String>(null)
    val spinSortPosition = mutableLiveData<Int>(0)
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
            if (isSaveSortPosition()) {
                sortTypePosition =
                    preferencesDataSource.getPrefInt(R.string.pref_key_sort_position, 0)
                spinSortPosition.value = sortTypePosition
            }
            flow { emit(sheetsRepository.readSpreadSheet(link)) }
                .flowOn(Dispatchers.IO)
                .catch { handleError(it) }
                .collect {
                    sheetdata = it
                    showSortedMonthData(sortTypePosition = sortTypePosition)
                    showCategories()
                    loading.value = false
                }
        }
    }

    private fun isSaveSortPosition() =
        preferencesDataSource.getPrefBool(R.string.pref_key_save_sort, true)

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
        val mainData = mutableListOf<AdapterCategoryModel>()
        val totalData = mutableListOf<AdapterCategoryModel>()
        var totalIncome = BigDecimal.ZERO
        var totalOutcome = BigDecimal.ZERO
        val sortedPayments =
            sheetdata?.monthsData?.find { month.normalize() == it.monthName?.normalize() }
                ?.payments?.let { sortPayments(it) } ?: emptyList()
        for (payment in sortedPayments) {
            val value = payment.value ?: BigDecimal.ZERO
            val paymentCategory = payment.paymentCategory
            if (paymentCategory?.paymentType == PaymentType.INCOME) {
                totalIncome += value
            } else {
                totalOutcome += value
            }
            mainData.add(
                AdapterCategoryModel(
                    SimpleString(paymentCategory?.categoryTitle),
                    value?.formatNumberWithSpaces(),
                    paymentCategory?.paymentType
                )
            )
        }
        totalData.addSums(totalIncome, totalOutcome)
        val list = if (sortTypePosition in 3..4) {
            totalData + mainData
        } else {
            mainData + totalData
        }
        return month to list
    }

    private fun MutableList<AdapterCategoryModel>.addSums(
        totalIncome: BigDecimal,
        totalOutcome: BigDecimal
    ) {
        add(
            AdapterCategoryModel(
                ResourceString(R.string.income),
                totalIncome.formatNumberWithSpaces(),
                PaymentType.INCOME_SUM
            )
        )
        add(
            AdapterCategoryModel(
                ResourceString(R.string.outcome),
                totalOutcome.formatNumberWithSpaces(),
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

    private fun List<Payment>.sortByIncomeOutcome(): List<Payment> {
        return this.sortedWith { p1, p2 ->
            val paymentType1 = p1.paymentCategory?.paymentType
            val paymentType2 = p2.paymentCategory?.paymentType
            when {
                paymentType1 == PaymentType.INCOME && paymentType2 == PaymentType.OUTCOME -> 1
                paymentType1 == PaymentType.OUTCOME && paymentType2 == PaymentType.INCOME -> -1
                else -> 0
            }
        }
    }

    private fun sortPayments(payments: List<Payment>): List<Payment> {
        return when (sortTypePosition) {
            0 -> payments.sortedBy { it.paymentCategory?.categoryTitle }
            1 -> payments.sortedByDescending { it.value }
            2 -> payments.sortedBy { it.value }
            4 -> payments.sortedByDescending { it.value }
            else -> payments.sortedBy { it.paymentCategory?.categoryTitle }
        }.sortByIncomeOutcome()
    }

    fun handleError(mLastError: Throwable?) {
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
            viewModelScope.launch {
                delay(2000L)
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

    fun showSortedMonthData(monthName: String? = null, sortTypePosition: Int = 0) {
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
        preferencesDataSource.getPrefString(R.string.preference_key_doc_link).let { docLink ->
            if (this.docLink != docLink) {
                hasDocLink.value = docLink.isNullOrBlank().not()
                this.docLink = docLink
            }
        }
    }
}