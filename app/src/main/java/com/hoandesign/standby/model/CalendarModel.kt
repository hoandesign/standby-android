package com.hoandesign.standby.model

import androidx.compose.ui.graphics.Color
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.util.Locale

/**
 * Checks whether the given [year] is a leap year in the Gregorian calendar.
 */
fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

/**
 * Calculates the number of days in a given [month] (1..12) for the specified [year].
 */
fun getDaysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (isLeapYear(year)) 29 else 28
    else -> throw IllegalArgumentException("Invalid month: $month. Month must be between 1 and 12.")
}

/**
 * Calculates the day-of-week offset for the 1st day of the given month,
 * assuming Sunday is the first day of the week (0 = Sunday, 1 = Monday, ..., 6 = Saturday).
 */
fun getFirstDayOfWeekOffset(year: Int, month: Int): Int {
    val firstDay = LocalDate.of(year, month, 1)
    return firstDay.dayOfWeek.value % 7
}

/**
 * Generates a calendar month grid representation.
 *
 * @param year Calendar year (e.g. 2026).
 * @param month Calendar month (1..12).
 * @param firstDayOfWeekOffset Number of blank/empty leading days before the 1st of the month (0..6).
 * @param daysInMonth Number of days in the month (e.g. 28..31).
 * @return List of day integers (1..daysInMonth) prefixed with [firstDayOfWeekOffset] nulls.
 */
fun generateMonthGrid(
    year: Int,
    month: Int,
    firstDayOfWeekOffset: Int = getFirstDayOfWeekOffset(year, month),
    daysInMonth: Int = getDaysInMonth(year, month)
): List<Int?> {
    val normalizedOffset = ((firstDayOfWeekOffset % 7) + 7) % 7
    val result = ArrayList<Int?>(normalizedOffset + daysInMonth)
    repeat(normalizedOffset) {
        result.add(null)
    }
    for (day in 1..daysInMonth) {
        result.add(day)
    }
    return result
}

/**
 * Formats a user-friendly countdown string from the current time to the event start time.
 * Examples: "Started", "In 20m", "In 1h 15m", "In 1h", "In 2d 3h".
 *
 * @param startEpochMillis Epoch timestamp in milliseconds for event start.
 * @param currentEpochMillis Current epoch timestamp in milliseconds.
 */
fun formatEventCountdown(startEpochMillis: Long, currentEpochMillis: Long): String {
    val diffMillis = startEpochMillis - currentEpochMillis
    if (diffMillis <= 0L) {
        return "Started"
    }

    val totalMinutes = diffMillis / 60_000L
    if (totalMinutes < 1L) {
        return "In 1m"
    }

    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    val days = hours / 24L
    val remHours = hours % 24L

    return when {
        hours == 0L -> "In ${minutes}m"
        days >= 1L -> {
            if (remHours > 0L) "In ${days}d ${remHours}h" else "In ${days}d"
        }
        minutes == 0L -> "In ${hours}h"
        else -> "In ${hours}h ${minutes}m"
    }
}

/**
 * Represents a calendar month view model for month grid display.
 *
 * @param year Year number (e.g. 2026).
 * @param month Month index (1 = January, ..., 12 = December).
 * @param monthName Human-readable uppercase month name (e.g. "APRIL", "SEPTEMBER").
 * @param daysInMonth Total number of days in the month (28..31).
 * @param firstDayOfWeekOffset Day-of-week index for day 1 (0 = Sunday .. 6 = Saturday).
 * @param todayDayOfMonth Today's day of month (1..31), or -1 if today is not in this month.
 */
data class CalendarMonth(
    val year: Int,
    val month: Int,
    val monthName: String,
    val daysInMonth: Int,
    val firstDayOfWeekOffset: Int,
    val todayDayOfMonth: Int
) {
    val grid: List<Int?>
        get() = generateMonthGrid(year, month, firstDayOfWeekOffset, daysInMonth)

    companion object {
        /**
         * Creates a [CalendarMonth] instance representing the current local date.
         */
        fun now(localDate: LocalDate = LocalDate.now()): CalendarMonth {
            val year = localDate.year
            val month = localDate.monthValue
            val monthName = localDate.month.name.uppercase(Locale.US)
            val daysInMonth = getDaysInMonth(year, month)
            val firstDayOffset = getFirstDayOfWeekOffset(year, month)
            val todayDay = localDate.dayOfMonth

            return CalendarMonth(
                year = year,
                month = month,
                monthName = monthName,
                daysInMonth = daysInMonth,
                firstDayOfWeekOffset = firstDayOffset,
                todayDayOfMonth = todayDay
            )
        }

        /**
         * Creates a [CalendarMonth] instance for an arbitrary month and year.
         */
        fun of(year: Int, month: Int, todayDayOfMonth: Int = -1): CalendarMonth {
            val daysInMonth = getDaysInMonth(year, month)
            val firstDayOffset = getFirstDayOfWeekOffset(year, month)
            val monthName = Month.of(month).name.uppercase(Locale.US)

            return CalendarMonth(
                year = year,
                month = month,
                monthName = monthName,
                daysInMonth = daysInMonth,
                firstDayOfWeekOffset = firstDayOffset,
                todayDayOfMonth = todayDayOfMonth
            )
        }
    }
}

/**
 * Represents an individual calendar meeting or event.
 *
 * @param id Unique identifier.
 * @param title Title or description of the event.
 * @param startTimeFormatted Human-readable formatted start time (e.g. "10:00 AM").
 * @param endTimeFormatted Human-readable formatted end time (e.g. "11:00 AM").
 * @param location Optional location or video conference link (e.g. "Design Studio A").
 * @param color Theme category color for the event bar / badge.
 * @param startEpochMillis Start timestamp in milliseconds.
 * @param endEpochMillis End timestamp in milliseconds.
 * @param isAllDay True if the event spans the entire day.
 */
data class CalendarEvent(
    val id: Long,
    val title: String,
    val startTimeFormatted: String,
    val endTimeFormatted: String,
    val location: String? = null,
    val color: Color = Color(0xFFFF453A),
    val startEpochMillis: Long = 0L,
    val endEpochMillis: Long = 0L,
    val isAllDay: Boolean = false
)
