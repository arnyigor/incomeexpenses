package com.arnigor.incomeexpenses.data.repository.sheets

import com.arnigor.incomeexpenses.data.model.SpreadsheetModifiedData
import com.arnigor.incomeexpenses.data.repository.sheets.utils.getColumnNameFromColumnPosition
import com.arnigor.incomeexpenses.data.repository.sheets.utils.getSpeadsheetIdFromLink
import com.arnigor.incomeexpenses.presentation.models.*
import com.arnigor.incomeexpenses.utils.DateTimeUtils
import com.arnigor.incomeexpenses.utils.normalize
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.sheets.v4.model.Spreadsheet
import java.math.BigDecimal
import javax.inject.Inject


class SheetsRepositoryImpl @Inject constructor(private val sheetsAPIDataSource: SheetsDataSource) :
    SheetsRepository {

    private companion object {
        const val SUMM_KEY = "СУММА"
        const val OUTCOME_KEY = "РАСХОД"
    }

    override fun initSheetsApi(credential: GoogleAccountCredential?) {
        sheetsAPIDataSource.initApi(credential)
    }

    override suspend fun readSpreadSheetData(link: String): Spreadsheet? {
        return sheetsAPIDataSource.readSpreadSheetData(getSpeadsheetIdFromLink(link))
    }

    override suspend fun getModifiedData(link: String): SpreadsheetModifiedData {
        return sheetsAPIDataSource.getModifiedData(getSpeadsheetIdFromLink(link))
    }

    override suspend fun writeValue(
        link: String,
        paymentCategory: PaymentCategory?,
        month: String,
        cellValue: String?
    ): Boolean {
        val (spreadsheetId, range) = getSheetData(link, month, paymentCategory)
        return sheetsAPIDataSource.writeValue(spreadsheetId, range, cellValue)
    }

    override suspend fun readCell(
        link: String,
        paymentCategory: PaymentCategory?,
        month: String
    ): PaymentData {
        val (spreadsheetId, range) = getSheetData(link, month, paymentCategory)
        val values = sheetsAPIDataSource.readSpreadSheet(
            spreadsheetId = spreadsheetId,
            spreadsheetRange = range,
            majorDimension = "COLUMNS",
            valueRenderOption = "FORMULA"
        )
        val value = values.flatten().getOrNull(0)?.let { value ->
            val cellValue = value.toString()
            if (cellValue.startsWith("=").not()) {
                "=${cellValue}"
            } else {
                cellValue
            }
        } ?: "="
        return PaymentData(value, range)
    }

    private suspend fun getSheetData(
        link: String,
        month: String,
        paymentCategory: PaymentCategory?
    ): Pair<String, String> {
        val spreadsheetId = getSpeadsheetIdFromLink(link)
        val months =
            sheetsAPIDataSource.readSpreadSheet(spreadsheetId, "A1:A14", majorDimension = "COLUMNS")
        val rangeIndex = getMonthIndex(months, month) + 1
        val range =
            "${paymentCategory?.sheetPosition}$rangeIndex:${paymentCategory?.sheetPosition}$rangeIndex"
        return Pair(spreadsheetId, range)
    }

    private fun getMonthIndex(
        months: List<List<Any>>,
        month: String
    ): Int {
        val monthIndex: Int
        if (months.isNotEmpty()) {
            monthIndex = months.flatten().map { it.toString().normalize() }
                .indexOf(month.normalize())
        } else {
            error("No data found.")
        }
        if (monthIndex == -1) {
            error("Не найден месяц")
        }
        return monthIndex
    }

    override suspend fun readSpreadSheet(link: String): SpreadSheetData {
        val spreadsheetId = getSpeadsheetIdFromLink(link)
        val readSpreadSheetData = readSpreadSheetData(spreadsheetId)
        val year = DateTimeUtils.getDateTime("yyyy")
        val sheets = readSpreadSheetData?.sheets
        val sheet = sheets?.find { it.properties.title.contains(year) }
        val gridProperties = sheet?.properties?.gridProperties
        val lastColumnNameFromColumnCount =
            getColumnNameFromColumnPosition(gridProperties?.columnCount)
        val rowCount = gridProperties?.rowCount
        val range = "A1:$lastColumnNameFromColumnCount$rowCount"
        val values = sheetsAPIDataSource.readSpreadSheet(spreadsheetId, range)
        return if (values.isNotEmpty()) {
            val categories = mutableListOf<PaymentCategory>()
            val monthsData = mutableListOf<MonthData>()
            var outComeIndex = -1
            var totalOutcomeIndex = -1
            var totalIncomeIndex = -1
            for (valuesIndexed in values.withIndex()) {
                val lineIndex = valuesIndexed.index
                val rows = valuesIndexed.value
                val payments = mutableListOf<Payment>()
                var monthName: String? = null
                var totalIncome: BigDecimal? = null
                var totalOutcome: BigDecimal? = null
                for (rowsIndexed in rows.withIndex()) {
                    val cellIndex = rowsIndexed.index
                    val cell = rowsIndexed.value
                    val cellData = cell.toString()
                    val columnName = getColumnNameFromColumnPosition(cellIndex + 1)
                    if (lineIndex == 1 && cellIndex > 0 && cellData != SUMM_KEY) {
                        if (cellData == SUMM_KEY) {
                            if (cellIndex < outComeIndex) {
                                totalIncomeIndex = cellIndex
                            } else {
                                totalOutcomeIndex = cellIndex
                            }
                        }
                        val category = createCategory(
                            cellData,
                            cellIndex,
                            outComeIndex,
                            columnName
                        )
                        categories.add(category)
                    }
                    if (cell == OUTCOME_KEY) {
                        outComeIndex = cellIndex
                    }
                    val monthCell = lineIndex >= 2 && cellIndex == 0
                    if (monthCell) {
                        monthName = cellData
                    }
                    when {
                        cellIndex == totalIncomeIndex -> {
                            totalIncome = cellData.replace(",", ".").toBigDecimalOrNull()
                        }
                        cellIndex == totalOutcomeIndex -> {
                            totalOutcome = cellData.replace(",", ".").toBigDecimalOrNull()
                        }
                        cellIndex > 0 && lineIndex >= 2 -> {
                            if (cellData.isNotBlank()) {
                                categories.find { it.sheetPosition == columnName }
                                    ?.let {
                                        val value = cellData
                                            .replace(",", ".")
                                            .toBigDecimalOrNull() ?: BigDecimal.ZERO
                                        val payment = Payment(
                                            paymentCategory = it,
                                            value = value
                                        )
                                        payments.add(payment)
                                    }
                            }
                        }
                    }
                }
                if (payments.isNotEmpty()) {
                    monthsData.add(
                        MonthData(
                            monthName,
                            payments,
                            totalIncome,
                            totalOutcome
                        )
                    )
                }
            }
            SpreadSheetData(categories, monthsData)
        } else {
            error("No data found.")
        }
    }

    private fun createCategory(
        cellData: String,
        cellIndex: Int,
        outComeIndex: Int,
        columnName: String
    ) = PaymentCategory(
        cellData,
        if (cellIndex >= outComeIndex) {
            PaymentType.OUTCOME
        } else {
            PaymentType.INCOME
        },
        columnName
    )
}
