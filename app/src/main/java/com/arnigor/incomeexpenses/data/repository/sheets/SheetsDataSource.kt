package com.arnigor.incomeexpenses.data.repository.sheets

import com.arnigor.incomeexpenses.data.model.SpreadsheetInfo
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.Spreadsheet

interface SheetsDataSource {
    /**
     * @param spreadsheetId doc id
     * @param spreadsheetRange example A1:B2
     * @param majorDimension ROWS(default), COLUMNS
     * @param valueRenderOption FORMATTED_VALUE,UNFORMATTED_VALUE,FORMULA
     */
    suspend fun readSpreadSheet(
        spreadsheetId: String,
        spreadsheetRange: String,
        majorDimension: String? = null,
        valueRenderOption: String? = null,
    ): List<List<Any>>

    fun initApi(googleAccountCredential: GoogleAccountCredential?)

    suspend fun readSpreadSheetData(spreadsheetId: String): SheetProperties?
    suspend fun createSpreadsheet(spreadSheet: Spreadsheet): SpreadsheetInfo

    suspend fun writeValue(spreadsheetId: String, range: String, cellValue: String?): Boolean
}
