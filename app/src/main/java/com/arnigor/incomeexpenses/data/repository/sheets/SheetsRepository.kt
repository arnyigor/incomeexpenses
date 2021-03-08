package com.arnigor.incomeexpenses.data.repository.sheets

import com.arnigor.incomeexpenses.ui.models.SpreadSheetData
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.sheets.v4.model.SheetProperties

interface SheetsRepository {
    suspend fun readSpreadSheet(
        link: String
    ): SpreadSheetData

    fun initSheetsApi(credential: GoogleAccountCredential?)

    suspend fun readSpreadSheetData(spreadsheetId: String): SheetProperties?
}
