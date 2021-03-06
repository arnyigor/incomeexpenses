package com.arnigor.incomeexpenses.data.repository.sheets

import com.arnigor.incomeexpenses.data.model.Person
import com.arnigor.incomeexpenses.data.model.SpreadsheetInfo
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.Spreadsheet
import io.reactivex.Observable
import io.reactivex.Single

interface SheetsDataSource {
    fun readSpreadSheet(spreadsheetId: String, spreadsheetRange: String): Single<List<Person>>
    fun readSpreadSheets(): Single<Sheets.Spreadsheets>
    fun createSpreadsheet(spreadSheet: Spreadsheet): Observable<SpreadsheetInfo>
}
