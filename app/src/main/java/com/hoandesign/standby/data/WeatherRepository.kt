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
        val DEFAULT_WEATHER = WeatherState(isLive = false)
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
        latitude: Double,
        longitude: Double,
        cityName: String = "Local Weather"
    ): WeatherState = withContext(Dispatchers.IO) {
        val endpoint = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=$latitude&longitude=$longitude" +
                "&current=temperature_2m,weather_code,precipitation_probability" +
                "&hourly=temperature_2m,weather_code" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
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
                    val dailyCodeArray = daily?.optJSONArray("weather_code")
                    val dailyTimeArray = daily?.optJSONArray("time")

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

                    // Parse hourly forecast (next 12 hours)
                    val hourlyList = mutableListOf<com.hoandesign.standby.model.HourlyForecast>()
                    val hourly = json.optJSONObject("hourly")
                    if (hourly != null) {
                        val times = hourly.optJSONArray("time")
                        val temps = hourly.optJSONArray("temperature_2m")
                        val codes = hourly.optJSONArray("weather_code")
                        val limit = minOf(times?.length() ?: 0, temps?.length() ?: 0, codes?.length() ?: 0, 16)
                        for (i in 0 until limit) {
                            val timeStr = times?.optString(i) ?: ""
                            val hourPart = timeStr.substringAfter("T", "").take(5)
                            val tC = temps?.optDouble(i, 20.0)?.roundToInt() ?: 20
                            val cWmo = codes?.optInt(i, 1) ?: 1
                            val (cCond, _) = mapWmoCodeToCondition(cWmo)
                            hourlyList.add(
                                com.hoandesign.standby.model.HourlyForecast(
                                    timeLabel = if (hourPart.isNotBlank()) hourPart else "${i}:00",
                                    tempCelsius = tC,
                                    tempFahrenheit = celsiusToFahrenheit(tC),
                                    conditionEmoji = cCond
                                )
                            )
                        }
                    }

                    // Parse daily forecast (next 5 days)
                    val dailyList = mutableListOf<com.hoandesign.standby.model.DailyForecast>()
                    if (daily != null && dailyTimeArray != null) {
                        val daysCount = minOf(dailyTimeArray.length(), 7)
                        for (d in 0 until daysCount) {
                            val rawDate = dailyTimeArray.optString(d, "")
                            val dayLabel = try {
                                val parsedDate = java.time.LocalDate.parse(rawDate)
                                parsedDate.dayOfWeek.name.take(3)
                            } catch (_: Exception) {
                                "DAY $d"
                            }
                            val dayHighC = highMaxArray?.optDouble(d, (tempC + 4).toDouble())?.roundToInt() ?: (tempC + 4)
                            val dayLowC = lowMinArray?.optDouble(d, (tempC - 4).toDouble())?.roundToInt() ?: (tempC - 4)
                            val dayWmo = dailyCodeArray?.optInt(d, 1) ?: 1
                            val (dayCond, _) = mapWmoCodeToCondition(dayWmo)

                            dailyList.add(
                                com.hoandesign.standby.model.DailyForecast(
                                    dayLabel = dayLabel,
                                    highTempCelsius = dayHighC,
                                    lowTempCelsius = dayLowC,
                                    highTempFahrenheit = celsiusToFahrenheit(dayHighC),
                                    lowTempFahrenheit = celsiusToFahrenheit(dayLowC),
                                    conditionEmoji = dayCond
                                )
                            )
                        }
                    }

                    val parsedState = WeatherState(
                        tempCelsius = tempC,
                        tempFahrenheit = celsiusToFahrenheit(tempC),
                        condition = condition,
                        iconEmoji = iconEmoji,
                        highTemp = celsiusToFahrenheit(highC),
                        lowTemp = celsiusToFahrenheit(lowC),
                        aqi = cachedWeather.aqi,
                        precipitationChance = precipitation,
                        cityName = cityName,
                        isLive = true,
                        hourlyForecast = hourlyList,
                        dailyForecast = dailyList
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
     * Flow that emits the cached weather immediately, followed by periodic live updates
     * if valid GPS coordinates are provided.
     */
    fun weatherFlow(
        latitude: Double?,
        longitude: Double?,
        cityName: String?,
        refreshIntervalMs: Long = 900_000L // 15 minutes
    ): Flow<WeatherState> = flow {
        emit(cachedWeather)
        if (latitude != null && longitude != null) {
            while (true) {
                val updated = fetchWeather(latitude, longitude, cityName ?: "Local Weather")
                emit(updated)
                delay(refreshIntervalMs)
            }
        }
    }
}
