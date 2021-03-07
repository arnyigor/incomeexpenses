package com.arnigor.incomeexpenses.data.repository.sheets

import com.arnigor.incomeexpenses.data.model.SpreadsheetInfo
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.Spreadsheet
import io.reactivex.Observable
import io.reactivex.Single

class SheetsAPIDataSource(
    private val sheetsAPI: Sheets,
) : SheetsDataSource {

    override fun readSpreadSheet(
        spreadsheetId: String,
        spreadsheetRange: String
    ): Single<List<MutableList<String>>> {
        return Observable.fromCallable {
            val response = sheetsAPI.spreadsheets().values()
                .get(spreadsheetId, spreadsheetRange)
                .execute()
            response.getValues() as List<List<Any>>
        }
            .map { values ->
                if (values.isNotEmpty()) {
                    val list = mutableListOf<String>()
                    for (row in values) {
                        list.add(String.format("%s, %s\n", row[0], row[4]))
                    }
                    list
                } else {
                    error("No data found.")
                }
            }
            .toList()
    }

    override fun readSpreadSheets(): Single<Sheets.Spreadsheets> {
        return Single.fromCallable { sheetsAPI.spreadsheets() }

    }

    override fun createSpreadsheet(spreadSheet: Spreadsheet): Observable<SpreadsheetInfo> {
        return Observable.fromCallable {
            sheetsAPI.spreadsheets()
                .create(spreadSheet)
                .execute()
        }.map { SpreadsheetInfo(",", ""/*it[KEY_ID] as String, it[KEY_URL] as String*/) }
    }
}
