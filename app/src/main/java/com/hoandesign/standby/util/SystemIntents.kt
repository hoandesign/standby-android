package com.hoandesign.standby.util

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.Settings
import android.util.Log

/**
 * High-level intent specification decoupled from the Android runtime for
 * deterministic unit testing and crash-proof dispatch.
 */
data class SystemIntentSpec(
    val action: String,
    val dataUri: String? = null,
    val packageName: String? = null,
    val className: String? = null,
    val category: String? = null
) {
    /**
     * Converts this specification into an executable Android [Intent].
     */
    fun toIntent(): Intent {
        val intent = Intent(action)
        if (dataUri != null) {
            intent.data = Uri.parse(dataUri)
        }
        if (packageName != null && className != null) {
            intent.setClassName(packageName, className)
        } else if (packageName != null) {
            intent.`package` = packageName
        }
        if (category != null) {
            intent.addCategory(category)
        }
        return intent
    }
}

/**
 * Crash-proof deep-link intent dispatcher for StandBy complications and widgets.
 *
 * Guaranteed Safety Properties:
 * 1. Automatic [Intent.FLAG_ACTIVITY_NEW_TASK] injection when launched from non-Activity contexts
 *    (e.g., DreamService, application context).
 * 2. Multi-tier fallback chains for every system destination (e.g. Alarm -> OEM Clocks -> Date Settings).
 * 3. Complete exception trapping (`ActivityNotFoundException`, `SecurityException`, etc.) preventing
 *    ambient display crashes under Direct Boot, tablet configurations, or custom ROMs.
 * 4. Deterministic [SystemIntentSpec] builders for unit testing on any JVM target.
 */
object SystemIntents {

    private const val TAG = "SystemIntents"

    // Common OEM Clock Application Package IDs
    val CLOCK_PACKAGES = listOf(
        "com.google.android.deskclock",
        "com.sec.android.app.clockpackage",
        "com.android.deskclock",
        "com.coloros.alarmclock",
        "com.oppo.alarmclock"
    )

    // Common OEM Calendar Application Package IDs
    val CALENDAR_PACKAGES = listOf(
        "com.google.android.calendar",
        "com.samsung.android.calendar",
        "com.android.calendar"
    )

    // Common OEM Weather Application Package IDs
    val WEATHER_PACKAGES = listOf(
        "com.sec.android.daemonapp",
        "com.miui.weather2",
        "com.coloros.weather2",
        "com.motorola.timeweatherwidget"
    )

    // ---------------------------------------------------------------------------------------------
    // Core Safe Launch Mechanisms
    // ---------------------------------------------------------------------------------------------

    /**
     * Safely executes [Context.startActivity], ensuring [Intent.FLAG_ACTIVITY_NEW_TASK] is present
     * if the calling context is not an [Activity], and suppressing any [Exception] (such as
     * [android.content.ActivityNotFoundException] or [SecurityException]).
     *
     * @return `true` if the activity started successfully; `false` otherwise.
     */
    fun safeStartActivity(context: Context, intent: Intent): Boolean {
        return try {
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.w(TAG, "safeStartActivity failed for intent action: ${intent.action}", e)
            false
        }
    }

    /**
     * Attempts to launch an activity using the provided [SystemIntentSpec].
     */
    fun safeStartSpec(context: Context, spec: SystemIntentSpec): Boolean {
        return safeStartActivity(context, spec.toIntent())
    }

    /**
     * Iterates through a prioritized list of [SystemIntentSpec]s, attempting to launch each one until
     * the first successful dispatch.
     *
     * @return `true` if any intent succeeded; `false` if all candidates failed.
     */
    fun safeStartAny(context: Context, specs: List<SystemIntentSpec>): Boolean {
        for (spec in specs) {
            if (safeStartSpec(context, spec)) {
                return true
            }
        }
        return false
    }

    // ---------------------------------------------------------------------------------------------
    // 1. Battery Settings
    // ---------------------------------------------------------------------------------------------

    fun buildBatterySettingsSpecs(): List<SystemIntentSpec> {
        return listOf(
            SystemIntentSpec(action = Intent.ACTION_POWER_USAGE_SUMMARY),
            SystemIntentSpec(action = Settings.ACTION_BATTERY_SAVER_SETTINGS),
            SystemIntentSpec(action = Settings.ACTION_SETTINGS)
        )
    }

    /**
     * Deep-links the user to the native Android Battery Usage or Power Saver settings.
     */
    fun launchBatterySettings(context: Context): Boolean {
        return safeStartAny(context, buildBatterySettingsSpecs())
    }

    // ---------------------------------------------------------------------------------------------
    // 2. Alarm Clock & Timer
    // ---------------------------------------------------------------------------------------------

    fun buildAlarmClockSpecs(): List<SystemIntentSpec> {
        val candidates = mutableListOf(
            SystemIntentSpec(action = AlarmClock.ACTION_SHOW_ALARMS),
            SystemIntentSpec(action = AlarmClock.ACTION_SET_ALARM)
        )

        // Add OEM Clock package launch intents
        for (pkg in CLOCK_PACKAGES) {
            candidates.add(
                SystemIntentSpec(
                    action = Intent.ACTION_MAIN,
                    packageName = pkg,
                    category = Intent.CATEGORY_LAUNCHER
                )
            )
        }

        // Final safe fallback: Date & Time Settings
        candidates.add(SystemIntentSpec(action = Settings.ACTION_DATE_SETTINGS))
        return candidates
    }

    /**
     * Deep-links the user to the native system Alarm Manager, OEM Clock app, or Date Settings.
     */
    fun launchAlarmClock(context: Context): Boolean {
        return safeStartAny(context, buildAlarmClockSpecs())
    }

    fun buildTimerSpecs(): List<SystemIntentSpec> {
        return listOf(
            SystemIntentSpec(action = AlarmClock.ACTION_SET_TIMER),
            SystemIntentSpec(action = AlarmClock.ACTION_SHOW_ALARMS),
            SystemIntentSpec(action = Settings.ACTION_DATE_SETTINGS)
        )
    }

    /**
     * Deep-links the user to the native Clock Timer screen.
     */
    fun launchTimer(context: Context): Boolean {
        return safeStartAny(context, buildTimerSpecs())
    }

    // ---------------------------------------------------------------------------------------------
    // 3. Calendar & Agenda
    // ---------------------------------------------------------------------------------------------

    fun buildSystemCalendarSpecs(epochMillis: Long? = null): List<SystemIntentSpec> {
        val candidates = mutableListOf<SystemIntentSpec>()

        if (epochMillis != null && epochMillis > 0L) {
            // Target specific timestamp in system calendar
            candidates.add(
                SystemIntentSpec(
                    action = Intent.ACTION_VIEW,
                    dataUri = "content://com.android.calendar/time/$epochMillis"
                )
            )
        }

        // Generic calendar launch intents
        candidates.add(
            SystemIntentSpec(
                action = Intent.ACTION_MAIN,
                category = Intent.CATEGORY_APP_CALENDAR
            )
        )
        candidates.add(
            SystemIntentSpec(
                action = Intent.ACTION_VIEW,
                dataUri = "content://com.android.calendar/time"
            )
        )

        for (pkg in CALENDAR_PACKAGES) {
            candidates.add(
                SystemIntentSpec(
                    action = Intent.ACTION_MAIN,
                    packageName = pkg,
                    category = Intent.CATEGORY_LAUNCHER
                )
            )
        }

        candidates.add(SystemIntentSpec(action = Settings.ACTION_DATE_SETTINGS))
        return candidates
    }

    /**
     * Deep-links the user to the native Calendar app, optionally positioned at a specific epoch time.
     */
    fun launchSystemCalendar(context: Context, epochMillis: Long? = null): Boolean {
        return safeStartAny(context, buildSystemCalendarSpecs(epochMillis))
    }

    fun buildCalendarEventSpecs(eventId: Long): List<SystemIntentSpec> {
        val candidates = mutableListOf<SystemIntentSpec>()
        if (eventId > 0L) {
            candidates.add(
                SystemIntentSpec(
                    action = Intent.ACTION_VIEW,
                    dataUri = "content://com.android.calendar/events/$eventId"
                )
            )
            candidates.add(
                SystemIntentSpec(
                    action = Intent.ACTION_VIEW,
                    dataUri = "content://com.android.calendar/time"
                )
            )
        }
        candidates.addAll(buildSystemCalendarSpecs())
        return candidates
    }

    /**
     * Deep-links the user directly to a specific Calendar Event's detail screen.
     */
    fun launchCalendarEvent(context: Context, eventId: Long): Boolean {
        return safeStartAny(context, buildCalendarEventSpecs(eventId))
    }

    // ---------------------------------------------------------------------------------------------
    // 4. Weather
    // ---------------------------------------------------------------------------------------------

    fun buildWeatherSpecs(
        cityName: String? = null,
        lat: Double? = null,
        lon: Double? = null
    ): List<SystemIntentSpec> {
        val candidates = mutableListOf<SystemIntentSpec>()

        // 1. Google Weather standalone shortcut URI
        candidates.add(
            SystemIntentSpec(
                action = Intent.ACTION_VIEW,
                dataUri = "dynact://velour/weather/ProxyActivity"
            )
        )

        // 2. Google Search Weather Exported Activity
        candidates.add(
            SystemIntentSpec(
                action = Intent.ACTION_MAIN,
                packageName = "com.google.android.googlequicksearchbox",
                className = "com.google.android.apps.search.weather.WeatherExportedActivity"
            )
        )

        // 3. Known OEM Weather package launchers
        for (pkg in WEATHER_PACKAGES) {
            candidates.add(
                SystemIntentSpec(
                    action = Intent.ACTION_MAIN,
                    packageName = pkg,
                    category = Intent.CATEGORY_LAUNCHER
                )
            )
        }

        // 4. Web-based Weather search fallback
        val cleanCity = cityName?.trim()
        val queryUrl = if (!cleanCity.isNullOrBlank() && !cleanCity.equals("Local Weather", ignoreCase = true) && !cleanCity.equals("Location Needed", ignoreCase = true)) {
            "https://www.google.com/search?q=weather+${cleanCity.replace(" ", "+")}"
        } else if (lat != null && lon != null) {
            "https://www.google.com/search?q=weather+${lat},${lon}"
        } else {
            "https://www.google.com/search?q=weather"
        }

        candidates.add(
            SystemIntentSpec(
                action = Intent.ACTION_VIEW,
                dataUri = queryUrl
            )
        )

        // 5. Geo-URI fallback
        if (lat != null && lon != null) {
            candidates.add(
                SystemIntentSpec(
                    action = Intent.ACTION_VIEW,
                    dataUri = "geo:$lat,$lon?q=weather"
                )
            )
        }

        return candidates
    }

    /**
     * Deep-links the user to the native Google Weather or OEM Weather app, with geocoded web fallback.
     */
    fun launchWeather(
        context: Context,
        cityName: String? = null,
        lat: Double? = null,
        lon: Double? = null
    ): Boolean {
        return safeStartAny(context, buildWeatherSpecs(cityName, lat, lon))
    }

    // ---------------------------------------------------------------------------------------------
    // 5. System Bento Settings (Wi-Fi, Bluetooth, Cellular, Storage)
    // ---------------------------------------------------------------------------------------------

    fun buildWifiSettingsSpecs(): List<SystemIntentSpec> {
        return listOf(
            SystemIntentSpec(action = Settings.ACTION_WIFI_SETTINGS),
            SystemIntentSpec(action = Settings.ACTION_WIRELESS_SETTINGS),
            SystemIntentSpec(action = Settings.ACTION_SETTINGS)
        )
    }

    fun launchWifiSettings(context: Context): Boolean {
        return safeStartAny(context, buildWifiSettingsSpecs())
    }

    fun buildBluetoothSettingsSpecs(): List<SystemIntentSpec> {
        return listOf(
            SystemIntentSpec(action = Settings.ACTION_BLUETOOTH_SETTINGS),
            SystemIntentSpec(action = Settings.ACTION_WIRELESS_SETTINGS),
            SystemIntentSpec(action = Settings.ACTION_SETTINGS)
        )
    }

    fun launchBluetoothSettings(context: Context): Boolean {
        return safeStartAny(context, buildBluetoothSettingsSpecs())
    }

    fun buildCellularSettingsSpecs(): List<SystemIntentSpec> {
        return listOf(
            SystemIntentSpec(action = Settings.ACTION_NETWORK_OPERATOR_SETTINGS),
            SystemIntentSpec(action = Settings.ACTION_WIRELESS_SETTINGS),
            SystemIntentSpec(action = Settings.ACTION_SETTINGS)
        )
    }

    fun launchCellularSettings(context: Context): Boolean {
        return safeStartAny(context, buildCellularSettingsSpecs())
    }

    fun buildStorageSettingsSpecs(): List<SystemIntentSpec> {
        return listOf(
            SystemIntentSpec(action = Settings.ACTION_INTERNAL_STORAGE_SETTINGS),
            SystemIntentSpec(action = Settings.ACTION_SETTINGS)
        )
    }

    fun launchStorageSettings(context: Context): Boolean {
        return safeStartAny(context, buildStorageSettingsSpecs())
    }
}
