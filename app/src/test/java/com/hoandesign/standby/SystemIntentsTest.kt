package com.hoandesign.standby

import android.content.Intent
import android.provider.AlarmClock
import android.provider.Settings
import com.hoandesign.standby.util.SystemIntents
import com.hoandesign.standby.util.SystemIntentSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying deep-link intent resolution, fallback chains, and safety properties in [SystemIntents].
 */
class SystemIntentsTest {

    // ---------------------------------------------------------------------------------------------
    // 1. Battery Settings Intent Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testBuildBatterySettingsSpecs_containsAllFallbacks() {
        val specs = SystemIntents.buildBatterySettingsSpecs()
        assertTrue("Must contain at least 3 battery candidates", specs.size >= 3)
        assertEquals(Intent.ACTION_POWER_USAGE_SUMMARY, specs[0].action)
        assertEquals(Settings.ACTION_BATTERY_SAVER_SETTINGS, specs[1].action)
        assertEquals(Settings.ACTION_SETTINGS, specs[2].action)
    }

    // ---------------------------------------------------------------------------------------------
    // 2. Alarm Clock & Timer Intent Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testBuildAlarmClockSpecs_containsShowAndSetAlarms() {
        val specs = SystemIntents.buildAlarmClockSpecs()
        assertTrue("Must contain multiple alarm candidates", specs.size >= 3)
        assertEquals(AlarmClock.ACTION_SHOW_ALARMS, specs[0].action)
        assertEquals(AlarmClock.ACTION_SET_ALARM, specs[1].action)

        // Verify last fallback is date & time settings
        assertEquals(Settings.ACTION_DATE_SETTINGS, specs.last().action)
    }

    @Test
    fun testBuildAlarmClockSpecs_includesAllKnownOemPackages() {
        val specs = SystemIntents.buildAlarmClockSpecs()
        val targetedPackages = specs.mapNotNull { it.packageName }
        for (pkg in SystemIntents.CLOCK_PACKAGES) {
            assertTrue("Must include clock package candidate: $pkg", targetedPackages.contains(pkg))
        }
    }

    @Test
    fun testBuildTimerSpecs_containsSetTimer() {
        val specs = SystemIntents.buildTimerSpecs()
        assertTrue("Timer specs must not be empty", specs.isNotEmpty())
        assertEquals(AlarmClock.ACTION_SET_TIMER, specs[0].action)
        assertEquals(AlarmClock.ACTION_SHOW_ALARMS, specs[1].action)
        assertEquals(Settings.ACTION_DATE_SETTINGS, specs[2].action)
    }

    // ---------------------------------------------------------------------------------------------
    // 3. Calendar Intent Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testBuildSystemCalendarSpecs_withTimestamp_targetsTargetDate() {
        val targetMillis = 1758450000000L
        val specs = SystemIntents.buildSystemCalendarSpecs(epochMillis = targetMillis)

        val firstSpec = specs.first()
        assertEquals(Intent.ACTION_VIEW, firstSpec.action)
        assertNotNull(firstSpec.dataUri)
        assertTrue(
            "URI must contain target epoch millis",
            firstSpec.dataUri!!.contains(targetMillis.toString())
        )
    }

    @Test
    fun testBuildSystemCalendarSpecs_withoutTimestamp_containsAppCalendarCategory() {
        val specs = SystemIntents.buildSystemCalendarSpecs()
        val hasCategory = specs.any { it.category == Intent.CATEGORY_APP_CALENDAR }
        assertTrue("Must provide CATEGORY_APP_CALENDAR candidate", hasCategory)

        val targetedPackages = specs.mapNotNull { it.packageName }
        for (pkg in SystemIntents.CALENDAR_PACKAGES) {
            assertTrue("Must include calendar package candidate: $pkg", targetedPackages.contains(pkg))
        }
    }

    @Test
    fun testBuildCalendarEventSpecs_withValidId_targetsEventUri() {
        val eventId = 42L
        val specs = SystemIntents.buildCalendarEventSpecs(eventId)
        assertTrue("Must have event view candidates", specs.isNotEmpty())

        val firstSpec = specs[0]
        assertEquals(Intent.ACTION_VIEW, firstSpec.action)
        assertNotNull(firstSpec.dataUri)
        assertTrue(
            "URI must reference event id: ${firstSpec.dataUri}",
            firstSpec.dataUri!!.contains("42")
        )
    }

    // ---------------------------------------------------------------------------------------------
    // 4. Weather Intent Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testBuildWeatherSpecs_includesGoogleWeatherAndWebSearch() {
        val cityName = "Cupertino"
        val specs = SystemIntents.buildWeatherSpecs(cityName = cityName)

        val hasVelour = specs.any { it.dataUri?.contains("dynact://velour/weather") == true }
        assertTrue("Must include Google Weather shortcut", hasVelour)

        val hasSearch = specs.any { it.dataUri?.contains("google.com/search?q=weather") == true }
        assertTrue("Must include Google search fallback", hasSearch)
    }

    @Test
    fun testBuildWeatherSpecs_withCoordinatesFallback_generatesGeoAndCoordSearch() {
        val specs = SystemIntents.buildWeatherSpecs(cityName = null, lat = 37.77, lon = -122.42)
        val hasGeo = specs.any { it.dataUri?.startsWith("geo:37.77,-122.42") == true }
        assertTrue("Must include geo: coordinates URI fallback", hasGeo)

        val hasCoordSearch = specs.any { it.dataUri?.contains("37.77,-122.42") == true }
        assertTrue("Must include coordinates web search fallback", hasCoordSearch)
    }

    @Test
    fun testBuildWeatherSpecs_includesOemWeatherPackages() {
        val specs = SystemIntents.buildWeatherSpecs()
        val packages = specs.mapNotNull { it.packageName }
        for (pkg in SystemIntents.WEATHER_PACKAGES) {
            assertTrue("Must include OEM weather package: $pkg", packages.contains(pkg))
        }
    }

    // ---------------------------------------------------------------------------------------------
    // 5. System Bento Settings Intent Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testBuildWifiSettingsSpecs_containsWifiAction() {
        val specs = SystemIntents.buildWifiSettingsSpecs()
        assertEquals(Settings.ACTION_WIFI_SETTINGS, specs[0].action)
        assertEquals(Settings.ACTION_WIRELESS_SETTINGS, specs[1].action)
        assertEquals(Settings.ACTION_SETTINGS, specs[2].action)
    }

    @Test
    fun testBuildBluetoothSettingsSpecs_containsBluetoothAction() {
        val specs = SystemIntents.buildBluetoothSettingsSpecs()
        assertEquals(Settings.ACTION_BLUETOOTH_SETTINGS, specs[0].action)
        assertEquals(Settings.ACTION_WIRELESS_SETTINGS, specs[1].action)
        assertEquals(Settings.ACTION_SETTINGS, specs[2].action)
    }

    @Test
    fun testBuildCellularSettingsSpecs_containsCellularAction() {
        val specs = SystemIntents.buildCellularSettingsSpecs()
        assertEquals(Settings.ACTION_NETWORK_OPERATOR_SETTINGS, specs[0].action)
        assertEquals(Settings.ACTION_WIRELESS_SETTINGS, specs[1].action)
        assertEquals(Settings.ACTION_SETTINGS, specs[2].action)
    }

    @Test
    fun testBuildStorageSettingsSpecs_containsStorageAction() {
        val specs = SystemIntents.buildStorageSettingsSpecs()
        assertEquals(Settings.ACTION_INTERNAL_STORAGE_SETTINGS, specs[0].action)
        assertEquals(Settings.ACTION_SETTINGS, specs[1].action)
    }

    // ---------------------------------------------------------------------------------------------
    // 6. SystemIntentSpec Data Integrity Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testSystemIntentSpec_toIntent_createsCorrectIntent() {
        val spec = SystemIntentSpec(
            action = Intent.ACTION_VIEW,
            dataUri = "https://google.com",
            packageName = "com.google.android.googlequicksearchbox",
            category = Intent.CATEGORY_BROWSABLE
        )
        val intent = spec.toIntent()
        assertNotNull(intent)
    }
}
