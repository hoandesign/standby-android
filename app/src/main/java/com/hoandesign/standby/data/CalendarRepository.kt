package com.hoandesign.standby.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.hoandesign.standby.model.CalendarEvent
import com.hoandesign.standby.ui.theme.AccentBlue
import com.hoandesign.standby.ui.theme.AccentGreen
import com.hoandesign.standby.ui.theme.AccentOrange
import com.hoandesign.standby.ui.theme.AccentPurple
import com.hoandesign.standby.ui.theme.NightRed
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Repository responsible for retrieving calendar events from the device's system calendar
 * via [CalendarContract.Events], or providing high-fidelity built-in preview events
 * when permission is not granted or the user's calendar is empty.
 */
class CalendarRepository(
    private val context: Context? = null
) {
    /**
     * Checks if the [Manifest.permission.READ_CALENDAR] permission has been granted.
     */
    fun hasCalendarPermission(): Boolean {
        val ctx = context ?: return false
        return ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Retrieves upcoming calendar events. Queries [CalendarContract.Events] if permission
     * is granted and returns system events. If permission is lacking, an error occurs, or
     * no upcoming events exist, falls back gracefully to high-fidelity sample events.
     */
    fun getUpcomingEvents(baseTimeMillis: Long = System.currentTimeMillis()): List<CalendarEvent> {
        val ctx = context
        if (ctx != null && hasCalendarPermission()) {
            val systemEvents = querySystemEvents(ctx, baseTimeMillis)
            if (systemEvents.isNotEmpty()) {
                return systemEvents
            }
        }
        return getSampleEvents(baseTimeMillis)
    }

    /**
     * Queries the system [CalendarContract.Events] table.
     */
    private fun querySystemEvents(
        context: Context,
        baseTimeMillis: Long
    ): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.DISPLAY_COLOR,
            CalendarContract.Events.ALL_DAY
        )

        val selection = "${CalendarContract.Events.DTEND} >= ? AND ${CalendarContract.Events.DELETED} = 0"
        val selectionArgs = arrayOf(baseTimeMillis.toString())
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC LIMIT 20"

        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
        val zoneId = ZoneId.systemDefault()

        try {
            context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(CalendarContract.Events._ID)
                val titleIdx = cursor.getColumnIndex(CalendarContract.Events.TITLE)
                val startIdx = cursor.getColumnIndex(CalendarContract.Events.DTSTART)
                val endIdx = cursor.getColumnIndex(CalendarContract.Events.DTEND)
                val locIdx = cursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)
                val colorIdx = cursor.getColumnIndex(CalendarContract.Events.DISPLAY_COLOR)
                val allDayIdx = cursor.getColumnIndex(CalendarContract.Events.ALL_DAY)

                val fallbackColors = listOf(NightRed, AccentBlue, AccentOrange, AccentPurple, AccentGreen)
                var colorCounter = 0

                while (cursor.moveToNext()) {
                    val id = if (idIdx != -1) cursor.getLong(idIdx) else 0L
                    val title = if (titleIdx != -1) cursor.getString(titleIdx) ?: "Untitled Event" else "Untitled Event"
                    val dtStart = if (startIdx != -1) cursor.getLong(startIdx) else baseTimeMillis
                    val dtEnd = if (endIdx != -1) cursor.getLong(endIdx) else dtStart + 3600_000L
                    val location = if (locIdx != -1) cursor.getString(locIdx) else null
                    val allDay = if (allDayIdx != -1) cursor.getInt(allDayIdx) == 1 else false
                    val colorVal = if (colorIdx != -1) cursor.getInt(colorIdx) else 0

                    val eventColor = if (colorVal != 0) {
                        Color(colorVal)
                    } else {
                        fallbackColors[colorCounter % fallbackColors.size].also { colorCounter++ }
                    }

                    val startFormatted = if (allDay) {
                        "All Day"
                    } else {
                        Instant.ofEpochMilli(dtStart).atZone(zoneId).format(timeFormatter)
                    }
                    val endFormatted = if (allDay) {
                        "All Day"
                    } else {
                        Instant.ofEpochMilli(dtEnd).atZone(zoneId).format(timeFormatter)
                    }

                    events.add(
                        CalendarEvent(
                            id = id,
                            title = title,
                            startTimeFormatted = startFormatted,
                            endTimeFormatted = endFormatted,
                            location = location,
                            color = eventColor,
                            startEpochMillis = dtStart,
                            endEpochMillis = dtEnd,
                            isAllDay = allDay
                        )
                    )
                }
            }
        } catch (_: SecurityException) {
            // Permission revoked concurrently
        } catch (_: Exception) {
            // SQLite or cursor errors
        }

        return events
    }

    companion object {
        /**
         * Returns curated, realistic calendar events aligned with user reference design.
         * Dynamically calculated relative to [baseTimeMillis] so countdowns and times
         * are always fresh and vibrant.
         */
        fun getSampleEvents(baseTimeMillis: Long = System.currentTimeMillis()): List<CalendarEvent> {
            val zoneId = ZoneId.systemDefault()
            val now = Instant.ofEpochMilli(baseTimeMillis).atZone(zoneId)
            val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

            // Event 1: Product Design Review (starts in 20 min)
            val start1 = now.plusMinutes(20)
            val end1 = start1.plusMinutes(45)

            // Event 2: Team Sync (starts in 1h 15m)
            val start2 = now.plusMinutes(75)
            val end2 = start2.plusMinutes(45)

            // Event 3: Sprint Planning (starts in 3h)
            val start3 = now.plusHours(3)
            val end3 = start3.plusMinutes(60)

            // Event 4: Design System QA (starts in 5h)
            val start4 = now.plusHours(5)
            val end4 = start4.plusHours(1)

            return listOf(
                CalendarEvent(
                    id = 101L,
                    title = "Product Design Review",
                    startTimeFormatted = start1.format(formatter),
                    endTimeFormatted = end1.format(formatter),
                    location = "Design Lab · Zoom",
                    color = NightRed,
                    startEpochMillis = start1.toInstant().toEpochMilli(),
                    endEpochMillis = end1.toInstant().toEpochMilli()
                ),
                CalendarEvent(
                    id = 102L,
                    title = "Team Sync",
                    startTimeFormatted = start2.format(formatter),
                    endTimeFormatted = end2.format(formatter),
                    location = "Meeting Room 4B",
                    color = AccentBlue,
                    startEpochMillis = start2.toInstant().toEpochMilli(),
                    endEpochMillis = end2.toInstant().toEpochMilli()
                ),
                CalendarEvent(
                    id = 103L,
                    title = "Sprint Planning",
                    startTimeFormatted = start3.format(formatter),
                    endTimeFormatted = end3.format(formatter),
                    location = "Virtual / Hangouts",
                    color = AccentOrange,
                    startEpochMillis = start3.toInstant().toEpochMilli(),
                    endEpochMillis = end3.toInstant().toEpochMilli()
                ),
                CalendarEvent(
                    id = 104L,
                    title = "Design System QA",
                    startTimeFormatted = start4.format(formatter),
                    endTimeFormatted = end4.format(formatter),
                    location = "Figma Review Suite",
                    color = AccentPurple,
                    startEpochMillis = start4.toInstant().toEpochMilli(),
                    endEpochMillis = end4.toInstant().toEpochMilli()
                )
            )
        }
    }
}
