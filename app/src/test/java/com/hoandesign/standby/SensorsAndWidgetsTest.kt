package com.hoandesign.standby

import android.os.BatteryManager
import com.hoandesign.standby.data.calculateBatteryState
import com.hoandesign.standby.data.parseBatteryIntent
import com.hoandesign.standby.model.BatteryState
import com.hoandesign.standby.model.DEFAULT_MEDIA_TRACK
import com.hoandesign.standby.model.MediaTrack
import com.hoandesign.standby.model.WeatherState
import com.hoandesign.standby.model.celsiusToFahrenheit
import com.hoandesign.standby.model.fahrenheitToCelsius
import com.hoandesign.standby.model.formatMediaDuration
import com.hoandesign.standby.model.mapWmoCodeToCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying sensor models, repositories, and widget calculations:
 * 1. WMO weather code to condition and emoji mappings (clear, cloudy, rain, snow, thunder).
 * 2. WeatherState temperature conversions (Celsius <-> Fahrenheit).
 * 3. Battery status and intent parsing logic (charging speeds, percentages, time-to-full).
 * 4. MediaTrack progress fractions, percentages, and duration formatters.
 */
class SensorsAndWidgetsTest {

    // ---------------------------------------------------------------------------------------------
    // 1. WMO Weather Code Mapping Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testWmoWeatherCodeMapping_clearConditions() {
        val clearSky = mapWmoCodeToCondition(0)
        assertEquals("Clear Sky", clearSky.first)
        assertEquals("☀️", clearSky.second)

        val mainlyClear = mapWmoCodeToCondition(1)
        assertEquals("Mainly Clear", mainlyClear.first)
        assertEquals("🌤️", mainlyClear.second)
    }

    @Test
    fun testWmoWeatherCodeMapping_cloudyAndOvercastConditions() {
        val partlyCloudy = mapWmoCodeToCondition(2)
        assertEquals("Partly Cloudy", partlyCloudy.first)
        assertEquals("⛅", partlyCloudy.second)

        val overcast = mapWmoCodeToCondition(3)
        assertEquals("Overcast", overcast.first)
        assertEquals("☁️", overcast.second)
    }

    @Test
    fun testWmoWeatherCodeMapping_rainConditions() {
        val lightRain = mapWmoCodeToCondition(61)
        assertEquals("Light Rain", lightRain.first)
        assertEquals("🌧️", lightRain.second)

        val moderateRain = mapWmoCodeToCondition(63)
        assertEquals("Moderate Rain", moderateRain.first)
        assertEquals("🌧️", moderateRain.second)

        val heavyRain = mapWmoCodeToCondition(65)
        assertEquals("Heavy Rain", heavyRain.first)
        assertEquals("🌧️", heavyRain.second)

        val rainShowers = mapWmoCodeToCondition(80)
        assertEquals("Rain Showers", rainShowers.first)
        assertEquals("🌦️", rainShowers.second)
    }

    @Test
    fun testWmoWeatherCodeMapping_snowConditions() {
        val lightSnow = mapWmoCodeToCondition(71)
        assertEquals("Light Snow", lightSnow.first)
        assertEquals("🌨️", lightSnow.second)

        val moderateSnow = mapWmoCodeToCondition(73)
        assertEquals("Moderate Snow", moderateSnow.first)
        assertEquals("🌨️", moderateSnow.second)

        val heavySnow = mapWmoCodeToCondition(75)
        assertEquals("Heavy Snow", heavySnow.first)
        assertEquals("🌨️", heavySnow.second)

        val snowShowers = mapWmoCodeToCondition(85)
        assertEquals("Snow Showers", snowShowers.first)
        assertEquals("🌨️", snowShowers.second)
    }

    @Test
    fun testWmoWeatherCodeMapping_thunderstormConditions() {
        val thunderstorm = mapWmoCodeToCondition(95)
        assertEquals("Thunderstorm", thunderstorm.first)
        assertEquals("⛈️", thunderstorm.second)

        val severeThunder = mapWmoCodeToCondition(96)
        assertEquals("Thunderstorm with Hail", severeThunder.first)
        assertEquals("⛈️", severeThunder.second)

        val extremeThunder = mapWmoCodeToCondition(99)
        assertEquals("Thunderstorm with Hail", extremeThunder.first)
        assertEquals("⛈️", extremeThunder.second)
    }

    @Test
    fun testWmoWeatherCodeMapping_fogDrizzleAndFallbacks() {
        val fog = mapWmoCodeToCondition(45)
        assertEquals("Foggy", fog.first)
        assertEquals("🌫️", fog.second)

        val drizzle = mapWmoCodeToCondition(51)
        assertEquals("Drizzle", drizzle.first)
        assertEquals("🌦️", drizzle.second)

        // Unknown code fallback
        val unknown = mapWmoCodeToCondition(999)
        assertEquals("Clear Sky", unknown.first)
        assertEquals("☀️", unknown.second)
    }

    // ---------------------------------------------------------------------------------------------
    // 2. WeatherState Temperature Calculation Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testCelsiusToFahrenheit_cardinalPoints() {
        assertEquals(32, celsiusToFahrenheit(0))
        assertEquals(212, celsiusToFahrenheit(100))
        assertEquals(-40, celsiusToFahrenheit(-40))
        assertEquals(68, celsiusToFahrenheit(20))
        assertEquals(75, celsiusToFahrenheit(24))
        assertEquals(82, celsiusToFahrenheit(28))
    }

    @Test
    fun testFahrenheitToCelsius_cardinalPoints() {
        assertEquals(0, fahrenheitToCelsius(32))
        assertEquals(100, fahrenheitToCelsius(212))
        assertEquals(-40, fahrenheitToCelsius(-40))
        assertEquals(20, fahrenheitToCelsius(68))
        assertEquals(24, fahrenheitToCelsius(75))
        assertEquals(28, fahrenheitToCelsius(82))
    }

    @Test
    fun testWeatherState_defaultsAndProperties() {
        val defaultWeather = WeatherState()
        assertEquals(0, defaultWeather.tempCelsius)
        assertEquals(0, defaultWeather.tempFahrenheit)
        assertEquals("Location Needed", defaultWeather.condition)
        assertEquals("location", defaultWeather.iconEmoji)
        assertEquals(0, defaultWeather.highTemp)
        assertEquals(0, defaultWeather.lowTemp)
        assertEquals(0, defaultWeather.aqi)
        assertEquals(0, defaultWeather.precipitationChance)
        assertEquals("Location Needed", defaultWeather.cityName)

        val liveWeather = WeatherState(
            tempCelsius = 26,
            tempFahrenheit = 78,
            condition = "Sunny",
            iconEmoji = "clear",
            highTemp = 85,
            lowTemp = 72,
            aqi = 35,
            precipitationChance = 5,
            cityName = "Tokyo"
        )
        assertEquals(26, liveWeather.tempCelsius)
        assertEquals("Tokyo", liveWeather.cityName)
    }

    // ---------------------------------------------------------------------------------------------
    // 3. Battery Intent and Parsing Logic Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testBatteryIntent_nullIntentReturnsDefault() {
        val state = parseBatteryIntent(null)
        assertEquals(85, state.percentage)
        assertTrue(state.isCharging)
        assertEquals("Fast Charging", state.chargeSpeed)
        assertEquals(4895, state.capacityMah)
        assertEquals(22, state.timeToFullMinutes)
    }

    @Test
    fun testBatteryCalculation_fastCharging85Percent() {
        val state = calculateBatteryState(
            level = 85,
            scale = 100,
            status = BatteryManager.BATTERY_STATUS_CHARGING,
            plugged = BatteryManager.BATTERY_PLUGGED_AC
        )

        assertEquals(85, state.percentage)
        assertTrue(state.isCharging)
        assertEquals("Fast Charging", state.chargeSpeed)
        assertEquals(4895, state.capacityMah)
        assertEquals(22, state.timeToFullMinutes)
    }

    @Test
    fun testBatteryCalculation_wirelessQiCharging() {
        val state = calculateBatteryState(
            level = 50,
            scale = 100,
            status = BatteryManager.BATTERY_STATUS_CHARGING,
            plugged = BatteryManager.BATTERY_PLUGGED_WIRELESS
        )

        assertEquals(50, state.percentage)
        assertTrue(state.isCharging)
        assertEquals("Wireless Qi", state.chargeSpeed)
        // (100 - 50) * 1.47 = 73.5 -> 74 minutes
        assertEquals(74, state.timeToFullMinutes)
    }

    @Test
    fun testBatteryCalculation_usbCharging() {
        val state = calculateBatteryState(
            level = 30,
            scale = 100,
            status = BatteryManager.BATTERY_STATUS_CHARGING,
            plugged = BatteryManager.BATTERY_PLUGGED_USB
        )

        assertEquals(30, state.percentage)
        assertTrue(state.isCharging)
        assertEquals("USB Charging", state.chargeSpeed)
    }

    @Test
    fun testBatteryCalculation_fullyCharged() {
        val state = calculateBatteryState(
            level = 100,
            scale = 100,
            status = BatteryManager.BATTERY_STATUS_FULL,
            plugged = BatteryManager.BATTERY_PLUGGED_AC
        )

        assertEquals(100, state.percentage)
        assertTrue(state.isCharging)
        assertEquals(0, state.timeToFullMinutes)
    }

    @Test
    fun testBatteryCalculation_dischargingOnBattery() {
        val state = calculateBatteryState(
            level = 65,
            scale = 100,
            status = BatteryManager.BATTERY_STATUS_DISCHARGING,
            plugged = 0
        )

        assertEquals(65, state.percentage)
        assertFalse(state.isCharging)
        assertEquals("On Battery", state.chargeSpeed)
        assertNull(state.timeToFullMinutes)
    }

    // ---------------------------------------------------------------------------------------------
    // 4. MediaTrack Progress and Duration Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testMediaTrack_progressPercentageCalculation() {
        val defaultTrack = DEFAULT_MEDIA_TRACK
        assertEquals("Not Playing", defaultTrack.title)
        assertFalse(defaultTrack.isPlaying)

        val track = MediaTrack(
            title = "Midnight City",
            artist = "M83",
            album = "Hurry Up, We're Dreaming",
            durationMs = 244_000L,
            positionMs = 88_000L,
            isPlaying = true
        )
        assertEquals("Midnight City", track.title)
        assertEquals("M83", track.artist)
        assertEquals("Hurry Up, We're Dreaming", track.album)
        assertEquals(244_000L, track.durationMs)
        assertEquals(88_000L, track.positionMs)
        assertTrue(track.isPlaying)

        // 88000 / 244000 = 0.36065574
        assertEquals(0.36065573f, track.progressFraction, 0.001f)
        assertEquals(36.065575f, track.progressPercentage, 0.01f)
    }

    @Test
    fun testMediaTrack_progressBoundaryConditions() {
        // Position at beginning
        val startTrack = MediaTrack(durationMs = 200_000L, positionMs = 0L)
        assertEquals(0.0f, startTrack.progressFraction, 0.0001f)
        assertEquals(0.0f, startTrack.progressPercentage, 0.0001f)

        // Position at exact end
        val endTrack = MediaTrack(durationMs = 200_000L, positionMs = 200_000L)
        assertEquals(1.0f, endTrack.progressFraction, 0.0001f)
        assertEquals(100.0f, endTrack.progressPercentage, 0.0001f)

        // Position exceeds duration (clamped)
        val clampedTrack = MediaTrack(durationMs = 200_000L, positionMs = 300_000L)
        assertEquals(1.0f, clampedTrack.progressFraction, 0.0001f)
        assertEquals(100.0f, clampedTrack.progressPercentage, 0.0001f)

        // Zero duration edge case
        val zeroTrack = MediaTrack(durationMs = 0L, positionMs = 0L)
        assertEquals(0.0f, zeroTrack.progressFraction, 0.0001f)
        assertEquals(0.0f, zeroTrack.progressPercentage, 0.0001f)
    }

    @Test
    fun testFormatMediaDuration() {
        assertEquals("1:28", formatMediaDuration(88_000L))
        assertEquals("4:04", formatMediaDuration(244_000L))
        assertEquals("0:00", formatMediaDuration(0L))
        assertEquals("0:05", formatMediaDuration(5_000L))
        assertEquals("3:32", formatMediaDuration(212_000L))
        assertEquals("10:00", formatMediaDuration(600_000L))
    }
}
