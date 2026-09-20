package com.hoandesign.standby.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.hoandesign.standby.ui.widgets.battery.BatteryWidget
import com.hoandesign.standby.ui.widgets.calendar.AgendaWidget
import com.hoandesign.standby.ui.widgets.calendar.MonthCalendarWidget
import com.hoandesign.standby.ui.widgets.clock.AnalogClockWidget
import com.hoandesign.standby.ui.widgets.clock.BigDigitalClockWidget
import com.hoandesign.standby.ui.widgets.clock.RadialClockWidget
import com.hoandesign.standby.ui.widgets.clock.RetroFlipClockWidget
import com.hoandesign.standby.ui.widgets.clock.SolarArcClockWidget
import com.hoandesign.standby.ui.widgets.media.MusicPlayerWidget
import com.hoandesign.standby.ui.widgets.photo.PhotoFrameWidget
import com.hoandesign.standby.ui.widgets.system.SystemBentoWidget
import com.hoandesign.standby.ui.widgets.timer.DeskTimerWidget
import com.hoandesign.standby.ui.widgets.vibes.VibesWidget
import com.hoandesign.standby.ui.widgets.weather.WeatherWidget

/**
 * All 14 modular widgets available for Left & Right Bento Slots in StandBy.
 */
enum class StandbyWidgetId(
    val title: String,
    val category: String,
    val description: String
) {
    ANALOG_CLOCK(
        title = "Analog Clock",
        category = "Clock",
        description = "Swiss Bauhaus dial with sweeping second hand"
    ),
    BIG_DIGITAL_CLOCK(
        title = "Big Digital",
        category = "Clock",
        description = "Monumental StandBy typography with date & alarm"
    ),
    RETRO_FLIP_CLOCK(
        title = "Retro Flip",
        category = "Clock",
        description = "Vintage split-flap mechanical station clock"
    ),
    RADIAL_CLOCK(
        title = "Radial Clock",
        category = "Clock",
        description = "Concentric glowing hour and minute orbit rings"
    ),
    SOLAR_ARC_CLOCK(
        title = "Solar Arc",
        category = "Clock",
        description = "Celestial sun trajectory across dawn, noon & dusk"
    ),
    WEATHER(
        title = "Live Weather",
        category = "Weather",
        description = "Current temperature, sky condition & weekly forecast"
    ),
    MONTH_CALENDAR(
        title = "Month Calendar",
        category = "Productivity",
        description = "Classic StandBy red header and monthly date grid"
    ),
    AGENDA(
        title = "Daily Agenda",
        category = "Productivity",
        description = "Upcoming meetings, countdowns and schedule list"
    ),
    BATTERY(
        title = "Battery & Power",
        category = "System",
        description = "Apple circular arc ring, fast charging wattage & health"
    ),
    MUSIC_PLAYER(
        title = "Now Playing",
        category = "Media",
        description = "Spinning vinyl record disc and scrubbable playback"
    ),
    SYSTEM_BENTO(
        title = "System Specs",
        category = "System",
        description = "Real-time RAM, storage, CPU telemetry and thermal gauge"
    ),
    DESK_TIMER(
        title = "Desk Timer",
        category = "Productivity",
        description = "Pomodoro countdown ring and focus interval presets"
    ),
    VIBES(
        title = "Ambient Vibes",
        category = "Media",
        description = "Lo-fi rain, vinyl crackle and sound generator"
    ),
    PHOTO_FRAME(
        title = "Photo Frame",
        category = "Media",
        description = "Curated ambient photography with subtle ken-burns motion"
    );

    val icon: ImageVector
        get() = when (this) {
            ANALOG_CLOCK -> Icons.Default.AccessTime
            BIG_DIGITAL_CLOCK -> Icons.Default.Schedule
            RETRO_FLIP_CLOCK -> Icons.Default.AccessTime
            RADIAL_CLOCK -> Icons.Default.AccessTime
            SOLAR_ARC_CLOCK -> Icons.Default.WbSunny
            WEATHER -> Icons.Default.WbSunny
            MONTH_CALENDAR -> Icons.Default.CalendarMonth
            AGENDA -> Icons.Default.CalendarToday
            BATTERY -> Icons.Default.BatteryChargingFull
            MUSIC_PLAYER -> Icons.Default.MusicNote
            SYSTEM_BENTO -> Icons.Default.Memory
            DESK_TIMER -> Icons.Default.Timer
            VIBES -> Icons.Default.GraphicEq
            PHOTO_FRAME -> Icons.Default.PhotoLibrary
        }
}

/**
 * Registry providing Composable renderers for widgets in both compact bento slots
 * and full-screen immersive mode.
 */
object StandbyWidgetRegistry {

    val allWidgets: List<StandbyWidgetId> = StandbyWidgetId.entries

    val defaultLeftSlot: List<StandbyWidgetId> = listOf(
        StandbyWidgetId.ANALOG_CLOCK,
        StandbyWidgetId.BIG_DIGITAL_CLOCK,
        StandbyWidgetId.WEATHER,
        StandbyWidgetId.RETRO_FLIP_CLOCK,
        StandbyWidgetId.SOLAR_ARC_CLOCK,
        StandbyWidgetId.RADIAL_CLOCK
    )

    val defaultRightSlot: List<StandbyWidgetId> = listOf(
        StandbyWidgetId.MONTH_CALENDAR,
        StandbyWidgetId.BATTERY,
        StandbyWidgetId.MUSIC_PLAYER,
        StandbyWidgetId.AGENDA,
        StandbyWidgetId.SYSTEM_BENTO,
        StandbyWidgetId.DESK_TIMER,
        StandbyWidgetId.VIBES,
        StandbyWidgetId.PHOTO_FRAME
    )

    fun serializeWidgetList(list: List<StandbyWidgetId>): String =
        list.joinToString(",") { it.name }

    fun deserializeWidgetList(raw: String?, fallback: List<StandbyWidgetId>): List<StandbyWidgetId> {
        if (raw.isNullOrBlank()) return fallback
        val parsed = raw.split(",").mapNotNull { name ->
            try {
                StandbyWidgetId.valueOf(name.trim())
            } catch (e: Exception) {
                null
            }
        }
        return if (parsed.isNotEmpty()) parsed else fallback
    }

    /**
     * Renders the compact widget inside a dual bento slot card.
     */
    @Composable
    fun RenderCompact(
        widgetId: StandbyWidgetId,
        accentColor: Color,
        modifier: Modifier = Modifier
    ) {
        when (widgetId) {
            StandbyWidgetId.ANALOG_CLOCK -> AnalogClockWidget(modifier = modifier)
            StandbyWidgetId.BIG_DIGITAL_CLOCK -> BigDigitalClockWidget(accentColor = accentColor, modifier = modifier)
            StandbyWidgetId.RETRO_FLIP_CLOCK -> RetroFlipClockWidget(accentColor = accentColor, modifier = modifier)
            StandbyWidgetId.RADIAL_CLOCK -> RadialClockWidget(accentColor = accentColor, modifier = modifier)
            StandbyWidgetId.SOLAR_ARC_CLOCK -> SolarArcClockWidget(accentColor = accentColor, modifier = modifier)
            StandbyWidgetId.WEATHER -> WeatherWidget(modifier = modifier)
            StandbyWidgetId.MONTH_CALENDAR -> MonthCalendarWidget(modifier = modifier)
            StandbyWidgetId.AGENDA -> AgendaWidget(modifier = modifier)
            StandbyWidgetId.BATTERY -> BatteryWidget(isFullscreen = false, modifier = modifier)
            StandbyWidgetId.MUSIC_PLAYER -> MusicPlayerWidget(isCompact = true, modifier = modifier)
            StandbyWidgetId.SYSTEM_BENTO -> SystemBentoWidget(modifier = modifier)
            StandbyWidgetId.DESK_TIMER -> DeskTimerWidget(accentColor = accentColor, modifier = modifier)
            StandbyWidgetId.VIBES -> VibesWidget(modifier = modifier)
            StandbyWidgetId.PHOTO_FRAME -> PhotoFrameWidget(modifier = modifier)
        }
    }

    /**
     * Renders the true full-screen immersive view for a single slot widget.
     */
    @Composable
    fun RenderFullscreen(
        widgetId: StandbyWidgetId,
        accentColor: Color,
        modifier: Modifier = Modifier
    ) {
        when (widgetId) {
            StandbyWidgetId.ANALOG_CLOCK -> AnalogClockWidget(modifier = modifier)
            StandbyWidgetId.BIG_DIGITAL_CLOCK -> BigDigitalClockWidget(accentColor = accentColor, modifier = modifier)
            StandbyWidgetId.RETRO_FLIP_CLOCK -> RetroFlipClockWidget(accentColor = accentColor, modifier = modifier)
            StandbyWidgetId.RADIAL_CLOCK -> RadialClockWidget(accentColor = accentColor, modifier = modifier)
            StandbyWidgetId.SOLAR_ARC_CLOCK -> SolarArcClockWidget(accentColor = accentColor, modifier = modifier)
            StandbyWidgetId.WEATHER -> WeatherWidget(modifier = modifier)
            StandbyWidgetId.MONTH_CALENDAR -> MonthCalendarWidget(modifier = modifier)
            StandbyWidgetId.AGENDA -> AgendaWidget(modifier = modifier)
            StandbyWidgetId.BATTERY -> BatteryWidget(isFullscreen = true, modifier = modifier)
            StandbyWidgetId.MUSIC_PLAYER -> MusicPlayerWidget(isCompact = false, modifier = modifier)
            StandbyWidgetId.SYSTEM_BENTO -> SystemBentoWidget(modifier = modifier)
            StandbyWidgetId.DESK_TIMER -> DeskTimerWidget(accentColor = accentColor, modifier = modifier)
            StandbyWidgetId.VIBES -> VibesWidget(modifier = modifier)
            StandbyWidgetId.PHOTO_FRAME -> PhotoFrameWidget(modifier = modifier)
        }
    }
}
