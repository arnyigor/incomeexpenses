package com.arnigor.incomeexpenses.data.repository.sheets

import com.arnigor.incomeexpenses.data.model.SpreadsheetInfo
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.UpdateValuesResponse
import com.google.api.services.sheets.v4.model.ValueRange
import javax.inject.Inject

class SheetsDataSourceImpl @Inject constructor() : SheetsDataSource {
    private var sheetsAPI: Sheets? = null
    private var driveApi: Drive? = null

    override fun initApi(googleAccountCredential: GoogleAccountCredential?) {
        sheetsAPI = Sheets.Builder(
            AndroidHttp.newCompatibleTransport(),
            JacksonFactory.getDefaultInstance(),
            googleAccountCredential
        )
            .setApplicationName("Google Sheets API Android Quickstart")
            .build()
        driveApi = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            JacksonFactory.getDefaultInstance(),
            googleAccountCredential
        )
            .setApplicationName("My Application Name")
            .build()
    }

    override suspend fun readSpreadSheet(
        spreadsheetId: String,
        spreadsheetRange: String,
        majorDimension: String?,
        valueRenderOption: String?
    ): List<List<Any>> {
        return sheetsApi().spreadsheets().values()
            .get(spreadsheetId, spreadsheetRange).apply {
                if (majorDimension.isNullOrBlank().not()) {
                    setMajorDimension(majorDimension)
                }
                if (valueRenderOption.isNullOrBlank().not()) {
                    setValueRenderOption(valueRenderOption)
                }
            }.execute().getValues() ?: return listOf(listOf())
    }

    private fun sheetsApi(): Sheets {
        return requireNotNull(sheetsAPI) {
            "Ошибка инициализации Google таблиц"
        }
    }

    override suspend fun readSpreadSheetData(spreadsheetId: String): Spreadsheet? {
        val modifiedTime = driveApi?.files()
            ?.get(spreadsheetId)
            ?.setFields("id, modifiedTime")
            ?.execute()?.modifiedTime
        return sheetsApi().spreadsheets()[spreadsheetId].execute()
    }

    override suspend fun writeValue(
        spreadsheetId: String,
        range: String,
        cellValue: String?
    ): Boolean {
        val values: List<List<Any>> = listOf(listOf(cellValue ?: ""))
        val body: ValueRange = ValueRange()
            .setValues(values)
        val result: UpdateValuesResponse =
            sheetsApi().spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute()
        return result.updatedCells != 0
    }

    override suspend fun createSpreadsheet(spreadSheet: Spreadsheet): SpreadsheetInfo {
        val execute = sheetsApi().spreadsheets()
            .create(spreadSheet)
            .execute()
        return SpreadsheetInfo(",", ""/*it[KEY_ID] as String, it[KEY_URL] as String*/)
    }
}
