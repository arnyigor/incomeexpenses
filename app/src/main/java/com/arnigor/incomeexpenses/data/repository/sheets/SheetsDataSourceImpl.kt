package com.arnigor.incomeexpenses.data.repository.sheets

import com.arnigor.incomeexpenses.data.model.SpreadsheetInfo
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.Spreadsheet
import javax.inject.Inject

class SheetsDataSourceImpl @Inject constructor() : SheetsDataSource {
    private var sheetsAPI: Sheets? = null

    override fun initApi(googleAccountCredential: GoogleAccountCredential?) {
        sheetsAPI = Sheets.Builder(
            AndroidHttp.newCompatibleTransport(),
            JacksonFactory.getDefaultInstance(),
            googleAccountCredential
        )
            .setApplicationName("Google Sheets API Android Quickstart")
            .build()
    }

    override suspend fun readSpreadSheet(
        spreadsheetId: String,
        spreadsheetRange: String
    ): List<List<Any>> {
        return sheetsApi().spreadsheets().values()
            .get(spreadsheetId, spreadsheetRange)
//            .setMajorDimension("COLUMNS")
//            .setValueRenderOption("FORMULA")
            .execute().getValues() as List<List<Any>>
    }

    private fun sheetsApi(): Sheets {
        return requireNotNull(sheetsAPI) {
            "Ошибка инициализации Google таблиц"
        }
    }

    override suspend fun readSpreadSheetData(spreadsheetId: String): SheetProperties? {
        val execute = sheetsApi().spreadsheets().get(spreadsheetId).execute()
        return execute.sheets[0].properties
    }

    override suspend fun createSpreadsheet(spreadSheet: Spreadsheet): SpreadsheetInfo {
        val execute = sheetsApi().spreadsheets()
            .create(spreadSheet)
            .execute()
        return SpreadsheetInfo(",", ""/*it[KEY_ID] as String, it[KEY_URL] as String*/)
    }
}
