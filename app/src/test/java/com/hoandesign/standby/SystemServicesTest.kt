package com.hoandesign.standby

import com.hoandesign.standby.model.NightModePreference
import com.hoandesign.standby.model.NightModeState
import com.hoandesign.standby.receiver.ChargingReceiver
import com.hoandesign.standby.receiver.shouldAutoLaunchStandBy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying System Docking Activation and Ambient Light Sensor logic:
 * 1. [shouldAutoLaunchStandBy] truth-table matrix across charging, landscape, and auto-launch states.
 * 2. Ambient light sensor lux threshold trigger logic across [NightModePreference] modes.
 * 3. [NightModeState] dynamic state transitions via [NightModeState.withLux] and [NightModeState.withPreference].
 */
class SystemServicesTest {

    // ---------------------------------------------------------------------------------------------
    // 1. shouldAutoLaunchStandBy Matrix Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testShouldAutoLaunchStandBy_allConditionsMet_returnsTrue() {
        val result = shouldAutoLaunchStandBy(
            isCharging = true,
            isLandscape = true,
            isAutoLaunchEnabled = true
        )
        assertTrue("StandBy must auto-launch when charging, landscape, and enabled", result)
    }

    @Test
    fun testShouldAutoLaunchStandBy_notCharging_returnsFalse() {
        val result = shouldAutoLaunchStandBy(
            isCharging = false,
            isLandscape = true,
            isAutoLaunchEnabled = true
        )
        assertFalse("StandBy must not launch when not charging", result)
    }

    @Test
    fun testShouldAutoLaunchStandBy_portraitOrientation_returnsFalse() {
        val result = shouldAutoLaunchStandBy(
            isCharging = true,
            isLandscape = false,
            isAutoLaunchEnabled = true
        )
        assertFalse("StandBy must not launch in portrait orientation", result)
    }

    @Test
    fun testShouldAutoLaunchStandBy_autoLaunchDisabled_returnsFalse() {
        val result = shouldAutoLaunchStandBy(
            isCharging = true,
            isLandscape = true,
            isAutoLaunchEnabled = false
        )
        assertFalse("StandBy must not launch when user disabled auto-launch", result)
    }

    @Test
    fun testShouldAutoLaunchStandBy_exhaustiveTruthTableMatrix() {
        val truthTable = listOf(
            Triple(true, true, true) to true,
            Triple(true, true, false) to false,
            Triple(true, false, true) to false,
            Triple(true, false, false) to false,
            Triple(false, true, true) to false,
            Triple(false, true, false) to false,
            Triple(false, false, true) to false,
            Triple(false, false, false) to false
        )

        for ((inputs, expected) in truthTable) {
            val (charging, landscape, enabled) = inputs
            val actualTopLevel = shouldAutoLaunchStandBy(charging, landscape, enabled)
            val actualCompanion = ChargingReceiver.shouldAutoLaunchStandBy(charging, landscape, enabled)

            assertEquals(
                "Failed top-level matrix for (charging=$charging, landscape=$landscape, enabled=$enabled)",
                expected,
                actualTopLevel
            )
            assertEquals(
                "Failed companion matrix for (charging=$charging, landscape=$landscape, enabled=$enabled)",
                expected,
                actualCompanion
            )
        }
    }

    // ---------------------------------------------------------------------------------------------
    // 2. Ambient Light Sensor Lux Threshold Trigger Logic Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testLuxThresholdConstant_isFiveLux() {
        assertEquals(5.0f, NightModeState.LUX_THRESHOLD, 0.001f)
    }

    @Test
    fun testAmbientLux_autoMode_strictlyBelowThreshold_activatesNightMode() {
        val darkLuxValues = listOf(0.0f, 0.5f, 1.0f, 2.5f, 4.0f, 4.9f, 4.99f)

        for (lux in darkLuxValues) {
            val isActive = NightModeState.computeIsNightModeActive(NightModePreference.AUTO, lux)
            assertTrue(
                "Night mode must be active in AUTO mode when lux ($lux) < ${NightModeState.LUX_THRESHOLD}",
                isActive
            )
        }
    }

    @Test
    fun testAmbientLux_autoMode_atOrAboveThreshold_deactivatesNightMode() {
        val brightLuxValues = listOf(5.0f, 5.01f, 5.5f, 10.0f, 50.0f, 100.0f, 1000.0f)

        for (lux in brightLuxValues) {
            val isActive = NightModeState.computeIsNightModeActive(NightModePreference.AUTO, lux)
            assertFalse(
                "Night mode must be inactive in AUTO mode when lux ($lux) >= ${NightModeState.LUX_THRESHOLD}",
                isActive
            )
        }
    }

    @Test
    fun testAmbientLux_alwaysOnMode_ignoresLuxAndStaysActive() {
        val allLuxValues = listOf(0.0f, 2.0f, 5.0f, 10.0f, 500.0f, 10_000.0f)

        for (lux in allLuxValues) {
            val isActive = NightModeState.computeIsNightModeActive(NightModePreference.ALWAYS_ON, lux)
            assertTrue(
                "Night mode must always be active in ALWAYS_ON mode regardless of lux ($lux)",
                isActive
            )
        }
    }

    @Test
    fun testAmbientLux_disabledMode_ignoresLuxAndStaysInactive() {
        val allLuxValues = listOf(0.0f, 1.0f, 4.9f, 5.0f, 10.0f, 500.0f)

        for (lux in allLuxValues) {
            val isActive = NightModeState.computeIsNightModeActive(NightModePreference.DISABLED, lux)
            assertFalse(
                "Night mode must always be inactive in DISABLED mode regardless of lux ($lux)",
                isActive
            )
        }
    }

    // ---------------------------------------------------------------------------------------------
    // 3. NightModeState Factory & Transition Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    fun testNightModeState_fromLuxFactory() {
        val darkState = NightModeState.fromLux(2.0f, NightModePreference.AUTO)
        assertTrue(darkState.isNightModeActive)
        assertEquals(2.0f, darkState.ambientLux, 0.001f)
        assertEquals(NightModePreference.AUTO, darkState.preference)

        val brightState = NightModeState.fromLux(12.0f, NightModePreference.AUTO)
        assertFalse(brightState.isNightModeActive)
        assertEquals(12.0f, brightState.ambientLux, 0.001f)
        assertEquals(NightModePreference.AUTO, brightState.preference)
    }

    @Test
    fun testNightModeState_withLuxDynamicTransitions() {
        val initial = NightModeState(
            isNightModeActive = false,
            preference = NightModePreference.AUTO,
            ambientLux = 100.0f
        )
        assertFalse(initial.isNightModeActive)

        // Dim lights in room
        val dimmed = initial.withLux(1.5f)
        assertTrue("State must transition to active when room dims", dimmed.isNightModeActive)
        assertEquals(1.5f, dimmed.ambientLux, 0.001f)

        // Turn room lights back on
        val brightAgain = dimmed.withLux(80.0f)
        assertFalse("State must transition to inactive when lights turn on", brightAgain.isNightModeActive)
        assertEquals(80.0f, brightAgain.ambientLux, 0.001f)
    }

    @Test
    fun testNightModeState_withPreferenceDynamicTransitions() {
        val initial = NightModeState(
            isNightModeActive = false,
            preference = NightModePreference.AUTO,
            ambientLux = 100.0f
        )
        assertFalse(initial.isNightModeActive)

        // User overrides to ALWAYS_ON in bright room
        val alwaysOn = initial.withPreference(NightModePreference.ALWAYS_ON)
        assertTrue(alwaysOn.isNightModeActive)
        assertEquals(NightModePreference.ALWAYS_ON, alwaysOn.preference)

        // User overrides to DISABLED in dark room
        val darkDisabled = alwaysOn.withLux(0.5f).withPreference(NightModePreference.DISABLED)
        assertFalse(darkDisabled.isNightModeActive)
        assertEquals(NightModePreference.DISABLED, darkDisabled.preference)
    }

    @Test
    fun testChargingReceiver_preferenceConstants() {
        assertEquals("standby_settings", ChargingReceiver.PREFS_NAME)
        assertEquals("auto_launch_on_dock", ChargingReceiver.KEY_AUTO_LAUNCH_ON_DOCK)
        assertEquals("night_mode_preference", ChargingReceiver.KEY_NIGHT_MODE_PREFERENCE)
        assertEquals("accent_color", ChargingReceiver.KEY_ACCENT_COLOR)
    }
}
