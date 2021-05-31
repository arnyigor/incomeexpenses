package com.arnigor.incomeexpenses.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnigor.incomeexpenses.R
import com.arnigor.incomeexpenses.data.repository.prefs.PreferencesDataSource
import com.arnigor.incomeexpenses.data.repository.sheets.SheetsRepository
import com.arnigor.incomeexpenses.presentation.models.PaymentCategory
import com.arnigor.incomeexpenses.utils.mutableLiveData
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class DetailsViewModel @Inject constructor(
    private val sheetsRepository: SheetsRepository,
    private val preferencesDataSource: PreferencesDataSource,
) : ViewModel() {
    val categoriesData = mutableLiveData<CurrentCategoryData>()
    val cell = mutableLiveData<String>(null)
    val loading = mutableLiveData(false)
    val onBackPress = mutableLiveData(false)
    val toast = mutableLiveData<String>(null)
    val currentMonth = mutableLiveData<String>(null)
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
        cell.value = cellData ?: "="
        month?.let { it -> currentMonth.value = it }
    }

    fun save(cellData: String) {
        viewModelScope.launch {
            flow {
                emit(
                    sheetsRepository.writeValue(
                        docLink ?: "",
                        categoriesData.value?.currentCategory,
                        currentMonth.value ?: "",
                        cellData
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
}