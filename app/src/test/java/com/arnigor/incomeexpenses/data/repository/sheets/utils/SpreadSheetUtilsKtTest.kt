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
        assert("B" == getLastColumnNameFromSize(2))
        assert("Z" == getLastColumnNameFromSize(26))
        assert("AA" == getLastColumnNameFromSize(27))
        assert("AF" == getLastColumnNameFromSize(32))
        assert("AZ" == getLastColumnNameFromSize(52))
        assert("BA" == getLastColumnNameFromSize(53))
        assert("BZ" == getLastColumnNameFromSize(78))
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