package ru.hyst329.openfool

import java.util.*
import java.util.Calendar.*

enum class Holiday {
    OCTOBER_REVOLUTION,
    NEW_YEAR
}

fun getCurrentHoliday(date: Calendar = Calendar.getInstance()): Holiday? {
    val month = date.get(Calendar.MONTH)
    val day = date.get(Calendar.DAY_OF_MONTH)
    if (month == NOVEMBER && day in 5..10) {
        return Holiday.OCTOBER_REVOLUTION
    }
    if (month == DECEMBER && day in 24..31 || month == JANUARY && day in 1..9) {
        return Holiday.NEW_YEAR
    }
    return null
}