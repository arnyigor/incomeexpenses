package com.arnigor.incomeexpenses.data.repository.sheets

import com.arnigor.incomeexpenses.data.manager.AuthenticationManager
import com.arnigor.incomeexpenses.data.model.Person
import com.arnigor.incomeexpenses.data.model.SpreadsheetInfo
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.Spreadsheet
import io.reactivex.Observable
import io.reactivex.Single

class SheetsAPIDataSource(
    private val authManager: AuthenticationManager,
    private val transport: HttpTransport,
    private val jsonFactory: JsonFactory
) : SheetsDataSource {
    companion object {
        val KEY_ID = "spreadsheetId"
        val KEY_URL = "spreadsheetUrl"
    }

    private val sheetsAPI: Sheets
        get() {
            return Sheets.Builder(
                transport,
                jsonFactory,
                authManager.googleAccountCredential
            )
                .setApplicationName("test")
                .build()
        }

    override fun readSpreadSheet(
        spreadsheetId: String,
        spreadsheetRange: String
    ): Single<List<Person>> {
        return Observable.fromCallable {
            val response = sheetsAPI.spreadsheets().values()
                .get(spreadsheetId, spreadsheetRange)
                .execute()
            response.getValues()
        }
            .flatMapIterable { it }
            .map { Person(it[0].toString(), it[4].toString()) }
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
        }.map { SpreadsheetInfo(it[KEY_ID] as String, it[KEY_URL] as String) }
    }
}
