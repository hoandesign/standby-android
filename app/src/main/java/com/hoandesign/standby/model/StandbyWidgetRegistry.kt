package com.hoandesign.standby.model

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
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
    PHOTO_FRAME("Photo Frame", "Media", "Curated ambient photography with subtle ken-burns motion"),
    RECTANGLE_CLOCK("Rectangle Clock", "Clock", "Architectural rectangular dial maximizing screen real estate");

    val icon: ImageVector
        get() = when (this) {
            ANALOG_CLOCK -> Icons.Default.AccessTime
            RECTANGLE_CLOCK -> Icons.Default.AccessTime
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
        StandbyWidgetId.ANALOG_CLOCK, StandbyWidgetId.RECTANGLE_CLOCK, StandbyWidgetId.BIG_DIGITAL_CLOCK, StandbyWidgetId.WEATHER,
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
            StandbyWidgetId.RECTANGLE_CLOCK -> com.hoandesign.standby.ui.widgets.clock.RectangleAnalogClockWidget(accentColor = accentColor, modifier = modifier)
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
            StandbyWidgetId.RECTANGLE_CLOCK -> com.hoandesign.standby.ui.widgets.clock.RectangleAnalogClockWidget(accentColor = accentColor, modifier = modifier.fillMaxSize().padding(16.dp))
            StandbyWidgetId.ANALOG_CLOCK -> AnalogClockWidget(modifier = modifier.fillMaxSize().padding(16.dp))
            StandbyWidgetId.BIG_DIGITAL_CLOCK -> BigDigitalClockWidget(accentColor = accentColor, modifier = modifier.fillMaxSize().padding(16.dp))
            StandbyWidgetId.RETRO_FLIP_CLOCK -> RetroFlipClockWidget(accentColor = accentColor, modifier = modifier.fillMaxSize().padding(16.dp))
            StandbyWidgetId.RADIAL_CLOCK -> RadialClockWidget(accentColor = accentColor, modifier = modifier.fillMaxSize().padding(16.dp))
            StandbyWidgetId.SOLAR_ARC_CLOCK -> SolarArcClockWidget(accentColor = accentColor, modifier = modifier.fillMaxSize().padding(16.dp))
            StandbyWidgetId.WEATHER -> FullscreenWeather(modifier = modifier)
            StandbyWidgetId.MONTH_CALENDAR -> FullscreenMonthCalendar(modifier = modifier)
            StandbyWidgetId.AGENDA -> FullscreenSchedule(modifier = modifier)
            StandbyWidgetId.BATTERY -> BatteryWidget(isFullscreen = true, modifier = modifier)
            StandbyWidgetId.MUSIC_PLAYER -> MusicPlayerWidget(isCompact = false, modifier = modifier)
            StandbyWidgetId.SYSTEM_BENTO -> FullscreenSystemBento(accentColor = accentColor, modifier = modifier)
            StandbyWidgetId.DESK_TIMER -> FullscreenDeskTimer(accentColor = accentColor, modifier = modifier)
            StandbyWidgetId.VIBES -> FullscreenVibes(accentColor = accentColor, modifier = modifier)
            StandbyWidgetId.PHOTO_FRAME -> PhotoFrameWidget(modifier = modifier.fillMaxSize())
        }
    }
}

/**
 * Fullscreen live weather station powered by Open-Meteo and device GPS geocoding.
 * Adapts between vertical stacked layout (Portrait) and dual-column dashboard (Landscape).
 */
@Composable
fun FullscreenWeather(
    modifier: Modifier = Modifier,
    repository: com.hoandesign.standby.data.WeatherRepository = androidx.compose.runtime.remember { com.hoandesign.standby.data.WeatherRepository() }
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val isNightMode = com.hoandesign.standby.ui.theme.StandbyTheme.isNightMode

    var hasLocationPerm by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(com.hoandesign.standby.util.PermissionHelper.isLocationGranted(context))
    }
    var currentLat by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Double?>(null) }
    var currentLon by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Double?>(null) }
    var currentCity by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(if (hasLocationPerm) "Local Weather" else "Location Needed")
    }

    val locationLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPerm = granted
        if (granted) {
            coroutineScope.launch {
                val fresh = com.hoandesign.standby.data.location.LocationHelper.getFreshLocationAndCity(context)
                val loc = fresh.first
                if (loc != null) {
                    currentLat = loc.latitude
                    currentLon = loc.longitude
                    currentCity = fresh.second
                    repository.fetchWeather(loc.latitude, loc.longitude, fresh.second)
                }
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(hasLocationPerm) {
        if (hasLocationPerm) {
            val fresh = com.hoandesign.standby.data.location.LocationHelper.getFreshLocationAndCity(context)
            val loc = fresh.first
            if (loc != null) {
                currentLat = loc.latitude
                currentLon = loc.longitude
                currentCity = fresh.second
                repository.fetchWeather(loc.latitude, loc.longitude, fresh.second)
            }
        }
    }

    val weatherFlow = androidx.compose.runtime.remember(repository, currentLat, currentLon, currentCity) {
        repository.weatherFlow(currentLat, currentLon, currentCity)
    }
    val weather by weatherFlow.collectAsState(initial = repository.getCachedWeather())

    val tempUnit = com.hoandesign.standby.ui.theme.StandbyTheme.temperatureUnit
    val isCelsius = tempUnit == com.hoandesign.standby.model.TemperatureUnit.CELSIUS

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(com.hoandesign.standby.ui.theme.OledBlack)
            .padding(20.dp)
    ) {
        val isPortrait = maxHeight > maxWidth

        if (!hasLocationPerm) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        locationLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "LIVE WEATHER ACCESS",
                        color = if (isNightMode) com.hoandesign.standby.ui.theme.NightRed else com.hoandesign.standby.ui.theme.AccentOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 0.08.em
                    )
                    Text(
                        text = "Tap to grant location permission & view real local weather",
                        color = if (isNightMode) Color(0x99FF453A) else com.hoandesign.standby.ui.theme.TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else if (isPortrait) {
            // Portrait Layout: Stacked vertically
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header: City, Condition, Temperature
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = weather.cityName.uppercase(),
                                color = if (isNightMode) com.hoandesign.standby.ui.theme.NightRed else com.hoandesign.standby.ui.theme.TextTertiary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.08.em
                            )
                            Text(
                                text = weather.condition,
                                color = if (isNightMode) Color(0xCCFF453A) else com.hoandesign.standby.ui.theme.TextSecondary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        com.hoandesign.standby.ui.components.WeatherVectorIcon(
                            condition = weather.condition,
                            size = 40.dp,
                            isNightMode = isNightMode
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val displayTemp = if (isCelsius) "${weather.tempCelsius}°" else "${weather.tempFahrenheit}°"
                        val highDisplay = if (isCelsius) "${((weather.highTemp - 32) * 5 / 9)}°" else "${weather.highTemp}°"
                        val lowDisplay = if (isCelsius) "${((weather.lowTemp - 32) * 5 / 9)}°" else "${weather.lowTemp}°"

                        Text(
                            text = displayTemp,
                            fontSize = 68.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isNightMode) com.hoandesign.standby.ui.theme.NightRed else Color.White,
                            letterSpacing = (-0.03).em
                        )
                        Text(
                            text = "H: $highDisplay  L: $lowDisplay",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isNightMode) Color(0xAAFF453A) else com.hoandesign.standby.ui.theme.TextSecondary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }

                // Hourly Forecast Horizontal Cards
                if (weather.hourlyForecast.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "HOURLY FORECAST",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.08.em,
                            color = if (isNightMode) com.hoandesign.standby.ui.theme.NightRed else com.hoandesign.standby.ui.theme.TextTertiary
                        )
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(weather.hourlyForecast.size) { i ->
                                val hour = weather.hourlyForecast[i]
                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isNightMode) Color(0x18FF453A) else com.hoandesign.standby.ui.theme.StandbyCardBgSecondary)
                                        .border(1.dp, if (isNightMode) com.hoandesign.standby.ui.theme.NightRedDim else com.hoandesign.standby.ui.theme.StandbyBorder, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val hourDisplay = if (isCelsius) "${hour.tempCelsius}°" else "${hour.tempFahrenheit}°"
                                    Text(hour.timeLabel, fontSize = 11.sp, color = if (isNightMode) Color(0x99FF453A) else com.hoandesign.standby.ui.theme.TextSecondary)
                                    com.hoandesign.standby.ui.components.WeatherVectorIcon(
                                        condition = hour.conditionEmoji,
                                        size = 18.dp,
                                        isNightMode = isNightMode
                                    )
                                    Text(hourDisplay, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isNightMode) com.hoandesign.standby.ui.theme.NightRed else Color.White)
                                }
                            }
                        }
                    }
                }

                // Daily 5-Day Forecast Rows
                if (weather.dailyForecast.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isNightMode) Color(0x18FF453A) else com.hoandesign.standby.ui.theme.StandbyCardBgSecondary)
                            .border(1.dp, if (isNightMode) com.hoandesign.standby.ui.theme.NightRedDim else com.hoandesign.standby.ui.theme.StandbyBorder, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        weather.dailyForecast.take(5).forEach { day ->
                            val dLow = if (isCelsius) "${day.lowTempCelsius}°" else "${day.lowTempFahrenheit}°"
                            val dHigh = if (isCelsius) "${day.highTempCelsius}°" else "${day.highTempFahrenheit}°"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(day.dayLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = if (isNightMode) Color(0xCCFF453A) else Color.White, modifier = Modifier.width(50.dp))
                                com.hoandesign.standby.ui.components.WeatherVectorIcon(
                                    condition = day.conditionEmoji,
                                    size = 18.dp,
                                    isNightMode = isNightMode
                                )
                                Text("L: $dLow", fontSize = 12.sp, color = if (isNightMode) Color(0x88FF453A) else com.hoandesign.standby.ui.theme.TextTertiary)
                                Text("H: $dHigh", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isNightMode) com.hoandesign.standby.ui.theme.NightRed else Color.White)
                            }
                        }
                    }
                }
            }
        } else {
            // Landscape Layout: Side-by-side
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Current Weather
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (weather.cityName == "Location Needed") "LOCAL WEATHER" else weather.cityName.uppercase(),
                        color = if (isNightMode) com.hoandesign.standby.ui.theme.NightRed else com.hoandesign.standby.ui.theme.TextTertiary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.08.em
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val currentTempDisplay = if (weather.cityName == "Location Needed") "--°" else (if (isCelsius) "${weather.tempCelsius}°" else "${weather.tempFahrenheit}°")
                        Text(
                            text = currentTempDisplay,
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isNightMode) com.hoandesign.standby.ui.theme.NightRed else Color.White,
                            letterSpacing = (-0.03).em
                        )
                        com.hoandesign.standby.ui.components.WeatherVectorIcon(
                            condition = if (weather.cityName == "Location Needed") "Partly Cloudy" else weather.condition,
                            size = 54.dp,
                            isNightMode = isNightMode
                        )
                    }
                    val lHigh = if (isCelsius) "${((weather.highTemp - 32) * 5 / 9)}°" else "${weather.highTemp}°"
                    val lLow = if (isCelsius) "${((weather.lowTemp - 32) * 5 / 9)}°" else "${weather.lowTemp}°"
                    Text(
                        text = if (weather.condition == "Location Needed") "Tap to refresh" else "${weather.condition}  ·  H: $lHigh  L: $lLow",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isNightMode) Color(0xCCFF453A) else com.hoandesign.standby.ui.theme.TextSecondary
                    )
                }

                // Right Column: Hourly + 5-Day Forecast
                Column(
                    modifier = Modifier.weight(1.3f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Hourly
                    if (weather.hourlyForecast.isNotEmpty()) {
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(weather.hourlyForecast.size) { i ->
                                val hour = weather.hourlyForecast[i]
                                val hourDisplay = if (isCelsius) "${hour.tempCelsius}°" else "${hour.tempFahrenheit}°"
                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isNightMode) Color(0x18FF453A) else com.hoandesign.standby.ui.theme.StandbyCardBgSecondary)
                                        .border(1.dp, if (isNightMode) com.hoandesign.standby.ui.theme.NightRedDim else com.hoandesign.standby.ui.theme.StandbyBorder, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(hour.timeLabel, fontSize = 10.sp, color = if (isNightMode) Color(0x99FF453A) else com.hoandesign.standby.ui.theme.TextSecondary)
                                    com.hoandesign.standby.ui.components.WeatherVectorIcon(
                                        condition = hour.conditionEmoji,
                                        size = 18.dp,
                                        isNightMode = isNightMode
                                    )
                                    Text(hourDisplay, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isNightMode) com.hoandesign.standby.ui.theme.NightRed else Color.White)
                                }
                            }
                        }
                    }

                    // 5-Day
                    if (weather.dailyForecast.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isNightMode) Color(0x18FF453A) else com.hoandesign.standby.ui.theme.StandbyCardBgSecondary)
                                .border(1.dp, if (isNightMode) com.hoandesign.standby.ui.theme.NightRedDim else com.hoandesign.standby.ui.theme.StandbyBorder, RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            weather.dailyForecast.take(4).forEach { day ->
                                val dLow = if (isCelsius) "${day.lowTempCelsius}°" else "${day.lowTempFahrenheit}°"
                                val dHigh = if (isCelsius) "${day.highTempCelsius}°" else "${day.highTempFahrenheit}°"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(day.dayLabel, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = if (isNightMode) Color(0xCCFF453A) else Color.White, modifier = Modifier.width(45.dp))
                                    com.hoandesign.standby.ui.components.WeatherVectorIcon(
                                        condition = day.conditionEmoji,
                                        size = 18.dp,
                                        isNightMode = isNightMode
                                    )
                                    Text("L: $dLow", fontSize = 11.sp, color = if (isNightMode) Color(0x88FF453A) else com.hoandesign.standby.ui.theme.TextTertiary)
                                    Text("H: $dHigh", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isNightMode) com.hoandesign.standby.ui.theme.NightRed else Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen Month Calendar paired with upcoming agenda events.
 * Adapts between vertical stacked layout (Portrait) and side-by-side (Landscape).
 */
@Composable
fun FullscreenMonthCalendar(modifier: Modifier = Modifier) {
    val month = androidx.compose.runtime.remember { com.hoandesign.standby.model.CalendarMonth.now() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(com.hoandesign.standby.ui.theme.OledBlack)
            .padding(16.dp)
    ) {
        val isPortrait = maxHeight > maxWidth

        if (isPortrait) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(com.hoandesign.standby.ui.theme.StandbyCardBg)
                        .border(1.dp, com.hoandesign.standby.ui.theme.StandbyBorder, RoundedCornerShape(20.dp))
                ) {
                    MonthCalendarWidget(calendarMonth = month)
                }
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(com.hoandesign.standby.ui.theme.StandbyCardBg)
                        .border(1.dp, com.hoandesign.standby.ui.theme.StandbyBorder, RoundedCornerShape(20.dp))
                ) {
                    ScheduleWidget()
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(com.hoandesign.standby.ui.theme.StandbyCardBg)
                        .border(1.dp, com.hoandesign.standby.ui.theme.StandbyBorder, RoundedCornerShape(20.dp))
                ) {
                    MonthCalendarWidget(calendarMonth = month)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(com.hoandesign.standby.ui.theme.StandbyCardBg)
                        .border(1.dp, com.hoandesign.standby.ui.theme.StandbyBorder, RoundedCornerShape(20.dp))
                ) {
                    ScheduleWidget()
                }
            }
        }
    }
}

/**
 * Fullscreen Schedule showing upcoming meetings and timeline.
 */
@Composable
fun FullscreenSchedule(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(com.hoandesign.standby.ui.theme.OledBlack)
            .padding(16.dp)
    ) {
        ScheduleWidget(modifier = Modifier.fillMaxSize())
    }
}

/**
 * Fullscreen real hardware telemetry (RAM, Storage, CPU, Battery).
 */
@Composable
fun FullscreenSystemBento(accentColor: Color, modifier: Modifier = Modifier) {
    SystemBentoWidget(modifier = modifier.fillMaxSize().padding(16.dp))
}

/**
 * Interactive Fullscreen Desk Timer with circular progress arc and presets.
 */
@Composable
fun FullscreenDeskTimer(accentColor: Color, modifier: Modifier = Modifier) {
    DeskTimerWidget(accentColor = accentColor, modifier = modifier.fillMaxSize().padding(16.dp))
}

/**
 * Fullscreen Ambient Vibes and Soundscapes with soothing audio controls.
 */
@Composable
fun FullscreenVibes(accentColor: Color, modifier: Modifier = Modifier) {
    VibesWidget(modifier = modifier.fillMaxSize().padding(16.dp))
}
