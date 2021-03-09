package com.arnigor.incomeexpenses.data.repository.sheets

import com.arnigor.incomeexpenses.data.repository.sheets.utils.getColumnNameFromColumnPosition
import com.arnigor.incomeexpenses.data.repository.sheets.utils.getSpeadsheetIdFromLink
import com.arnigor.incomeexpenses.ui.models.*
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.sheets.v4.model.SheetProperties
import java.math.BigDecimal
import java.util.*
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

    override suspend fun readSpreadSheetData(spreadsheetId: String): SheetProperties? =
        sheetsAPIDataSource.readSpreadSheetData(spreadsheetId)

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
        val cellvalue = values.flatten().getOrNull(0).toString()
        return PaymentData(cellvalue, range)
    }

    private suspend fun getSheetData(
        link: String,
        month: String,
        paymentCategory: PaymentCategory?
    ): Pair<String, String> {
        val spreadsheetId = getSpeadsheetIdFromLink(link)
        val months =
            sheetsAPIDataSource.readSpreadSheet(spreadsheetId, "A1:A14", majorDimension = "COLUMNS")
        val monthIndex: Int
        if (months.isNotEmpty()) {
            monthIndex = months.flatten().map { it.toString() }
                .indexOf(month.toUpperCase(Locale.getDefault()))
        } else {
            error("No data found.")
        }
        val rangeIndex = monthIndex + 1
        val range =
            "${paymentCategory?.sheetPosition}$rangeIndex:${paymentCategory?.sheetPosition}$rangeIndex"
        return Pair(spreadsheetId, range)
    }

    override suspend fun readSpreadSheet(link: String): SpreadSheetData {
        val spreadsheetId = getSpeadsheetIdFromLink(link)
        val readSpreadSheetData = readSpreadSheetData(spreadsheetId)
        val gridProperties = readSpreadSheetData?.gridProperties
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
