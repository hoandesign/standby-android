package com.hoandesign.standby

import androidx.compose.ui.graphics.Color
import com.hoandesign.standby.model.CalendarEvent
import com.hoandesign.standby.model.CalendarMonth
import com.hoandesign.standby.model.formatEventCountdown
import com.hoandesign.standby.model.formatEventDate
import com.hoandesign.standby.model.generateMonthGrid
import com.hoandesign.standby.model.getDaysInMonth
import com.hoandesign.standby.model.getFirstDayOfWeekOffset
import com.hoandesign.standby.model.isLeapYear
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarTest {

    @Test
    fun testMonthGridGenerator_offsetNullsAndDaySequences() {
        // Month starting on Sunday (offset 0), 31 days
        val gridSunday = generateMonthGrid(
            year = 2026,
            month = 3,
            firstDayOfWeekOffset = 0,
            daysInMonth = 31
        )
        assertEquals(31, gridSunday.size)
        assertEquals(1, gridSunday[0])
        assertEquals(31, gridSunday[30])
        assertEquals(0, gridSunday.count { it == null })

        // Month starting on Wednesday (offset 3), 30 days
        val gridWed = generateMonthGrid(
            year = 2026,
            month = 9,
            firstDayOfWeekOffset = 3,
            daysInMonth = 30
        )
        assertEquals(33, gridWed.size)
        assertNull(gridWed[0])
        assertNull(gridWed[1])
        assertNull(gridWed[2])
        assertEquals(1, gridWed[3])
        assertEquals(2, gridWed[4])
        assertEquals(30, gridWed[32])
        assertEquals(3, gridWed.count { it == null })

        // Month starting on Saturday (offset 6), 28 days
        val gridSat = generateMonthGrid(
            year = 2025,
            month = 2,
            firstDayOfWeekOffset = 6,
            daysInMonth = 28
        )
        assertEquals(34, gridSat.size)
        for (i in 0 until 6) {
            assertNull(gridSat[i])
        }
        assertEquals(1, gridSat[6])
        assertEquals(28, gridSat[33])
        assertEquals(6, gridSat.count { it == null })

        // September 2026 default lookup: Sep 1, 2026 is Tuesday (offset 2), 30 days
        assertEquals(2, getFirstDayOfWeekOffset(2026, 9))
        assertEquals(30, getDaysInMonth(2026, 9))
        val gridSep2026 = generateMonthGrid(2026, 9)
        assertEquals(32, gridSep2026.size)
        assertNull(gridSep2026[0])
        assertNull(gridSep2026[1])
        assertEquals(1, gridSep2026[2])
        assertEquals(30, gridSep2026[31])
    }

    @Test
    fun testCalendarMonth_dataClassAndGridIntegration() {
        val april2026 = CalendarMonth.of(year = 2026, month = 4, todayDayOfMonth = 15)
        // April 1, 2026 is Wednesday (offset 3), 30 days
        assertEquals(2026, april2026.year)
        assertEquals(4, april2026.month)
        assertEquals("APRIL", april2026.monthName)
        assertEquals(30, april2026.daysInMonth)
        assertEquals(3, april2026.firstDayOfWeekOffset)
        assertEquals(15, april2026.todayDayOfMonth)
        assertEquals(33, april2026.grid.size)
        assertNull(april2026.grid[0])
        assertEquals(1, april2026.grid[3])
        assertEquals(30, april2026.grid[32])
    }

    @Test
    fun testDaysInMonth_31DayMonths() {
        val thirtyOneDayMonths = listOf(1, 3, 5, 7, 8, 10, 12)
        for (m in thirtyOneDayMonths) {
            assertEquals("Month $m should have 31 days in 2026", 31, getDaysInMonth(2026, m))
        }
    }

    @Test
    fun testDaysInMonth_30DayMonths() {
        val thirtyDayMonths = listOf(4, 6, 9, 11)
        for (m in thirtyDayMonths) {
            assertEquals("Month $m should have 30 days in 2026", 30, getDaysInMonth(2026, m))
        }
    }

    @Test
    fun testDaysInMonth_februaryLeapAndNonLeap() {
        // Non-leap common years -> 28 days
        assertEquals(28, getDaysInMonth(2023, 2))
        assertEquals(28, getDaysInMonth(2025, 2))
        assertEquals(28, getDaysInMonth(2026, 2))
        assertEquals(28, getDaysInMonth(2027, 2))
        // Century years not divisible by 400 -> 28 days
        assertEquals(28, getDaysInMonth(1900, 2))
        assertEquals(28, getDaysInMonth(2100, 2))

        // Leap years -> 29 days
        assertEquals(29, getDaysInMonth(2020, 2))
        assertEquals(29, getDaysInMonth(2024, 2))
        assertEquals(29, getDaysInMonth(2028, 2))
        // Century years divisible by 400 -> 29 days
        assertEquals(29, getDaysInMonth(2000, 2))
        assertEquals(29, getDaysInMonth(2400, 2))
    }

    @Test
    fun testIsLeapYear() {
        assertTrue(isLeapYear(2020))
        assertTrue(isLeapYear(2024))
        assertTrue(isLeapYear(2028))
        assertTrue(isLeapYear(2000))
        assertTrue(isLeapYear(2400))

        assertFalse(isLeapYear(2023))
        assertFalse(isLeapYear(2025))
        assertFalse(isLeapYear(2026))
        assertFalse(isLeapYear(1900))
        assertFalse(isLeapYear(2100))
    }

    @Test
    fun testEventCountdownFormatting_scenarios() {
        val current = 1_000_000L

        // Event already started or current
        assertEquals("Started", formatEventCountdown(current, current))
        assertEquals("Started", formatEventCountdown(current - 5000L, current))

        // Event starting within 1 minute
        assertEquals("In 1m", formatEventCountdown(current + 30_000L, current))
        assertEquals("In 1m", formatEventCountdown(current + 59_000L, current))

        // Event in minutes
        assertEquals("In 20m", formatEventCountdown(current + 20 * 60_000L, current))
        assertEquals("In 5m", formatEventCountdown(current + 5 * 60_000L, current))
        assertEquals("In 45m", formatEventCountdown(current + 45 * 60_000L, current))

        // Event in exact hours
        assertEquals("In 1h", formatEventCountdown(current + 60 * 60_000L, current))
        assertEquals("In 3h", formatEventCountdown(current + 180 * 60_000L, current))

        // Event in hours and minutes
        assertEquals("In 1h 15m", formatEventCountdown(current + 75 * 60_000L, current))
        assertEquals("In 2h 30m", formatEventCountdown(current + 150 * 60_000L, current))
        assertEquals("In 5h 45m", formatEventCountdown(current + (5 * 60 + 45) * 60_000L, current))

        // Multi-day events
        assertEquals("In 1d", formatEventCountdown(current + 24 * 3600_000L, current))
        assertEquals("In 1d 4h", formatEventCountdown(current + 28 * 3600_000L, current))
        assertEquals("In 3d", formatEventCountdown(current + 72 * 3600_000L, current))
        assertEquals("In 2d 5h", formatEventCountdown(current + 53 * 3600_000L, current))
    }

    @Test
    fun testCalendarEvent_propertiesAndDefaults() {
        val event = CalendarEvent(
            id = 42L,
            title = "Product Design Review",
            startTimeFormatted = "10:00 AM",
            endTimeFormatted = "10:45 AM"
        )

        assertEquals(42L, event.id)
        assertEquals("Product Design Review", event.title)
        assertEquals("10:00 AM", event.startTimeFormatted)
        assertEquals("10:45 AM", event.endTimeFormatted)
        assertNull(event.location)
        assertEquals(Color(0xFFFF453A), event.color)
        assertEquals(0L, event.startEpochMillis)
        assertEquals(0L, event.endEpochMillis)
        assertFalse(event.isAllDay)
        assertEquals("Today", event.displayDate)
        assertEquals("TODAY", event.displayTag)
    }

    @Test
    fun testFormatEventDate_todayTomorrowAndFuture() {
        val zone = java.time.ZoneId.of("UTC")
        val today = java.time.LocalDate.now(zone)
        val now = today.atStartOfDay(zone).toInstant().toEpochMilli()

        // Today
        val (todayFormatted, todayTag) = formatEventDate(now + 3600_000L, zone)
        assertTrue(todayFormatted.contains("Today"))
        assertEquals("TODAY", todayTag)

        // Tomorrow
        val tomorrowMillis = now + 86400_000L + 3600_000L
        val (tomorrowFormatted, tomorrowTag) = formatEventDate(tomorrowMillis, zone)
        assertTrue(tomorrowFormatted.contains("Tomorrow"))
        assertEquals("TOMORROW", tomorrowTag)

        // Future day (e.g. 3 days out)
        val futureMillis = now + 3 * 86400_000L + 3600_000L
        val (futureFormatted, futureTag) = formatEventDate(futureMillis, zone)
        val futureDate = today.plusDays(3)
        val shortFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM d", java.util.Locale.US)
        assertTrue(futureFormatted.contains(futureDate.format(shortFormatter)))
        assertEquals(futureDate.format(shortFormatter).uppercase(java.util.Locale.US), futureTag)
    }
}
