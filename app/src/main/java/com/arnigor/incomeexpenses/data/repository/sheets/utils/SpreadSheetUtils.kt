package com.arnigor.incomeexpenses.data.repository.sheets.utils

fun getSpeadsheetIdFromLink(url: String): String {
    return url.substringAfter("docs.google.com/spreadsheets/d/").substringBefore("/edit")
}

fun getColumnNumFromName(name: String): Int {
    val chars = mutableListOf<Char>()
    val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val length = letters.length
    name.forEach {
        chars.add(it)
    }
    var result = 0
    val size = chars.size
    chars.forEachIndexed { indexVal, char ->
        val index = letters.indexOf(char)
        if (size == 1) {
            val i = index + 1
            return i
        } else {
            val i = indexVal * length + index + 1
            result += i // TODO: 07.03.2021 "BA" не работает
        }
    }
    val i = result - 1
    return i
}

fun getLastColumnNameFromSize(size: Int): String {
    val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val length = letters.length
    val first = size / length
    val next = size % length
    return when {
        first > 0 -> {
            if (next > 0) {
                letters[first - 1].toString() + letters[next - 1].toString()
            } else {
                if (first > 1) {
                    val firstNext = first - 1
                    letters[firstNext - 1].toString() + letters[length - 1].toString()
                } else {
                    val firstNext = length / first
                    letters[firstNext - 1].toString()
                }
            }
        }
        else -> {
            letters[size - 1].toString()
        }
    }
}