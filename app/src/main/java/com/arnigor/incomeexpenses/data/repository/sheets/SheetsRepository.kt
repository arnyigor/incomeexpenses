package com.arnigor.incomeexpenses.data.repository.sheets

import com.arnigor.incomeexpenses.data.model.Person
import com.arnigor.incomeexpenses.data.model.SpreadsheetInfo
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.Spreadsheet
import io.reactivex.Observable
import io.reactivex.Single

class SheetsRepository(private val sheetsAPIDataSource: SheetsAPIDataSource) {

    fun readSpreadSheet(
        spreadsheetId: String,
        spreadsheetRange: String
    ): Single<List<Person>> {
        return sheetsAPIDataSource.readSpreadSheet(spreadsheetId, spreadsheetRange)
    }

    fun readAllSpreadSheets(): Single<Sheets.Spreadsheets> {
        return sheetsAPIDataSource.readSpreadSheets()
    }

    fun createSpreadsheet(spreadSheet: Spreadsheet): Observable<SpreadsheetInfo> {
        return sheetsAPIDataSource.createSpreadsheet(spreadSheet)
    }
}
