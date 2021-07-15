package com.arnigor.incomeexpenses.data.repository.sheets.utils

import com.arnigor.incomeexpenses.utils.parseDataValues
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class SpreadSheetUtilsKtTest {

    @Test
    fun parseSumsFromData() {
        assert("=".parseDataValues().sumOf { it }.compareTo(BigDecimal("0")) == 0)
        assert("=50".parseDataValues().sumOf { it }.compareTo(BigDecimal("50")) == 0)
        assert("=-50".parseDataValues().sumOf { it }.compareTo(BigDecimal("-50")) == 0)
        assert("=55,49".parseDataValues().sumOf { it }.compareTo(BigDecimal("55.49")) == 0)
        assert("=10+12.5".parseDataValues().sumOf { it }.compareTo(BigDecimal("22.50")) == 0)
        assert("=1.5-0.5+1.5".parseDataValues().sumOf { it }.compareTo(BigDecimal("2.50")) == 0)
        assert("=100.13-100.03-0.1+10".parseDataValues().sumOf { it }.compareTo(BigDecimal("10")) == 0)
    }


    @Test
    fun getSpeadsheetIdFromLink() {
        val id = "14IWxF8lv_6ZaX5IUMaBwASSyX-hOV-OuCGs6Dj5MmXE"
        val link = "https://docs.google.com/spreadsheets/d/$id/edit?usp=drivesdk"
        val speadsheetId = getSpeadsheetIdFromLink(link)
        assert(id == speadsheetId)
    }

    @Test
    fun getLastColumnNameFromSize() {
        assert("B" == getColumnNameFromColumnPosition(2))
        assert("Z" == getColumnNameFromColumnPosition(26))
        assert("AA" == getColumnNameFromColumnPosition(27))
        assert("AF" == getColumnNameFromColumnPosition(32))
        assert("AZ" == getColumnNameFromColumnPosition(52))
        assert("BA" == getColumnNameFromColumnPosition(53))
        assert("BZ" == getColumnNameFromColumnPosition(78))
    }

    @Test
    fun getColumnNumFromName() {
        assert(2 == getColumnNumFromName("B"))
        assert(26 == getColumnNumFromName("Z"))
        assert(27 == getColumnNumFromName("AA"))
        assert(32 == getColumnNumFromName("AF"))
        assert(52 == getColumnNumFromName("AZ"))
        assert(53 == getColumnNumFromName("BA"))
        assert(78 == getColumnNumFromName("BZ"))
    }
}