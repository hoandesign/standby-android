package com.hoandesign.standby.ui.widgets.weather

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.hoandesign.standby.data.location.LocationHelper
import com.hoandesign.standby.data.WeatherRepository
import com.hoandesign.standby.model.WeatherState
import com.hoandesign.standby.ui.theme.AccentCyan
import com.hoandesign.standby.ui.theme.AccentGreen
import com.hoandesign.standby.ui.theme.NightRed
import com.hoandesign.standby.ui.theme.NightRedDim
import com.hoandesign.standby.ui.theme.OledBlack
import com.hoandesign.standby.ui.theme.StandbyBorder
import com.hoandesign.standby.ui.theme.StandbyCardBgSecondary
import com.hoandesign.standby.ui.theme.StandbyTheme
import com.hoandesign.standby.ui.theme.TextPrimary
import com.hoandesign.standby.ui.theme.TextSecondary
import com.hoandesign.standby.ui.theme.TextTertiary
import kotlinx.coroutines.launch

/**
 * Ambient Weather Widget presenting real-time meteorological conditions.
 *
 * Features:
 * - Bold primary temperature display with tap-to-toggle Celsius/Fahrenheit.
 * - Current condition emoji icon and human-readable status.
 * - Daily High / Low temperature range indicator.
 * - Air Quality Index (AQI) badge with color-coded safety rating.
 * - Precipitation chance meter with subtle rain progress bar.
 * - Full adaptation to deep monochromatic OLED Red Night Mode.
 *
 * @param modifier Root modifier.
 * @param repository Weather repository instance; defaults to remember singleton.
 * @param initialWeather Optional override for test or preview states.
 */
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import com.hoandesign.standby.util.PermissionHelper

@Composable
fun WeatherWidget(
    modifier: Modifier = Modifier,
    repository: WeatherRepository = remember { WeatherRepository() },
    initialWeather: WeatherState? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var hasLocationPerm by remember {
        mutableStateOf(PermissionHelper.isLocationGranted(context))
    }
    var currentLat by remember { mutableStateOf(37.7749) }
    var currentLon by remember { mutableStateOf(-122.4194) }
    var currentCity by remember { mutableStateOf(if (hasLocationPerm) "Local Weather" else "Location Needed") }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPerm = granted
        if (granted) {
            coroutineScope.launch {
                val fresh = LocationHelper.getFreshLocationAndCity(context)
                if (fresh.first != null) {
                    currentLat = fresh.first!!.latitude
                    currentLon = fresh.first!!.longitude
                }
                currentCity = fresh.second
                repository.fetchWeather(currentLat, currentLon, currentCity)
            }
        }
    }

    LaunchedEffect(hasLocationPerm) {
        if (hasLocationPerm) {
            val fresh = LocationHelper.getFreshLocationAndCity(context)
            if (fresh.first != null) {
                currentLat = fresh.first!!.latitude
                currentLon = fresh.first!!.longitude
            }
            currentCity = fresh.second
            repository.fetchWeather(currentLat, currentLon, currentCity)
        }
    }
    
    val weatherFlow = remember(repository, currentLat, currentLon, currentCity) { 
        repository.weatherFlow(currentLat, currentLon, currentCity) 
    }
    val observedState by weatherFlow.collectAsState(initial = repository.getCachedWeather())
    val weather = initialWeather ?: observedState

    var showCelsius by remember { mutableStateOf(false) }
    val isNightMode = StandbyTheme.isNightMode

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
            .clickable {
                if (!hasLocationPerm) {
                    locationLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                } else {
                    // Tap toggles temperature unit and triggers a background refresh
                    showCelsius = !showCelsius
                    coroutineScope.launch {
                        repository.fetchWeather(currentLat, currentLon, currentCity)
                    }
                }
            }
            .padding(14.dp)
    ) {
        val availableHeight = maxHeight

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: City Name & Condition
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = if (!hasLocationPerm) "📍 TAP TO ENABLE" else if (weather.cityName == "Location Needed") "LOCAL WEATHER" else weather.cityName.uppercase(),
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.08.em,
                            color = if (isNightMode) NightRed else if (!hasLocationPerm) com.hoandesign.standby.ui.theme.AccentOrange else TextTertiary
                        )
                    )
                    Text(
                        text = if (!hasLocationPerm) "Grant location permission" else if (weather.condition == "Location Needed") "Tap to refresh" else weather.condition,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = if (isNightMode) Color(0xCCFF453A) else TextSecondary
                        )
                    )
                }

                // Weather Icon Emoji
                Text(
                    text = if (!hasLocationPerm || weather.condition == "Location Needed") "⛅" else weather.iconEmoji,
                    fontSize = if (availableHeight < 140.dp) 24.sp else 32.sp
                )
            }

            // Hero Temperature & High/Low
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Prominent Temperature
                AnimatedContent(
                    targetState = if (!hasLocationPerm || weather.cityName == "Location Needed") "--°" else (if (showCelsius) "${weather.tempCelsius}°" else "${weather.tempFahrenheit}°"),
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "TempUnitTransition"
                ) { displayTemp ->
                    Text(
                        text = displayTemp,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (availableHeight < 140.dp) 44.sp else 58.sp,
                            letterSpacing = (-0.03).em,
                            color = if (isNightMode) NightRed else TextPrimary
                        )
                    )
                }

                // High / Low Range
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    val highVal = if (!hasLocationPerm || weather.cityName == "Location Needed") "--" else (if (showCelsius) ((weather.highTemp - 32) * 5 / 9).toString() else weather.highTemp.toString())
                    val lowVal = if (!hasLocationPerm || weather.cityName == "Location Needed") "--" else (if (showCelsius) ((weather.lowTemp - 32) * 5 / 9).toString() else weather.lowTemp.toString())

                    Text(
                        text = "H: $highVal°",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (isNightMode) Color(0xDDFF453A) else TextPrimary
                        )
                    )
                    Text(
                        text = "L: $lowVal°",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isNightMode) Color(0x88FF453A) else TextSecondary
                        )
                    )
                }
            }

            // Bottom Metrics: AQI Pill & Precipitation Meter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AQI Status Pill
                val aqiBg = if (isNightMode) Color(0x22FF453A) else StandbyCardBgSecondary
                val aqiBorder = if (isNightMode) Color(0x44FF453A) else StandbyBorder
                val aqiAccent = if (isNightMode) NightRed else AccentGreen

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(aqiBg)
                        .border(1.dp, aqiBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(aqiAccent)
                    )
                    Text(
                        text = if (!hasLocationPerm) "AQI --" else "AQI ${weather.aqi} · Good",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = if (isNightMode) NightRed else TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Precipitation Chance Meter
                val rainAccent = if (isNightMode) NightRed else AccentCyan
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isNightMode) Color(0x22FF453A) else StandbyCardBgSecondary)
                        .border(1.dp, if (isNightMode) Color(0x44FF453A) else StandbyBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "💧",
                        fontSize = 10.sp
                    )
                    Text(
                        text = if (!hasLocationPerm) "--%" else "${weather.precipitationChance}%",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = rainAccent
                        )
                    )
                    // Mini progress bar for precipitation
                    Box(
                        modifier = Modifier
                            .width(26.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isNightMode) NightRedDim else Color(0x33FFFFFF))
                    ) {
                        val fraction = (weather.precipitationChance / 100f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(rainAccent)
                        )
                    }
                }
            }
        }
    }
}
