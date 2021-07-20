package com.arnigor.incomeexpenses.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnigor.incomeexpenses.R
import com.arnigor.incomeexpenses.data.repository.prefs.PreferencesDataSource
import com.arnigor.incomeexpenses.data.repository.sheets.SheetsRepository
import com.arnigor.incomeexpenses.presentation.models.PaymentCategory
import com.arnigor.incomeexpenses.presentation.models.PaymentsAdapterModel
import com.arnigor.incomeexpenses.utils.mutableLiveData
import com.arnigor.incomeexpenses.utils.parseDataValues
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

class DetailsViewModel @Inject constructor(
    private val sheetsRepository: SheetsRepository,
    private val preferencesDataSource: PreferencesDataSource,
) : ViewModel() {
    private var firstLoadSum: BigDecimal = BigDecimal.ZERO
    private var paymentsDecimal: MutableList<BigDecimal> = mutableListOf()
    val categoriesData = mutableLiveData<CurrentCategoryData>()
    val adapterModels = mutableLiveData<List<PaymentsAdapterModel>>(emptyList())
    val paymentsSum = mutableLiveData<String>(null)
    val editEnable = mutableLiveData(false)
    val loading = mutableLiveData(false)
    val onBackPress = mutableLiveData(false)
    val toast = mutableLiveData<String>(null)
    val currentMonth = mutableLiveData<String>(null)
    val sumData = mutableLiveData<SumData>(null)
    private var docLink: String? = null

    private fun updateDocLink() {
        preferencesDataSource.getPrefString(R.string.preference_key_doc_link).let { docLink ->
            if (this.docLink != docLink) {
                this.docLink = docLink
            }
        }
    }

    fun initUI(
        categories: Array<PaymentCategory>?,
        category: PaymentCategory?,
        month: String?,
        cellData: String?
    ) {
        updateDocLink()
        categoriesData.value = CurrentCategoryData(
            categories = categories?.toList() ?: emptyList(),
            currentCategory = category
        )
        val data = cellData.takeIf { it.isNullOrBlank().not() } ?: "="
        editEnable.value = data.map { it in 'A'..'Z' }.any { it }.not()
        paymentsDecimal = data.parseDataValues()
        firstLoadSum = paymentsDecimal.sumOf { it }
        updateList()
        month?.let { it -> currentMonth.value = it }
    }

    private fun updateList() {
        calcSumDiff()
        adapterModels.value = paymentsDecimal.reversed().map { PaymentsAdapterModel(it) }
    }

    private fun calcSumDiff() {
        val curSum = paymentsDecimal.sumOf { it }
        val bigDecimal = curSum - firstLoadSum
        val diff = bigDecimal
            .takeIf { bigDecimal.compareTo(BigDecimal.ZERO) != 0 }?.let {
                "(${firstLoadSum}${if (it > BigDecimal.ZERO) "+" else ""}$it)"
            } ?: ""
        paymentsSum.value = "${curSum}$diff"
    }

    fun confirmSave(payments: MutableList<PaymentsAdapterModel>) {
        val sum = payments.sumOf { it.sum }
        val added = sum - firstLoadSum
        sumData.value = SumData(firstLoadSum, added, sum)
    }

    fun save(payments: MutableList<PaymentsAdapterModel>) {
        viewModelScope.launch {
            flow {
                val cellValue = StringBuilder().apply {
                    append("=")
                    for ((ind, value) in payments.reversed().withIndex()) {
                        if (ind != 0) {
                            append("+")
                        }
                        append(value.sum.toString().replace(".", ","))
                    }
                }.toString()
                emit(
                    sheetsRepository.writeValue(
                        link = docLink ?: "",
                        paymentCategory = categoriesData.value?.currentCategory,
                        month = currentMonth.value ?: "",
                        cellValue = cellValue
                    )
                )
            }
                .flowOn(Dispatchers.IO)
                .onStart { loading.value = true }
                .onCompletion { loading.value = false }
                .catch { handleError(it) }
                .collect { save ->
                    if (save) {
                        toast.value = "Значение сохранено"
                        onBackPress.value = true
                    } else {
                        toast.value = "Значение не сохранено"
                    }
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

    fun removeSum(position: Int) {
        paymentsDecimal.removeAt(paymentsDecimal.lastIndex - position)
        updateList()
    }

    fun addPayment(payment: String) {
        if (payment.isNotBlank()) {
            payment.replace(",", ".").toBigDecimalOrNull()?.let {
                paymentsDecimal.add(it)
                updateList()
            }
        }
    }

    fun itemChanged(position: Int, sum: String) {
        val index = paymentsDecimal.lastIndex - position
        paymentsDecimal.getOrNull(index)?.let {
            paymentsDecimal[index] = sum.toBigDecimalOrNull() ?: BigDecimal.ZERO
        }
        updateList()
    }
}