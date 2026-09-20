package com.hoandesign.standby.model

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoandesign.standby.ui.widgets.battery.BatteryWidget
import com.hoandesign.standby.ui.widgets.calendar.ScheduleWidget
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

enum class StandbyWidgetId(
    val title: String,
    val category: String,
    val description: String
) {
    ANALOG_CLOCK("Analog Clock", "Clock", "Swiss Bauhaus dial with sweeping second hand"),
    BIG_DIGITAL_CLOCK("Big Digital", "Clock", "Monumental StandBy typography with date & alarm"),
    RETRO_FLIP_CLOCK("Retro Flip", "Clock", "Vintage split-flap mechanical station clock"),
    RADIAL_CLOCK("Radial Clock", "Clock", "Concentric glowing hour and minute orbit rings"),
    SOLAR_ARC_CLOCK("Solar Arc", "Clock", "Celestial sun trajectory across dawn, noon & dusk"),
    WEATHER("Live Weather", "Weather", "Current temperature, sky condition & weekly forecast"),
    MONTH_CALENDAR("Month Calendar", "Productivity", "Classic StandBy red header and monthly date grid"),
    AGENDA("Daily Agenda", "Productivity", "Upcoming meetings, countdowns and schedule list"),
    BATTERY("Battery & Power", "System", "Apple circular arc ring, fast charging wattage & health"),
    MUSIC_PLAYER("Now Playing", "Media", "Spinning vinyl record disc and scrubbable playback"),
    SYSTEM_BENTO("System Specs", "System", "Real-time RAM, storage, CPU telemetry and thermal gauge"),
    DESK_TIMER("Desk Timer", "Productivity", "Pomodoro countdown ring and focus interval presets"),
    VIBES("Ambient Vibes", "Media", "Lo-fi rain, vinyl crackle and sound generator"),
    PHOTO_FRAME("Photo Frame", "Media", "Curated ambient photography with subtle ken-burns motion");

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

object StandbyWidgetRegistry {
    val allWidgets: List<StandbyWidgetId> = StandbyWidgetId.entries
    val defaultLeftSlot: List<StandbyWidgetId> = listOf(
        StandbyWidgetId.ANALOG_CLOCK, StandbyWidgetId.BIG_DIGITAL_CLOCK, StandbyWidgetId.WEATHER,
        StandbyWidgetId.RETRO_FLIP_CLOCK, StandbyWidgetId.SOLAR_ARC_CLOCK, StandbyWidgetId.RADIAL_CLOCK
    )
    val defaultRightSlot: List<StandbyWidgetId> = listOf(
        StandbyWidgetId.MONTH_CALENDAR, StandbyWidgetId.BATTERY, StandbyWidgetId.MUSIC_PLAYER,
        StandbyWidgetId.AGENDA, StandbyWidgetId.SYSTEM_BENTO, StandbyWidgetId.DESK_TIMER,
        StandbyWidgetId.VIBES, StandbyWidgetId.PHOTO_FRAME
    )

    fun serializeWidgetList(list: List<StandbyWidgetId>): String = list.joinToString(",") { it.name }

    fun deserializeWidgetList(raw: String?, fallback: List<StandbyWidgetId>): List<StandbyWidgetId> {
        if (raw.isNullOrBlank()) return fallback
        val parsed = raw.split(",").mapNotNull { name ->
            try { StandbyWidgetId.valueOf(name.trim()) } catch (e: Exception) { null }
        }
        return if (parsed.isNotEmpty()) parsed else fallback
    }

    @Composable
    fun RenderCompact(widgetId: StandbyWidgetId, accentColor: Color, modifier: Modifier = Modifier) {
        when (widgetId) {
            StandbyWidgetId.ANALOG_CLOCK -> AnalogClockWidget(modifier = modifier)
            StandbyWidgetId.BIG_DIGITAL_CLOCK -> BigDigitalClockWidget(accentColor = accentColor, modifier = modifier)
            StandbyWidgetId.RETRO_FLIP_CLOCK -> RetroFlipClockWidget(accentColor = accentColor, modifier = modifier)
            StandbyWidgetId.RADIAL_CLOCK -> RadialClockWidget(accentColor = accentColor, modifier = modifier)
            StandbyWidgetId.SOLAR_ARC_CLOCK -> SolarArcClockWidget(accentColor = accentColor, modifier = modifier)
            StandbyWidgetId.WEATHER -> WeatherWidget(modifier = modifier)
            StandbyWidgetId.MONTH_CALENDAR -> MonthCalendarWidget(modifier = modifier)
            StandbyWidgetId.AGENDA -> ScheduleWidget(modifier = modifier)
            StandbyWidgetId.BATTERY -> BatteryWidget(isFullscreen = false, modifier = modifier)
            StandbyWidgetId.MUSIC_PLAYER -> MusicPlayerWidget(isCompact = true, modifier = modifier)
            StandbyWidgetId.SYSTEM_BENTO -> SystemBentoWidget(modifier = modifier)
            StandbyWidgetId.DESK_TIMER -> DeskTimerWidget(accentColor = accentColor, modifier = modifier)
            StandbyWidgetId.VIBES -> VibesWidget(modifier = modifier)
            StandbyWidgetId.PHOTO_FRAME -> PhotoFrameWidget(modifier = modifier)
        }
    }

    @Composable
    fun RenderFullscreen(widgetId: StandbyWidgetId, accentColor: Color, modifier: Modifier = Modifier) {
        when (widgetId) {
            StandbyWidgetId.ANALOG_CLOCK -> FullscreenAnalogClock(modifier)
            StandbyWidgetId.RETRO_FLIP_CLOCK -> FullscreenDigitalClock(modifier)
            StandbyWidgetId.WEATHER -> FullscreenWeather(modifier)
            StandbyWidgetId.MONTH_CALENDAR -> FullscreenMonthCalendar(modifier)
            StandbyWidgetId.AGENDA -> FullscreenSchedule(modifier)
            StandbyWidgetId.SYSTEM_BENTO -> FullscreenStocks(modifier)
            StandbyWidgetId.VIBES -> FullscreenHealth(modifier)
            StandbyWidgetId.DESK_TIMER -> FullscreenTimers(modifier)
            StandbyWidgetId.PHOTO_FRAME -> FullscreenPhotoFrame(modifier)
            StandbyWidgetId.SOLAR_ARC_CLOCK -> FullscreenWorldClock(modifier)
            StandbyWidgetId.BIG_DIGITAL_CLOCK -> BigDigitalClockWidget(accentColor = accentColor, modifier = modifier)
            StandbyWidgetId.BATTERY -> BatteryWidget(isFullscreen = true, modifier = modifier)
            StandbyWidgetId.MUSIC_PLAYER -> MusicPlayerWidget(isCompact = false, modifier = modifier)
            StandbyWidgetId.RADIAL_CLOCK -> RadialClockWidget(accentColor = accentColor, modifier = modifier)
        }
    }
}

@Composable
fun FullscreenAnalogClock(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(300.dp).border(4.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
            Text("12", color = Color.White, fontSize = 24.sp, modifier = Modifier.align(Alignment.TopCenter).padding(8.dp))
            Text("3", color = Color.White, fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(8.dp))
            Text("6", color = Color.White, fontSize = 24.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp))
            Text("9", color = Color.White, fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterStart).padding(8.dp))
            Box(modifier = Modifier.width(4.dp).height(120.dp).background(Color.White).align(Alignment.BottomCenter).offset(y = (-150).dp))
            Box(modifier = Modifier.width(2.dp).height(140.dp).background(Color(0xFFFFA500)).align(Alignment.BottomCenter).offset(y = (-150).dp))
        }
        Text("PST", color = Color.Gray, modifier = Modifier.align(Alignment.BottomStart).padding(32.dp))
        Text("10:09", color = Color.White, fontSize = 32.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(32.dp))
    }
}

@Composable
fun FullscreenDigitalClock(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("10:09", fontSize = 120.sp, color = Color.White, fontWeight = FontWeight.Bold, style = androidx.compose.ui.text.TextStyle(drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)))
            Text("WED, SEP 20", fontSize = 24.sp, color = Color(0xFFFFA500), fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.padding(top = 16.dp)) {
                AssistChip(onClick = {}, label = { Text("72° Sunny") })
                Spacer(modifier = Modifier.width(8.dp))
                AssistChip(onClick = {}, label = { Text("100% Battery") })
            }
        }
    }
}

@Composable
fun FullscreenWeather(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().background(Color(0xFF1E1E1E)).padding(24.dp)) {
        Text("72°", fontSize = 96.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Text("San Francisco | Sunny", color = Color.Gray, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth().height(64.dp).background(Color.DarkGray, RoundedCornerShape(16.dp))) {}
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.DarkGray, RoundedCornerShape(16.dp))) {}
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.DarkGray, RoundedCornerShape(16.dp)))
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.DarkGray, RoundedCornerShape(16.dp)))
            }
        }
    }
}

@Composable
fun FullscreenMonthCalendar(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxSize().background(Color.Black)) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp).background(Color.DarkGray, RoundedCornerShape(16.dp)))
        Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp).background(Color.DarkGray, RoundedCornerShape(16.dp)))
    }
}

@Composable
fun FullscreenSchedule(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().background(Color.Black).padding(24.dp)) {
        Text("Today's Agenda", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color.DarkGray, RoundedCornerShape(16.dp))) {
            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color.Red).align(Alignment.Center))
        }
    }
}

@Composable
fun FullscreenStocks(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().background(Color.Black).padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Markets", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Vol: 1.2B", color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.DarkGray, RoundedCornerShape(16.dp)))
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.DarkGray, RoundedCornerShape(16.dp)))
        }
    }
}

@Composable
fun FullscreenHealth(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxSize().background(Color.Black).padding(24.dp)) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(200.dp).border(16.dp, Color.Red, CircleShape))
            Box(modifier = Modifier.size(150.dp).border(16.dp, Color.Green, CircleShape))
            Box(modifier = Modifier.size(100.dp).border(16.dp, Color.Cyan, CircleShape))
        }
        Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.SpaceEvenly) {
            Text("Steps: 8,240", color = Color.White, fontSize = 24.sp)
            Text("Distance: 5.2 km", color = Color.White, fontSize = 24.sp)
            Text("Cal: 420 kcal", color = Color.White, fontSize = 24.sp)
            Text("BPM: 68", color = Color.White, fontSize = 24.sp)
        }
    }
}

@Composable
fun FullscreenTimers(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxSize().background(Color.Black).padding(24.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.size(200.dp).border(8.dp, Color(0xFFFFA500), CircleShape), contentAlignment = Alignment.Center) {
                Text("14:59", fontSize = 48.sp, color = Color.White)
            }
            Row(modifier = Modifier.padding(top = 16.dp)) {
                Button(onClick = {}) { Text("+1m") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {}) { Text("+5m") }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.size(200.dp).border(8.dp, Color.Cyan, CircleShape), contentAlignment = Alignment.Center) {
                Text("05:00", fontSize = 48.sp, color = Color.White)
            }
            Row(modifier = Modifier.padding(top = 16.dp)) {
                Button(onClick = {}) { Text("+1m") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {}) { Text("+5m") }
            }
        }
    }
}

@Composable
fun FullscreenPhotoFrame(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(Color.DarkGray)) {
        Text("2026-09-20", color = Color.White, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp))
        Text("Yosemite National Park", color = Color.White, modifier = Modifier.align(Alignment.BottomStart).padding(16.dp))
    }
}

@Composable
fun FullscreenWorldClock(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().background(Color.Black).padding(24.dp)) {
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.DarkGray, RoundedCornerShape(16.dp)).padding(8.dp), contentAlignment = Alignment.Center) {
                Text("New York\n10:09 AM", color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.DarkGray, RoundedCornerShape(16.dp)).padding(8.dp), contentAlignment = Alignment.Center) {
                Text("London\n3:09 PM", color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.DarkGray, RoundedCornerShape(16.dp)).padding(8.dp), contentAlignment = Alignment.Center) {
                Text("Tokyo\n11:09 PM", color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.DarkGray, RoundedCornerShape(16.dp)).padding(8.dp), contentAlignment = Alignment.Center) {
                Text("Hanoi\n9:09 PM", color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}
