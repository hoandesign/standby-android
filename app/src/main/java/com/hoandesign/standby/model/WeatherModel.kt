package com.hoandesign.standby.model

import kotlin.math.roundToInt

/**
 * Temperature display preference.
 */
enum class TemperatureUnit(val symbol: String, val label: String) {
    CELSIUS("°C", "Celsius"),
    FAHRENHEIT("°F", "Fahrenheit")
}

/**
 * Immutable state representing current and daily weather metrics.
 *
 * @param tempCelsius Current temperature in degrees Celsius.
 * @param tempFahrenheit Current temperature in degrees Fahrenheit.
 * @param condition Descriptive weather condition (e.g. "Partly Cloudy", "Clear Sky").
 * @param iconEmoji Visual emoji representation for the condition (e.g. "⛅", "☀️").
 * @param highTemp Day's forecasted high temperature.
 * @param lowTemp Day's forecasted low temperature.
 * @param aqi Air Quality Index reading (e.g. 28).
 * @param precipitationChance Precipitation percentage probability (0 - 100).
 * @param cityName Name of the observed city or location.
 */
data class HourlyForecast(
    val timeLabel: String,
    val tempCelsius: Int,
    val tempFahrenheit: Int,
    val conditionEmoji: String
)

data class DailyForecast(
    val dayLabel: String,
    val highTempCelsius: Int,
    val lowTempCelsius: Int,
    val highTempFahrenheit: Int,
    val lowTempFahrenheit: Int,
    val conditionEmoji: String
)

data class WeatherState(
    val tempCelsius: Int = 0,
    val tempFahrenheit: Int = 0,
    val condition: String = "Location Needed",
    val iconEmoji: String = "location",
    val highTemp: Int = 0,
    val lowTemp: Int = 0,
    val aqi: Int = 0,
    val precipitationChance: Int = 0,
    val cityName: String = "Location Needed",
    val hourlyForecast: List<HourlyForecast> = emptyList(),
    val dailyForecast: List<DailyForecast> = emptyList()
)

/**
 * Pure conversion function from Celsius to Fahrenheit rounded to the nearest integer.
 */
fun celsiusToFahrenheit(celsius: Int): Int {
    return (celsius * 9.0 / 5.0 + 32.0).roundToInt()
}

/**
 * Pure conversion function from Fahrenheit to Celsius rounded to the nearest integer.
 */
fun fahrenheitToCelsius(fahrenheit: Int): Int {
    return ((fahrenheit - 32.0) * 5.0 / 9.0).roundToInt()
}

/**
 * Maps WMO (World Meteorological Organization) weather interpretation codes
 * to a human-readable condition string and corresponding icon emoji.
 *
 * WMO Code Standard:
 * 0: Clear Sky
 * 1, 2, 3: Mainly Clear, Partly Cloudy, Overcast
 * 45, 48: Fog
 * 51 - 55: Drizzle
 * 61 - 65: Rain (Light, Moderate, Heavy)
 * 71 - 77: Snowfall and Grains
 * 80 - 82: Rain Showers
 * 85 - 86: Snow Showers
 * 95 - 99: Thunderstorm
 */
fun mapWmoCodeToCondition(wmoCode: Int): Pair<String, String> {
    return when (wmoCode) {
        0 -> Pair("Clear Sky", "☀️")
        1 -> Pair("Mainly Clear", "🌤️")
        2 -> Pair("Partly Cloudy", "⛅")
        3 -> Pair("Overcast", "☁️")
        45, 48 -> Pair("Foggy", "🌫️")
        51, 53, 55 -> Pair("Drizzle", "🌦️")
        56, 57 -> Pair("Freezing Drizzle", "🌧️")
        61 -> Pair("Light Rain", "🌧️")
        63 -> Pair("Moderate Rain", "🌧️")
        65 -> Pair("Heavy Rain", "🌧️")
        66, 67 -> Pair("Freezing Rain", "🌧️")
        71 -> Pair("Light Snow", "🌨️")
        73 -> Pair("Moderate Snow", "🌨️")
        75 -> Pair("Heavy Snow", "🌨️")
        77 -> Pair("Snow Grains", "🌨️")
        80, 81, 82 -> Pair("Rain Showers", "🌦️")
        85, 86 -> Pair("Snow Showers", "🌨️")
        95 -> Pair("Thunderstorm", "⛈️")
        96, 99 -> Pair("Thunderstorm with Hail", "⛈️")
        else -> Pair("Clear Sky", "☀️")
    }
}
