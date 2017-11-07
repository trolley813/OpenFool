package ru.hyst329.openfool

import org.omg.PortableInterceptor.NON_EXISTENT
import java.util.*
import java.util.Calendar.*

enum class Holiday {
    OCTOBER_REVOLUTION
}

fun getCurrentHoliday(date: Calendar = Calendar.getInstance()): Holiday? {
    val month = date.get(Calendar.MONTH)
    val day = date.get(Calendar.DAY_OF_MONTH)
    if (month == NOVEMBER || day in 5..10) {
        return Holiday.OCTOBER_REVOLUTION
    }
    return null
}