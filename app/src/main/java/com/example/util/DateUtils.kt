package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatFriendlyDate(epochMillis: Long?): String {
        if (epochMillis == null) return "No date"
        val target = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val today = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

        return when {
            isSameDay(target, today) -> "Today"
            isSameDay(target, tomorrow) -> "Tomorrow"
            else -> SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(epochMillis))
        }
    }

    fun formatFriendlyTime(epochMillis: Long?, timeString: String?): String {
        if (!timeString.isNullOrBlank()) return timeString
        if (epochMillis == null) return ""
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(epochMillis))
    }

    fun isToday(epochMillis: Long?): Boolean {
        if (epochMillis == null) return false
        val target = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val today = Calendar.getInstance()
        return isSameDay(target, today)
    }

    fun isOverdue(epochMillis: Long?, isCompleted: Boolean): Boolean {
        if (epochMillis == null || isCompleted) return false
        val now = System.currentTimeMillis()
        val endOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis
        return epochMillis < now && !isToday(epochMillis)
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
