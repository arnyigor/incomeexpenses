package com.arnigor.incomeexpenses.data.repository.sheets

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.sheets.v4.model.SheetProperties
import javax.inject.Inject

class SheetsRepositoryImpl @Inject constructor(private val sheetsAPIDataSource: SheetsDataSource) :
    SheetsRepository {

    override fun initSheetsApi(credential: GoogleAccountCredential?) {
        sheetsAPIDataSource.initApi(credential)
    }

    override suspend fun readSpreadSheetData(spreadsheetId: String): SheetProperties? {
        return sheetsAPIDataSource.readSpreadSheetData(spreadsheetId)
    }

    override suspend fun readSpreadSheet(
        spreadsheetId: String,
        spreadsheetRange: String
    ): List<List<Any>> {
        return sheetsAPIDataSource.readSpreadSheet(spreadsheetId, spreadsheetRange)
    }
}
