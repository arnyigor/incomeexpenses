package com.arnigor.incomeexpenses.data.repository.sheets

import com.arnigor.incomeexpenses.presentation.models.PaymentCategory
import com.arnigor.incomeexpenses.presentation.models.PaymentData
import com.arnigor.incomeexpenses.presentation.models.SpreadSheetData
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.sheets.v4.model.Spreadsheet

interface SheetsRepository {
    suspend fun readSpreadSheet(link: String): SpreadSheetData
    fun initSheetsApi(credential: GoogleAccountCredential?)
    suspend fun readSpreadSheetData(link: String): Spreadsheet?
    suspend fun readCell(
        link: String,
        paymentCategory: PaymentCategory?,
        month: String
    ): PaymentData

    suspend fun writeValue(
        link: String,
        paymentCategory: PaymentCategory?,
        month: String,
        cellValue: String?
    ): Boolean
}
