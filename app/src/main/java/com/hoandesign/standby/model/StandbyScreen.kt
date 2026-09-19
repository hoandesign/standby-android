package com.hoandesign.standby.model

/**
 * High-level screens available in the root horizontal navigation of StandBy Android.
 */
enum class StandbyScreen(val title: String) {
    DUAL_WIDGET("Widgets"),
    HERO_CLOCK("Clock"),
    NOW_PLAYING("Music")
}

/**
 * Display layout mode for widget screen:
 * - [DUAL]: Default side-by-side or stacked dual widget slots.
 * - [SINGLE_EXPANDED]: One selected slot expanded full-screen for focused display.
 */
enum class WidgetDisplayMode {
    DUAL,
    SINGLE_EXPANDED
}

/**
 * Immutable navigation state tracking active screen, widget mode, and expanded slot index.
 *
 * @param currentScreen Currently active horizontal screen ([StandbyScreen.DUAL_WIDGET], [StandbyScreen.HERO_CLOCK], or [StandbyScreen.NOW_PLAYING]).
 * @param displayMode Active widget display layout mode ([WidgetDisplayMode.DUAL] or [WidgetDisplayMode.SINGLE_EXPANDED]).
 * @param expandedSlotIndex Index of the slot that is expanded when in [WidgetDisplayMode.SINGLE_EXPANDED] (0 for left/top slot, 1 for right/bottom slot).
 */
data class NavigationState(
    val currentScreen: StandbyScreen = StandbyScreen.DUAL_WIDGET,
    val displayMode: WidgetDisplayMode = WidgetDisplayMode.DUAL,
    val expandedSlotIndex: Int = 0
)

/**
 * Toggles between [WidgetDisplayMode.DUAL] and [WidgetDisplayMode.SINGLE_EXPANDED].
 * When transitioning into SINGLE_EXPANDED mode, [slotIndex] is marked as the expanded slot.
 */
fun NavigationState.toggleDisplayMode(slotIndex: Int = expandedSlotIndex): NavigationState {
    return if (displayMode == WidgetDisplayMode.DUAL) {
        copy(displayMode = WidgetDisplayMode.SINGLE_EXPANDED, expandedSlotIndex = slotIndex)
    } else {
        copy(displayMode = WidgetDisplayMode.DUAL)
    }
}

/**
 * Explicitly expands the specified slot [slotIndex] into full-screen mode.
 */
fun NavigationState.expandSlot(slotIndex: Int): NavigationState {
    return copy(displayMode = WidgetDisplayMode.SINGLE_EXPANDED, expandedSlotIndex = slotIndex)
}

/**
 * Explicitly collapses any expanded slot back into dual-widget mode.
 */
fun NavigationState.collapseToDual(): NavigationState {
    return copy(displayMode = WidgetDisplayMode.DUAL)
}

/**
 * Transitions to the next screen in [StandbyScreen] order, wrapping around if at the end.
 */
fun NavigationState.nextScreen(): NavigationState {
    val screens = StandbyScreen.entries
    val nextIndex = (currentScreen.ordinal + 1) % screens.size
    return copy(currentScreen = screens[nextIndex])
}

/**
 * Transitions to the previous screen in [StandbyScreen] order, wrapping around if at the beginning.
 */
fun NavigationState.previousScreen(): NavigationState {
    val screens = StandbyScreen.entries
    val prevIndex = if (currentScreen.ordinal - 1 < 0) screens.size - 1 else currentScreen.ordinal - 1
    return copy(currentScreen = screens[prevIndex])
}

/**
 * Transitions directly to the specified [screen].
 */
fun NavigationState.setScreen(screen: StandbyScreen): NavigationState {
    return copy(currentScreen = screen)
}
