package com.hoandesign.standby

import com.hoandesign.standby.model.NavigationState
import com.hoandesign.standby.model.StandbyScreen
import com.hoandesign.standby.model.WidgetDisplayMode
import com.hoandesign.standby.model.collapseToDual
import com.hoandesign.standby.model.expandSlot
import com.hoandesign.standby.model.nextScreen
import com.hoandesign.standby.model.previousScreen
import com.hoandesign.standby.model.setScreen
import com.hoandesign.standby.model.toggleDisplayMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class StandbyNavigationTest {

    @Test
    fun testStandbyScreen_enumOrderAndTitles() {
        val screens = StandbyScreen.entries
        assertEquals(3, screens.size)

        assertEquals(StandbyScreen.DUAL_WIDGET, screens[0])
        assertEquals(StandbyScreen.HERO_CLOCK, screens[1])
        assertEquals(StandbyScreen.NOW_PLAYING, screens[2])

        assertEquals(0, StandbyScreen.DUAL_WIDGET.ordinal)
        assertEquals(1, StandbyScreen.HERO_CLOCK.ordinal)
        assertEquals(2, StandbyScreen.NOW_PLAYING.ordinal)

        assertEquals("Widgets", StandbyScreen.DUAL_WIDGET.title)
        assertEquals("Clock", StandbyScreen.HERO_CLOCK.title)
        assertEquals("Music", StandbyScreen.NOW_PLAYING.title)
    }

    @Test
    fun testNavigationState_defaults() {
        val defaultState = NavigationState()
        assertEquals(StandbyScreen.DUAL_WIDGET, defaultState.currentScreen)
        assertEquals(WidgetDisplayMode.DUAL, defaultState.displayMode)
        assertEquals(0, defaultState.expandedSlotIndex)
    }

    @Test
    fun testNavigationState_toggleDisplayMode() {
        val initialState = NavigationState(
            currentScreen = StandbyScreen.DUAL_WIDGET,
            displayMode = WidgetDisplayMode.DUAL,
            expandedSlotIndex = 0
        )

        // Toggle from DUAL -> SINGLE_EXPANDED (slot 0)
        val expandedSlot0 = initialState.toggleDisplayMode(slotIndex = 0)
        assertEquals(WidgetDisplayMode.SINGLE_EXPANDED, expandedSlot0.displayMode)
        assertEquals(0, expandedSlot0.expandedSlotIndex)
        assertEquals(StandbyScreen.DUAL_WIDGET, expandedSlot0.currentScreen)

        // Toggle back from SINGLE_EXPANDED -> DUAL
        val collapsedState = expandedSlot0.toggleDisplayMode()
        assertEquals(WidgetDisplayMode.DUAL, collapsedState.displayMode)

        // Toggle from DUAL -> SINGLE_EXPANDED (slot 1)
        val expandedSlot1 = initialState.toggleDisplayMode(slotIndex = 1)
        assertEquals(WidgetDisplayMode.SINGLE_EXPANDED, expandedSlot1.displayMode)
        assertEquals(1, expandedSlot1.expandedSlotIndex)

        // Verify immutability: initial state unchanged
        assertEquals(WidgetDisplayMode.DUAL, initialState.displayMode)
    }

    @Test
    fun testNavigationState_expandSlotAndCollapseToDual() {
        val state = NavigationState()

        val expandedSlot1 = state.expandSlot(1)
        assertEquals(WidgetDisplayMode.SINGLE_EXPANDED, expandedSlot1.displayMode)
        assertEquals(1, expandedSlot1.expandedSlotIndex)

        val collapsed = expandedSlot1.collapseToDual()
        assertEquals(WidgetDisplayMode.DUAL, collapsed.displayMode)
        assertEquals(1, collapsed.expandedSlotIndex) // Slot index preserved for re-expansion
    }

    @Test
    fun testNavigationState_screenCycling() {
        var state = NavigationState()
        assertEquals(StandbyScreen.DUAL_WIDGET, state.currentScreen)

        // Forward cycling: DUAL_WIDGET -> HERO_CLOCK -> NOW_PLAYING -> DUAL_WIDGET
        state = state.nextScreen()
        assertEquals(StandbyScreen.HERO_CLOCK, state.currentScreen)

        state = state.nextScreen()
        assertEquals(StandbyScreen.NOW_PLAYING, state.currentScreen)

        state = state.nextScreen()
        assertEquals(StandbyScreen.DUAL_WIDGET, state.currentScreen)

        // Backward cycling: DUAL_WIDGET -> NOW_PLAYING -> HERO_CLOCK -> DUAL_WIDGET
        state = state.previousScreen()
        assertEquals(StandbyScreen.NOW_PLAYING, state.currentScreen)

        state = state.previousScreen()
        assertEquals(StandbyScreen.HERO_CLOCK, state.currentScreen)

        state = state.previousScreen()
        assertEquals(StandbyScreen.DUAL_WIDGET, state.currentScreen)
    }

    @Test
    fun testNavigationState_setScreen() {
        val state = NavigationState()
        val musicState = state.setScreen(StandbyScreen.NOW_PLAYING)
        assertEquals(StandbyScreen.NOW_PLAYING, musicState.currentScreen)

        val clockState = musicState.setScreen(StandbyScreen.HERO_CLOCK)
        assertEquals(StandbyScreen.HERO_CLOCK, clockState.currentScreen)
    }

    @Test
    fun testNavigationState_toggleDisplayModePreservesCurrentScreen() {
        val clockState = NavigationState(currentScreen = StandbyScreen.HERO_CLOCK)
        val expandedClock = clockState.toggleDisplayMode(slotIndex = 0)
        assertEquals(StandbyScreen.HERO_CLOCK, expandedClock.currentScreen)
        assertEquals(WidgetDisplayMode.SINGLE_EXPANDED, expandedClock.displayMode)
    }
}
