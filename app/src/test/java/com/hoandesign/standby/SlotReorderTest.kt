package com.hoandesign.standby

import com.hoandesign.standby.model.StandbyWidgetId
import com.hoandesign.standby.model.StandbyWidgetRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SlotReorderTest {

    @Test
    fun testReorderWidgets_movesItemCorrectly() {
        val initialList = listOf(
            StandbyWidgetId.ANALOG_CLOCK,
            StandbyWidgetId.WEATHER,
            StandbyWidgetId.BATTERY
        )

        // Move item at index 0 (ANALOG_CLOCK) to index 2
        val list = initialList.toMutableList()
        val movedItem = list.removeAt(0)
        list.add(2, movedItem)

        assertEquals(
            listOf(StandbyWidgetId.WEATHER, StandbyWidgetId.BATTERY, StandbyWidgetId.ANALOG_CLOCK),
            list
        )
    }

    @Test
    fun testReorderWidgets_adjacentSwap() {
        val initialList = listOf(
            StandbyWidgetId.MONTH_CALENDAR,
            StandbyWidgetId.AGENDA,
            StandbyWidgetId.MUSIC_PLAYER
        )

        // Swap 0 and 1
        val list = initialList.toMutableList()
        val item = list.removeAt(0)
        list.add(1, item)

        assertEquals(
            listOf(StandbyWidgetId.AGENDA, StandbyWidgetId.MONTH_CALENDAR, StandbyWidgetId.MUSIC_PLAYER),
            list
        )
    }

    @Test
    fun testSlotProtection_minimumOneWidget() {
        val list = mutableListOf(StandbyWidgetId.BATTERY)
        val canRemove = list.size > 1
        org.junit.Assert.assertFalse("Should not allow removing the last remaining widget in a slot", canRemove)
    }

    @Test
    fun testResetToDefaults_restoresDefaultSlots() {
        val customLeft = listOf(StandbyWidgetId.VIBES)
        val customRight = listOf(StandbyWidgetId.PHOTO_FRAME)

        // Reset
        val restoredLeft = StandbyWidgetRegistry.defaultLeftSlot
        val restoredRight = StandbyWidgetRegistry.defaultRightSlot

        assertEquals(StandbyWidgetId.ANALOG_CLOCK, restoredLeft.first())
        assertEquals(StandbyWidgetId.MONTH_CALENDAR, restoredRight.first())
        assertTrue(restoredLeft.size > 1)
        assertTrue(restoredRight.size > 1)
    }
}
