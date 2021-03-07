package com.arnigor.incomeexpenses.data.repository.sheets

import com.arnigor.incomeexpenses.data.model.SpreadsheetInfo
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.Spreadsheet

interface SheetsDataSource {
    suspend fun readSpreadSheet(
        spreadsheetId: String,
        spreadsheetRange: String
    ): List<List<Any>>

    fun initApi(googleAccountCredential: GoogleAccountCredential?)

    suspend fun readSpreadSheetData(spreadsheetId: String): SheetProperties?
    suspend fun createSpreadsheet(spreadSheet: Spreadsheet): SpreadsheetInfo
}
