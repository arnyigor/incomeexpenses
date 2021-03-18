package com.arnigor.incomeexpenses.data.model

data class SpreadsheetModifiedData(
    val createdTime: String,
    val modifiedTime: String,
    val modifiedByMe: Boolean,
    val duration: Long?
)
