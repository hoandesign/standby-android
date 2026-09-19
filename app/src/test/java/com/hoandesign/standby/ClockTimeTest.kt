package com.hoandesign.standby

import com.hoandesign.standby.model.ClockTime
import com.hoandesign.standby.model.hourAngle
import com.hoandesign.standby.model.isDaytime
import com.hoandesign.standby.model.minuteAngle
import com.hoandesign.standby.model.secondAngle
import com.hoandesign.standby.model.solarProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockTimeTest {

    private val epsilon = 0.0001f

    @Test
    fun testHourAngle_canonicalPositions() {
        // 12:00 -> 0 degrees
        assertEquals(0.0f, hourAngle(12, 0), epsilon)
        assertEquals(0.0f, hourAngle(0, 0), epsilon)

        // 3:00 -> 90 degrees
        assertEquals(90.0f, hourAngle(3, 0), epsilon)
        assertEquals(90.0f, hourAngle(15, 0), epsilon)

        // 6:30 -> (6 + 30/60) * 30 = 6.5 * 30 = 195 degrees
        assertEquals(195.0f, hourAngle(6, 30), epsilon)
        assertEquals(195.0f, hourAngle(18, 30), epsilon)

        // 9:15 -> (9 + 15/60) * 30 = 9.25 * 30 = 277.5 degrees
        assertEquals(277.5f, hourAngle(9, 15), epsilon)
        assertEquals(277.5f, hourAngle(21, 15), epsilon)
    }

    @Test
    fun testMinuteAngle_canonicalPositions() {
        // 0 minutes -> 0 degrees
        assertEquals(0.0f, minuteAngle(0, 0), epsilon)

        // 15 minutes -> 15 * 6 = 90 degrees
        assertEquals(90.0f, minuteAngle(15, 0), epsilon)

        // 30 minutes -> 30 * 6 = 180 degrees
        assertEquals(180.0f, minuteAngle(30, 0), epsilon)

        // 45 minutes -> 45 * 6 = 270 degrees
        assertEquals(270.0f, minuteAngle(45, 0), epsilon)

        // Fractional with seconds: 30m 30s -> (30 + 0.5) * 6 = 183 degrees
        assertEquals(183.0f, minuteAngle(30, 30), epsilon)
    }

    @Test
    fun testSecondAngle_steppedVsSmooth() {
        // Stepped: millis should be ignored, purely seconds * 6f
        assertEquals(0.0f, secondAngle(seconds = 0, millis = 500, smooth = false), epsilon)
        assertEquals(90.0f, secondAngle(seconds = 15, millis = 999, smooth = false), epsilon)
        assertEquals(180.0f, secondAngle(seconds = 30, millis = 500, smooth = false), epsilon)
        assertEquals(270.0f, secondAngle(seconds = 45, millis = 250, smooth = false), epsilon)

        // Smooth: includes millis / 1000f
        assertEquals(0.0f, secondAngle(seconds = 0, millis = 0, smooth = true), epsilon)
        assertEquals(3.0f, secondAngle(seconds = 0, millis = 500, smooth = true), epsilon)
        assertEquals(91.5f, secondAngle(seconds = 15, millis = 250, smooth = true), epsilon)
        assertEquals(183.0f, secondAngle(seconds = 30, millis = 500, smooth = true), epsilon)
    }

    @Test
    fun testSolarProgress_canonicalMilestones() {
        val sunrise = 360  // 6:00 AM
        val sunset = 1080  // 6:00 PM
        val solarNoon = (sunrise + sunset) / 2 // 720 (12:00 PM)

        // At sunrise -> 0.0f
        assertEquals(0.0f, solarProgress(timeMinutesOfDay = sunrise, sunriseMinutes = sunrise, sunsetMinutes = sunset), epsilon)

        // At solar noon -> 0.5f
        assertEquals(0.5f, solarProgress(timeMinutesOfDay = solarNoon, sunriseMinutes = sunrise, sunsetMinutes = sunset), epsilon)

        // At sunset -> 1.0f
        assertEquals(1.0f, solarProgress(timeMinutesOfDay = sunset, sunriseMinutes = sunrise, sunsetMinutes = sunset), epsilon)

        // Night before sunrise (e.g. 3:00 AM = 180 min) -> 0.0f
        assertEquals(0.0f, solarProgress(timeMinutesOfDay = 180, sunriseMinutes = sunrise, sunsetMinutes = sunset), epsilon)

        // Night after sunset (e.g. 9:00 PM = 1260 min) -> 1.0f
        assertEquals(1.0f, solarProgress(timeMinutesOfDay = 1260, sunriseMinutes = sunrise, sunsetMinutes = sunset), epsilon)
    }

    @Test
    fun testClockTime_dataClassPropertiesAndFormatting() {
        val time = ClockTime(hours = 9, minutes = 41, seconds = 15, millis = 500, is24Hour = false)

        assertEquals(9 * 60 + 41, time.timeMinutesOfDay)
        assertEquals(hourAngle(9, 41), time.hourAngle, epsilon)
        assertEquals(minuteAngle(41, 15), time.minuteAngle, epsilon)
        assertEquals(secondAngle(15, 500, smooth = true), time.secondAngle(smooth = true), epsilon)
        assertEquals(secondAngle(15, 500, smooth = false), time.secondAngle(smooth = false), epsilon)

        assertEquals("09", time.formattedHours(is24Hour = false))
        assertEquals("41", time.formattedMinutes())
        assertEquals("15", time.formattedSeconds())
        assertEquals("AM", time.amPm())
        assertEquals("09:41", time.formattedTime())
        assertEquals("09:41:15", time.formattedTime(includeSeconds = true))

        // Afternoon 24-hour test
        val afternoon = ClockTime(hours = 21, minutes = 5, seconds = 0, millis = 0, is24Hour = true)
        assertEquals("21", afternoon.formattedHours(is24Hour = true))
        assertEquals("09", afternoon.formattedHours(is24Hour = false))
        assertEquals("05", afternoon.formattedMinutes())
        assertEquals("PM", afternoon.amPm())
    }

    @Test
    fun testIsDaytime_checks() {
        assertTrue(isDaytime(720, 360, 1080)) // Noon is daytime
        assertTrue(isDaytime(360, 360, 1080)) // Sunrise edge
        assertTrue(isDaytime(1080, 360, 1080)) // Sunset edge
        assertFalse(isDaytime(300, 360, 1080)) // 5:00 AM is night
        assertFalse(isDaytime(1200, 360, 1080)) // 8:00 PM is night
    }
}
