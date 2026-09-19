package com.hoandesign.standby.model

import java.time.LocalTime
import java.util.Locale

/**
 * Pure angle calculation for analog clock hour hand.
 *
 * @param hours Hour of the day (0..23 or 1..12).
 * @param minutes Minute of the hour (0..59).
 * @return Hour hand rotation angle in degrees [0f, 360f).
 */
fun hourAngle(hours: Int, minutes: Int): Float =
    ((hours % 12) + minutes / 60f) * 30f

/**
 * Pure angle calculation for analog clock minute hand.
 *
 * @param minutes Minute of the hour (0..59).
 * @param seconds Second of the minute (0..59).
 * @return Minute hand rotation angle in degrees [0f, 360f).
 */
fun minuteAngle(minutes: Int, seconds: Int = 0): Float =
    (minutes + seconds / 60f) * 6f

/**
 * Pure angle calculation for analog clock second hand.
 *
 * @param seconds Second of the minute (0..59).
 * @param millis Millisecond of the second (0..999).
 * @param smooth If true, includes sub-second millisecond precision for continuous 60fps sweep.
 * @return Second hand rotation angle in degrees [0f, 360f).
 */
fun secondAngle(seconds: Int, millis: Int = 0, smooth: Boolean = true): Float =
    if (smooth) (seconds + millis / 1000f) * 6f else seconds * 6f

/**
 * Pure angle calculation for analog clock second hand without millis parameter.
 */
fun secondAngle(seconds: Int, smooth: Boolean): Float =
    secondAngle(seconds = seconds, millis = 0, smooth = smooth)

/**
 * Calculates normalized solar daylight progress along the horizon arc.
 *
 * @param timeMinutesOfDay Minute of the day from midnight (0..1439).
 * @param sunriseMinutes Minute of the day at sunrise (default 360 = 6:00 AM).
 * @param sunsetMinutes Minute of the day at sunset (default 1080 = 6:00 PM).
 * @return Progress value where 0.0f is sunrise, 0.5f is solar noon, and 1.0f is sunset.
 *         Returns 0.0f before sunrise and 1.0f after sunset.
 */
fun solarProgress(
    timeMinutesOfDay: Int,
    sunriseMinutes: Int = 360,
    sunsetMinutes: Int = 1080
): Float {
    val daySpan = sunsetMinutes - sunriseMinutes
    if (daySpan <= 0) return 0f
    return when {
        timeMinutesOfDay <= sunriseMinutes -> 0f
        timeMinutesOfDay >= sunsetMinutes -> 1f
        else -> (timeMinutesOfDay - sunriseMinutes).toFloat() / daySpan
    }
}

/**
 * Helper to determine if a given minute of the day falls within daylight hours.
 */
fun isDaytime(
    timeMinutesOfDay: Int,
    sunriseMinutes: Int = 360,
    sunsetMinutes: Int = 1080
): Boolean = timeMinutesOfDay in sunriseMinutes..sunsetMinutes

/**
 * Immutable time snapshot designed for StandBy clock faces.
 *
 * @param hours Hour of the day (0..23).
 * @param minutes Minute of the hour (0..59).
 * @param seconds Second of the minute (0..59).
 * @param millis Millisecond of the current second (0..999).
 * @param is24Hour True if formatted output should use 24-hour time.
 */
data class ClockTime(
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val millis: Int = 0,
    val is24Hour: Boolean = false
) {
    /**
     * Elapsed minutes of the current day (0..1439).
     */
    val timeMinutesOfDay: Int
        get() = hours * 60 + minutes

    /**
     * Continuous hour hand angle in degrees [0f, 360f).
     */
    val hourAngle: Float
        get() = hourAngle(hours, minutes)

    /**
     * Continuous minute hand angle in degrees [0f, 360f).
     */
    val minuteAngle: Float
        get() = minuteAngle(minutes, seconds)

    /**
     * Second hand angle in degrees [0f, 360f).
     */
    fun secondAngle(smooth: Boolean = true): Float =
        secondAngle(seconds, millis, smooth)

    /**
     * Current solar daylight progress along the daytime horizon arc [0f, 1f].
     */
    val solarProgress: Float
        get() = solarProgress(timeMinutesOfDay)

    /**
     * True if the current time falls within default daylight hours (6:00 AM - 6:00 PM).
     */
    val isDaytime: Boolean
        get() = isDaytime(timeMinutesOfDay)

    /**
     * Formats the hour string padded to 2 digits (e.g. "09" or "14").
     */
    fun formattedHours(is24Hour: Boolean = this.is24Hour): String {
        val h = if (is24Hour) {
            hours
        } else {
            val rem = hours % 12
            if (rem == 0) 12 else rem
        }
        return String.format(Locale.US, "%02d", h)
    }

    /**
     * Formats the minute string padded to 2 digits (e.g. "41").
     */
    fun formattedMinutes(): String =
        String.format(Locale.US, "%02d", minutes)

    /**
     * Formats the second string padded to 2 digits (e.g. "05").
     */
    fun formattedSeconds(): String =
        String.format(Locale.US, "%02d", seconds)

    /**
     * Returns "AM" or "PM" indicator based on the 24-hour hour value.
     */
    fun amPm(): String =
        if (hours < 12) "AM" else "PM"

    /**
     * Formats time string e.g. "09:41" or "14:30".
     */
    fun formattedTime(includeSeconds: Boolean = false): String {
        val base = "${formattedHours()}:${formattedMinutes()}"
        return if (includeSeconds) "$base:${formattedSeconds()}" else base
    }

    companion object {
        /**
         * Obtains the current system time snapshot with millisecond precision.
         */
        fun now(is24Hour: Boolean = false): ClockTime {
            val localTime = LocalTime.now()
            val millis = localTime.nano / 1_000_000
            return ClockTime(
                hours = localTime.hour,
                minutes = localTime.minute,
                seconds = localTime.second,
                millis = millis,
                is24Hour = is24Hour
            )
        }

        /**
         * Standard Apple / StandBy preview time "09:41".
         */
        val Sample0941 = ClockTime(
            hours = 9,
            minutes = 41,
            seconds = 0,
            millis = 0,
            is24Hour = false
        )

        /**
         * Classic Swiss analog display time "10:08:42".
         */
        val Sample1008 = ClockTime(
            hours = 10,
            minutes = 8,
            seconds = 42,
            millis = 0,
            is24Hour = false
        )
    }
}
