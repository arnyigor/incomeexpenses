package com.arnigor.incomeexpenses.data.repository.sheets

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential

interface SheetsRepository {
    suspend fun readSpreadSheet(
        spreadsheetId: String,
        spreadsheetRange: String
    ): List<List<Any>>

    fun initSheetsApi(credential: GoogleAccountCredential?)
}
