package com.hoandesign.standby

import com.hoandesign.standby.model.StandbyWidgetId
import com.hoandesign.standby.model.StandbyWidgetRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetRegistryTest {

    @Test
    fun testAllFourteenWidgetsRegistered() {
        assertEquals("Must have exactly 14 modular widgets", 14, StandbyWidgetRegistry.allWidgets.size)

        val ids = StandbyWidgetRegistry.allWidgets.toSet()
        assertTrue(ids.contains(StandbyWidgetId.ANALOG_CLOCK))
        assertTrue(ids.contains(StandbyWidgetId.BIG_DIGITAL_CLOCK))
        assertTrue(ids.contains(StandbyWidgetId.RETRO_FLIP_CLOCK))
        assertTrue(ids.contains(StandbyWidgetId.RADIAL_CLOCK))
        assertTrue(ids.contains(StandbyWidgetId.SOLAR_ARC_CLOCK))
        assertTrue(ids.contains(StandbyWidgetId.WEATHER))
        assertTrue(ids.contains(StandbyWidgetId.MONTH_CALENDAR))
        assertTrue(ids.contains(StandbyWidgetId.AGENDA))
        assertTrue(ids.contains(StandbyWidgetId.BATTERY))
        assertTrue(ids.contains(StandbyWidgetId.MUSIC_PLAYER))
        assertTrue(ids.contains(StandbyWidgetId.SYSTEM_BENTO))
        assertTrue(ids.contains(StandbyWidgetId.DESK_TIMER))
        assertTrue(ids.contains(StandbyWidgetId.VIBES))
        assertTrue(ids.contains(StandbyWidgetId.PHOTO_FRAME))
    }

    @Test
    fun testDefaultSlotsNotEmpty() {
        assertTrue(StandbyWidgetRegistry.defaultLeftSlot.isNotEmpty())
        assertTrue(StandbyWidgetRegistry.defaultRightSlot.isNotEmpty())
        assertEquals(StandbyWidgetId.ANALOG_CLOCK, StandbyWidgetRegistry.defaultLeftSlot.first())
        assertEquals(StandbyWidgetId.MONTH_CALENDAR, StandbyWidgetRegistry.defaultRightSlot.first())
    }

    @Test
    fun testSerializeAndDeserializeRoundTrip() {
        val original = listOf(
            StandbyWidgetId.BATTERY,
            StandbyWidgetId.WEATHER,
            StandbyWidgetId.DESK_TIMER
        )

        val serialized = StandbyWidgetRegistry.serializeWidgetList(original)
        assertEquals("BATTERY,WEATHER,DESK_TIMER", serialized)

        val deserialized = StandbyWidgetRegistry.deserializeWidgetList(serialized, emptyList())
        assertEquals(original, deserialized)
    }

    @Test
    fun testDeserializeHandlesInvalidAndEmptyStrings() {
        val fallback = listOf(StandbyWidgetId.ANALOG_CLOCK)

        // Null and blank strings should return fallback
        assertEquals(fallback, StandbyWidgetRegistry.deserializeWidgetList(null, fallback))
        assertEquals(fallback, StandbyWidgetRegistry.deserializeWidgetList("", fallback))
        assertEquals(fallback, StandbyWidgetRegistry.deserializeWidgetList("   ", fallback))

        // Corrupted or unknown names should be filtered out safely
        val mixed = "BATTERY,UNKNOWN_WIDGET,WEATHER,CORRUPTED"
        val parsed = StandbyWidgetRegistry.deserializeWidgetList(mixed, fallback)
        assertEquals(listOf(StandbyWidgetId.BATTERY, StandbyWidgetId.WEATHER), parsed)

        // Completely unknown list should return fallback
        val completelyInvalid = "FOO,BAR,BAZ"
        assertEquals(fallback, StandbyWidgetRegistry.deserializeWidgetList(completelyInvalid, fallback))
    }
}
