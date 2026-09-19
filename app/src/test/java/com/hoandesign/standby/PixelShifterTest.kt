package com.hoandesign.standby

import com.hoandesign.standby.model.NightModePreference
import com.hoandesign.standby.model.NightModeState
import com.hoandesign.standby.ui.components.calculatePixelShiftOffset
import com.hoandesign.standby.ui.components.createNightModeColorMatrix
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PixelShifterTest {

    @Test
    fun testPixelShiftOffset_staysWithinBounds() {
        // Test across multiple full cycles including negative and large numbers
        val testSteps = (-16L..100L).toList()

        for (step in testSteps) {
            val (dx, dy) = calculatePixelShiftOffset(step)

            assertTrue(
                "dx ($dx) at step $step must be >= -1.5f",
                dx >= -1.5f
            )
            assertTrue(
                "dx ($dx) at step $step must be <= 1.5f",
                dx <= 1.5f
            )
            assertTrue(
                "dy ($dy) at step $step must be >= -1.5f",
                dy >= -1.5f
            )
            assertTrue(
                "dy ($dy) at step $step must be <= 1.5f",
                dy <= 1.5f
            )
        }
    }

    @Test
    fun testPixelShiftOffset_shiftsOverConsecutiveSteps() {
        // Consecutive steps must produce distinct offsets to migrate OLED subpixels
        for (step in 0L..32L) {
            val current = calculatePixelShiftOffset(step)
            val next = calculatePixelShiftOffset(step + 1)

            val isDistinct = current.first != next.first || current.second != next.second
            assertTrue(
                "Offset at step $step $current must differ from step ${step + 1} $next",
                isDistinct
            )
        }
    }

    @Test
    fun testPixelShiftOffset_largeAndNegativeSteps() {
        val positive = calculatePixelShiftOffset(1_000_000L)
        assertTrue(positive.first in -1.5f..1.5f)
        assertTrue(positive.second in -1.5f..1.5f)

        val negative = calculatePixelShiftOffset(-800L)
        assertTrue(negative.first in -1.5f..1.5f)
        assertTrue(negative.second in -1.5f..1.5f)

        // Periodicity test: 8-step cycle
        val step0 = calculatePixelShiftOffset(0L)
        val step8 = calculatePixelShiftOffset(8L)
        assertEquals(step0.first, step8.first, 0.0001f)
        assertEquals(step0.second, step8.second, 0.0001f)
    }

    @Test
    fun testNightModeState_autoModeLuxThreshold() {
        // Threshold is below 5.0f lux
        val darkRoomState = NightModeState.fromLux(4.9f, NightModePreference.AUTO)
        assertTrue("Night mode should be active at 4.9 lux in AUTO mode", darkRoomState.isNightModeActive)

        val pitchBlackState = NightModeState.fromLux(0.0f, NightModePreference.AUTO)
        assertTrue("Night mode should be active at 0.0 lux in AUTO mode", pitchBlackState.isNightModeActive)

        val thresholdState = NightModeState.fromLux(5.0f, NightModePreference.AUTO)
        assertFalse("Night mode should NOT be active at exactly 5.0 lux in AUTO mode", thresholdState.isNightModeActive)

        val brightRoomState = NightModeState.fromLux(50.0f, NightModePreference.AUTO)
        assertFalse("Night mode should NOT be active at 50 lux in AUTO mode", brightRoomState.isNightModeActive)
    }

    @Test
    fun testNightModeState_alwaysOnPreference() {
        val brightSunlight = NightModeState.fromLux(10_000f, NightModePreference.ALWAYS_ON)
        assertTrue("Night mode should be active when ALWAYS_ON regardless of lux", brightSunlight.isNightModeActive)
    }

    @Test
    fun testNightModeState_disabledPreference() {
        val pitchBlack = NightModeState.fromLux(0f, NightModePreference.DISABLED)
        assertFalse("Night mode should NOT be active when DISABLED even in pitch black", pitchBlack.isNightModeActive)
    }

    @Test
    fun testNightModeState_withLuxAndWithPreferenceHelpers() {
        val initial = NightModeState(preference = NightModePreference.AUTO, ambientLux = 100f)
        assertFalse(initial.computeIsNightModeActive())

        val dimmed = initial.withLux(2.5f)
        assertEquals(2.5f, dimmed.ambientLux, 0.001f)
        assertTrue(dimmed.isNightModeActive)

        val brightened = dimmed.withLux(15f)
        assertEquals(15f, brightened.ambientLux, 0.001f)
        assertFalse(brightened.isNightModeActive)

        val forcedOn = brightened.withPreference(NightModePreference.ALWAYS_ON)
        assertEquals(NightModePreference.ALWAYS_ON, forcedOn.preference)
        assertTrue(forcedOn.isNightModeActive)
    }

    @Test
    fun testNightModeColorMatrix_identityAtZero() {
        val identityMatrix = createNightModeColorMatrix(0f)
        // Values array should match default identity matrix
        val values = identityMatrix.values
        assertEquals(1f, values[0], 0.001f) // R -> R
        assertEquals(1f, values[6], 0.001f) // G -> G
        assertEquals(1f, values[12], 0.001f) // B -> B
        assertEquals(1f, values[18], 0.001f) // A -> A
    }

    @Test
    fun testNightModeColorMatrix_redShiftAtFull() {
        val nightMatrix = createNightModeColorMatrix(1f, brightnessScale = 0.65f)
        val values = nightMatrix.values
        // In full night mode, R channel weight should dominate over G and B channels
        val redWeight = values[0]
        val greenWeight = values[5]
        val blueWeight = values[10]

        assertTrue("Red channel must be significantly greater than green channel", redWeight > greenWeight)
        assertTrue("Red channel must be significantly greater than blue channel", redWeight > blueWeight)
    }
}
