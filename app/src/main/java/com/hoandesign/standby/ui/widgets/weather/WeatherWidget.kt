package com.hoandesign.standby.ui.widgets.weather

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.hoandesign.standby.data.WeatherRepository
import com.hoandesign.standby.data.location.LocationHelper
import com.hoandesign.standby.model.WeatherState
import com.hoandesign.standby.ui.components.WeatherVectorIcon
import com.hoandesign.standby.ui.theme.NightRed
import com.hoandesign.standby.ui.theme.OledBlack
import com.hoandesign.standby.ui.theme.StandbyTheme
import com.hoandesign.standby.ui.theme.TextPrimary
import com.hoandesign.standby.ui.theme.TextSecondary
import com.hoandesign.standby.util.PermissionHelper
import kotlinx.coroutines.launch

/**
 * Authentic Apple iOS StandBy Weather Widget.
 *
 * Implements the clean left-aligned typography hierarchy from the Apple StandBy reference:
 * 1. City Name (Title Case, Medium)
 * 2. Monumental Hero Temperature (Bold ~94sp)
 * 3. Official Material Design Weather Vector Icon
 * 4. Condition Name (Bold ~20sp)
 * 5. Daily Range (H:xx° L:xx°)
 *
 * Pure OLED black (#000000), borderless, zero cartoon emojis.
 */
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
    var currentLat by remember { mutableStateOf<Double?>(null) }
    var currentLon by remember { mutableStateOf<Double?>(null) }
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

    LaunchedEffect(hasLocationPerm) {
        if (hasLocationPerm) {
            val fresh = LocationHelper.getFreshLocationAndCity(context)
            val loc = fresh.first
            if (loc != null) {
                currentLat = loc.latitude
                currentLon = loc.longitude
                currentCity = fresh.second
                repository.fetchWeather(loc.latitude, loc.longitude, fresh.second)
            }
        }
    }

    val weatherFlow = remember(repository, currentLat, currentLon, currentCity) { 
        repository.weatherFlow(currentLat, currentLon, currentCity) 
    }
    val observedState by weatherFlow.collectAsState(initial = repository.getCachedWeather())
    val weather = initialWeather ?: observedState

    val prefs = remember(context) {
        context.getSharedPreferences(com.hoandesign.standby.receiver.ChargingReceiver.PREFS_NAME, android.content.Context.MODE_PRIVATE)
    }

    val tempPreference = StandbyTheme.temperatureUnit
    val showCelsius = tempPreference == com.hoandesign.standby.model.TemperatureUnit.CELSIUS
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
                    val nextUnit = if (showCelsius) com.hoandesign.standby.model.TemperatureUnit.FAHRENHEIT else com.hoandesign.standby.model.TemperatureUnit.CELSIUS
                    prefs.edit().putString("pref_temp_unit", if (nextUnit == com.hoandesign.standby.model.TemperatureUnit.FAHRENHEIT) "F" else "C").apply()
                }
            }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val h = maxHeight
        val cityFontSize = if (h < 150.dp) 18.sp else 26.sp
        val tempFontSize = if (h < 150.dp) 56.sp else 88.sp
        val conditionFontSize = if (h < 150.dp) 15.sp else 19.sp
        val rangeFontSize = if (h < 150.dp) 13.sp else 16.sp

        val highVal = if (!hasLocationPerm || weather.cityName == "Location Needed") (if (showCelsius) "32" else "91") else (if (showCelsius) ((weather.highTemp - 32) * 5 / 9).toString() else weather.highTemp.toString())
        val lowVal = if (!hasLocationPerm || weather.cityName == "Location Needed") (if (showCelsius) "22" else "62") else (if (showCelsius) ((weather.lowTemp - 32) * 5 / 9).toString() else weather.lowTemp.toString())

        val displayCity = if (!hasLocationPerm) "Location Access" else if (weather.cityName == "Location Needed") "Cupertino" else weather.cityName
        val displayTemp = if (!hasLocationPerm || weather.cityName == "Location Needed") (if (showCelsius) "24°" else "63°") else (if (showCelsius) "${weather.tempCelsius}°" else "${weather.tempFahrenheit}°")
        val displayCondition = if (!hasLocationPerm || weather.condition == "Location Needed") "Sunny" else weather.condition

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. City Name (Title Case, Medium)
            Text(
                text = displayCity,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Medium,
                    fontSize = cityFontSize,
                    letterSpacing = (-0.02).em,
                    color = if (isNightMode) NightRed else TextPrimary
                )
            )

            // 2. Monumental Hero Temperature
            AnimatedContent(
                targetState = displayTemp,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TempUnitTransition"
            ) { temp ->
                Text(
                    text = temp,
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize = tempFontSize,
                        letterSpacing = (-0.04).em,
                        color = if (isNightMode) NightRed else TextPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 3. Official Material Design Weather Vector Icon
            WeatherVectorIcon(
                condition = displayCondition,
                size = if (h < 150.dp) 20.dp else 24.dp,
                isNightMode = isNightMode
            )

            Spacer(modifier = Modifier.height(3.dp))

            // 4. Condition Name (Bold)
            Text(
                text = displayCondition,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = conditionFontSize,
                    color = if (isNightMode) Color(0xEEFF453A) else TextPrimary
                )
            )

            // 5. Daily Range (e.g. H:91° L:62°)
            Text(
                text = "H:$highVal° L:$lowVal°",
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Normal,
                    fontSize = rangeFontSize,
                    letterSpacing = 0.02.em,
                    color = if (isNightMode) Color(0x99FF453A) else TextSecondary
                )
            )
        }
    }
}
