package com.arnigor.incomeexpenses.data.repository.sheets

import com.arnigor.incomeexpenses.data.model.SpreadsheetModifiedData
import com.arnigor.incomeexpenses.utils.DateTimeUtils
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.UpdateValuesResponse
import com.google.api.services.sheets.v4.model.ValueRange
import javax.inject.Inject

class SheetsDataSourceImpl @Inject constructor() : SheetsDataSource {
    private var googleAccountCredential: GoogleAccountCredential? = null
    private val sheetsAPI by lazy {
        Sheets.Builder(
            AndroidHttp.newCompatibleTransport(),
            JacksonFactory.getDefaultInstance(),
            googleAccountCredential
        )
            .setApplicationName("Google Sheets API Android Quickstart")
            .build()
    }
    private val driveApi by lazy {
        Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            JacksonFactory.getDefaultInstance(),
            googleAccountCredential
        )
            .setApplicationName("My Application Name")
            .build()
    }

    override fun initApi(googleAccountCredential: GoogleAccountCredential?) {
        this.googleAccountCredential = googleAccountCredential
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

    override suspend fun getModifiedData(spreadsheetId: String): SpreadsheetModifiedData {
        return driveApi.files()
            .get(spreadsheetId)
            .setFields("id, modifiedTime, createdTime, modifiedByMe")
            .execute()
            .let { file ->
                val (modifiedTime, duration) = getModifiedTimes(file)
                SpreadsheetModifiedData(
                    createdTime = formatTime(file?.createdTime, "dd MM yyyy HH:mm"),
                    modifiedTime = modifiedTime,
                    modifiedByMe = file?.modifiedByMe == true,
                    duration = duration
                )
            }
    }

    private fun getModifiedTimes(file: File): Pair<String, Long?> {
        var modifiedTime = formatTime(file.modifiedTime, "HH:mm:ss")
        var duration = file.modifiedTime?.value?.let {
            DateTimeUtils.durationInMinutes(it, System.currentTimeMillis())
        }
        duration?.let { dur ->
            if (dur >= 60) {
                duration = null
                modifiedTime = formatTime(file.modifiedTime, "dd MM yyyy HH:mm")
            } else {
                modifiedTime = formatTime(file.modifiedTime, "HH:mm:ss")
            }
        } ?: kotlin.run {
            modifiedTime = formatTime(file.modifiedTime, "dd MM yyyy HH:mm")
        }
        return Pair(modifiedTime, duration)
    }

    override suspend fun readSpreadSheetData(spreadsheetId: String): Spreadsheet? {
        return sheetsApi().spreadsheets()[spreadsheetId].execute()
    }

    override suspend fun writeValue(
        spreadsheetId: String,
        range: String,
        cellValue: String?
    ): Boolean {
        val value = cellValue.takeIf { it.isNullOrBlank().not() && it != "=" } ?: ""
        val values: List<List<Any>> = listOf(listOf(value))
        val body: ValueRange = ValueRange()
            .setValues(values)
        val result: UpdateValuesResponse =
            sheetsApi().spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute()
        return result.updatedCells != 0
    }

    private fun formatTime(modifiedTime: DateTime?, format: String): String {
        modifiedTime?.let {
            return DateTimeUtils.getDateTime(modifiedTime.value, format)
        } ?: return ""
    }
}
