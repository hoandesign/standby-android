package com.hoandesign.standby.data

import com.hoandesign.standby.model.WeatherState
import com.hoandesign.standby.model.celsiusToFahrenheit
import com.hoandesign.standby.model.mapWmoCodeToCondition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

/**
 * Repository for acquiring live weather data via Open-Meteo REST API
 * with instant, seamless offline caching.
 */
class WeatherRepository {

    companion object {
        val DEFAULT_WEATHER = WeatherState(
            tempCelsius = 24,
            tempFahrenheit = 76,
            condition = "Partly Cloudy",
            iconEmoji = "⛅",
            highTemp = 82,
            lowTemp = 68,
            aqi = 28,
            precipitationChance = 10,
            cityName = "Cupertino"
        )
    }

    @Volatile
    private var cachedWeather: WeatherState = DEFAULT_WEATHER

    /**
     * Returns the currently cached weather data synchronously.
     */
    fun getCachedWeather(): WeatherState = cachedWeather

    /**
     * Updates the local in-memory cache directly.
     */
    fun setCachedWeather(state: WeatherState) {
        cachedWeather = state
    }

    /**
     * Fetches real-time weather metrics from Open-Meteo REST API.
     * On network timeout, offline connectivity, or parse failure,
     * seamlessly falls back to the cached default without throwing.
     */
    suspend fun fetchWeather(
        latitude: Double = 37.7749,
        longitude: Double = -122.4194,
        cityName: String = "Cupertino"
    ): WeatherState = withContext(Dispatchers.IO) {
        val endpoint = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=$latitude&longitude=$longitude" +
                "&current=temperature_2m,weather_code,precipitation_probability" +
                "&daily=temperature_2m_max,temperature_2m_min" +
                "&timezone=auto"

        var connection: HttpURLConnection? = null
        try {
            val url = URL(endpoint)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.use { it.readText() }
                val json = JSONObject(response)

                val current = json.optJSONObject("current")
                if (current != null) {
                    val tempC = current.optDouble("temperature_2m", cachedWeather.tempCelsius.toDouble()).roundToInt()
                    val wmoCode = current.optInt("weather_code", 2)
                    val (condition, iconEmoji) = mapWmoCodeToCondition(wmoCode)
                    val precipitation = current.optInt("precipitation_probability", 10)

                    val daily = json.optJSONObject("daily")
                    val highMaxArray = daily?.optJSONArray("temperature_2m_max")
                    val lowMinArray = daily?.optJSONArray("temperature_2m_min")

                    val highC = if (highMaxArray != null && highMaxArray.length() > 0) {
                        highMaxArray.optDouble(0, (tempC + 4).toDouble()).roundToInt()
                    } else {
                        tempC + 4
                    }

                    val lowC = if (lowMinArray != null && lowMinArray.length() > 0) {
                        lowMinArray.optDouble(0, (tempC - 4).toDouble()).roundToInt()
                    } else {
                        tempC - 4
                    }

                    val parsedState = WeatherState(
                        tempCelsius = tempC,
                        tempFahrenheit = celsiusToFahrenheit(tempC),
                        condition = condition,
                        iconEmoji = iconEmoji,
                        highTemp = celsiusToFahrenheit(highC),
                        lowTemp = celsiusToFahrenheit(lowC),
                        aqi = cachedWeather.aqi, // AQI standard cached fallback
                        precipitationChance = precipitation,
                        cityName = cityName
                    )

                    cachedWeather = parsedState
                    return@withContext parsedState
                }
            }
        } catch (_: Throwable) {
            // Graceful fallback on network loss or timeout
        } finally {
            connection?.disconnect()
        }

        return@withContext cachedWeather
    }

    /**
     * Flow that emits the cached weather immediately, followed by periodic live updates.
     */
    fun weatherFlow(
        latitude: Double = 37.7749,
        longitude: Double = -122.4194,
        cityName: String = "Cupertino",
        refreshIntervalMs: Long = 900_000L // 15 minutes
    ): Flow<WeatherState> = flow {
        emit(cachedWeather)
        while (true) {
            val updated = fetchWeather(latitude, longitude, cityName)
            emit(updated)
            delay(refreshIntervalMs)
        }
    }
}
