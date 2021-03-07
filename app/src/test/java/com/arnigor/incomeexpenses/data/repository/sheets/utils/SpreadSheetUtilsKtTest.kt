package com.arnigor.incomeexpenses.data.repository.sheets.utils

import org.junit.jupiter.api.Test

class SpreadSheetUtilsKtTest {

    @Test
    fun getSpeadsheetIdFromLink() {
        val id = "14IWxF8lv_6ZaX5IUMaBwASSyX-hOV-OuCGs6Dj5MmXE"
        val link = "https://docs.google.com/spreadsheets/d/$id/edit?usp=drivesdk"
        val speadsheetId = getSpeadsheetIdFromLink(link)
        assert(id == speadsheetId)
    }

    @Test
    fun getLastColumnNameFromSize() {
        assert("B" == getLastColumnNameFromColumnPosition(2))
        assert("Z" == getLastColumnNameFromColumnPosition(26))
        assert("AA" == getLastColumnNameFromColumnPosition(27))
        assert("AF" == getLastColumnNameFromColumnPosition(32))
        assert("AZ" == getLastColumnNameFromColumnPosition(52))
        assert("BA" == getLastColumnNameFromColumnPosition(53))
        assert("BZ" == getLastColumnNameFromColumnPosition(78))
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