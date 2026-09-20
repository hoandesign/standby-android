package com.hoandesign.standby.ui.layout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.hoandesign.standby.model.LayoutArchetype
import com.hoandesign.standby.model.NavigationState
import com.hoandesign.standby.model.StandbyScreen
import com.hoandesign.standby.model.StandbyWidgetId
import com.hoandesign.standby.model.StandbyWidgetRegistry
import com.hoandesign.standby.model.WidgetDisplayMode
import com.hoandesign.standby.model.collapseToDual
import com.hoandesign.standby.model.expandSlot
import com.hoandesign.standby.model.setScreen
import com.hoandesign.standby.ui.components.HorizontalPagerIndicator
import com.hoandesign.standby.ui.components.VerticalPagerIndicator
import com.hoandesign.standby.ui.theme.StandbyBackground
import com.hoandesign.standby.ui.theme.StandbyBorderSubtle
import com.hoandesign.standby.ui.theme.StandbyCardBgSecondary
import com.hoandesign.standby.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

/**
 * Enhanced Dual-Axis Swiping Navigation Coordinator for StandBy Android.
 *
 * Coordinates:
 * - Page 0 ([StandbyScreen.DUAL_WIDGET]): Dynamic dual-slot bento widgets with:
 *   - Customizable Left and Right widget stacks.
 *   - Edit Mode to reorder/remove/add widgets.
 *   - True Full-Screen Single Slot UI expansion for any widget.
 *   - Auto-hiding top floating navigation bar with dynamic inset shift to eliminate any visual overlap.
 *   - Non-blocking tap-to-show detection using [PointerEventPass.Initial].
 * - Page 1 ([StandbyScreen.HERO_CLOCK]): Immersive full-screen Hero Clock suite.
 * - Page 2 ([StandbyScreen.NOW_PLAYING]): Full-screen Media & Music playback interface.
 */
@Composable
fun MainStandbyPager(
    archetype: LayoutArchetype,
    leftSlotWidgetIds: List<StandbyWidgetId>,
    rightSlotWidgetIds: List<StandbyWidgetId>,
    accentColor: Color,
    isEditMode: Boolean,
    onToggleEditMode: () -> Unit,
    onOpenWidgetPicker: (slotIndex: Int) -> Unit,
    onRemoveWidgetFromSlot: (slotIndex: Int, widgetIndex: Int) -> Unit,
    onReorderSlotWidgets: (slotIndex: Int, from: Int, to: Int) -> Unit = { _, _, _ -> },
    onResetSlotDefaults: (slotIndex: Int) -> Unit = {},
    heroClockContent: @Composable () -> Unit,
    nowPlayingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {},
    initialNavigationState: NavigationState = NavigationState(),
    onNavigationStateChange: ((NavigationState) -> Unit)? = null
) {
    var navigationState by remember { mutableStateOf(initialNavigationState) }
    val screens = StandbyScreen.entries

    val horizontalPagerState = rememberPagerState(
        initialPage = navigationState.currentScreen.ordinal,
        pageCount = { screens.size }
    )

    val safeLeftIds = if (leftSlotWidgetIds.isEmpty()) listOf(StandbyWidgetId.ANALOG_CLOCK) else leftSlotWidgetIds
    val safeRightIds = if (rightSlotWidgetIds.isEmpty()) listOf(StandbyWidgetId.MONTH_CALENDAR) else rightSlotWidgetIds

    val leftPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { safeLeftIds.size }
    )

    val rightPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { safeRightIds.size }
    )

    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var isNavVisible by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    fun recordInteraction() {
        lastInteractionTime = System.currentTimeMillis()
        isNavVisible = true
    }

    // Auto-hide navigation menu after 4.5s of inactivity when not in edit mode
    LaunchedEffect(isNavVisible, lastInteractionTime, isEditMode) {
        if (isNavVisible && !isEditMode) {
            delay(4500L)
            isNavVisible = false
        }
    }

    // Snap haptic feedback on vertical stack page change (Left slot)
    LaunchedEffect(leftPagerState) {
        snapshotFlow { leftPagerState.currentPage }
            .drop(1)
            .collect {
                recordInteraction()
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                } catch (e: Throwable) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
    }

    // Snap haptic feedback on vertical stack page change (Right slot)
    LaunchedEffect(rightPagerState) {
        snapshotFlow { rightPagerState.currentPage }
            .drop(1)
            .collect {
                recordInteraction()
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                } catch (e: Throwable) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
    }

    // Snap haptic feedback on horizontal screen change
    LaunchedEffect(horizontalPagerState) {
        snapshotFlow { horizontalPagerState.currentPage }
            .drop(1)
            .collect {
                recordInteraction()
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                } catch (e: Throwable) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
    }

    // Synchronize horizontal pager page with navigation state
    LaunchedEffect(horizontalPagerState.currentPage) {
        val targetScreen = screens[horizontalPagerState.currentPage]
        if (navigationState.currentScreen != targetScreen) {
            val updated = navigationState.setScreen(targetScreen)
            navigationState = updated
            onNavigationStateChange?.invoke(updated)
        }
    }

    // Synchronize external navigation state changes to pager
    LaunchedEffect(navigationState.currentScreen) {
        if (horizontalPagerState.currentPage != navigationState.currentScreen.ordinal) {
            horizontalPagerState.animateScrollToPage(navigationState.currentScreen.ordinal)
        }
    }

    fun updateNavigationState(newState: NavigationState) {
        navigationState = newState
        onNavigationStateChange?.invoke(newState)
    }

    // Smoothly shift bento cards down when the navigation menu is visible or in edit mode.
    // This completely eliminates any visual collision with clock numerals or month titles!
    val navTopInset by animateDpAsState(
        targetValue = if ((isNavVisible || isEditMode) && navigationState.displayMode == WidgetDisplayMode.DUAL) 48.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "navTopInset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StandbyBackground)
            .pointerInput(Unit) {
                // Non-blocking initial pointer pass: reliably reveals nav bar upon any touch
                // without swallowing gestures from child composables (sliders, buttons, pagers).
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    recordInteraction()
                }
            }
    ) {
        HorizontalPager(
            state = horizontalPagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            when (screens[pageIndex]) {
                StandbyScreen.DUAL_WIDGET -> {
                    AnimatedContent(
                        targetState = navigationState.displayMode,
                        transitionSpec = {
                            (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                             scaleIn(initialScale = 0.94f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                                .togetherWith(
                                    fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                    scaleOut(targetScale = 0.94f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                                )
                        },
                        label = "SingleDualModeTransition"
                    ) { mode ->
                        when (mode) {
                            WidgetDisplayMode.DUAL -> {
                                val isPortrait = archetype == LayoutArchetype.TALL_PORTRAIT
                                val slot1Title = if (isPortrait) "TOP BENTO" else "LEFT BENTO"
                                val slot2Title = if (isPortrait) "BOTTOM BENTO" else "RIGHT BENTO"

                                AdaptiveStandbyLayout(
                                    slot1 = {
                                        DynamicSlotCard(
                                            slotIndex = 0,
                                            slotTitle = slot1Title,
                                            widgetIds = safeLeftIds,
                                            pagerState = leftPagerState,
                                            accentColor = accentColor,
                                            isEditMode = isEditMode,
                                            onOpenWidgetPicker = {
                                                recordInteraction()
                                                onOpenWidgetPicker(0)
                                            },
                                            onRemoveWidget = { idx ->
                                                recordInteraction()
                                                onRemoveWidgetFromSlot(0, idx)
                                            },
                                            onToggleExpand = {
                                                recordInteraction()
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                if (isEditMode) onToggleEditMode()
                                                updateNavigationState(navigationState.expandSlot(0))
                                            },
                                            onReorderWidgets = { from, to ->
                                                recordInteraction()
                                                onReorderSlotWidgets(0, from, to)
                                            },
                                            onUserInteraction = { recordInteraction() }
                                        )
                                    },
                                    slot2 = {
                                        DynamicSlotCard(
                                            slotIndex = 1,
                                            slotTitle = slot2Title,
                                            widgetIds = safeRightIds,
                                            pagerState = rightPagerState,
                                            accentColor = accentColor,
                                            isEditMode = isEditMode,
                                            onOpenWidgetPicker = {
                                                recordInteraction()
                                                onOpenWidgetPicker(1)
                                            },
                                            onRemoveWidget = { idx ->
                                                recordInteraction()
                                                onRemoveWidgetFromSlot(1, idx)
                                            },
                                            onToggleExpand = {
                                                recordInteraction()
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                if (isEditMode) onToggleEditMode()
                                                updateNavigationState(navigationState.expandSlot(1))
                                            },
                                            onReorderWidgets = { from, to ->
                                                recordInteraction()
                                                onReorderSlotWidgets(1, from, to)
                                            },
                                            onUserInteraction = { recordInteraction() }
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = navTopInset)
                                )
                            }
                            WidgetDisplayMode.SINGLE_EXPANDED -> {
                                // True Edge-to-Edge Fullscreen Single Slot Feature
                                val isLeftSlot = navigationState.expandedSlotIndex == 0
                                val activeWidgetId = if (isLeftSlot) {
                                    val safeIndex = leftPagerState.currentPage.coerceIn(0, safeLeftIds.size - 1)
                                    safeLeftIds[safeIndex]
                                } else {
                                    val safeIndex = rightPagerState.currentPage.coerceIn(0, safeRightIds.size - 1)
                                    safeRightIds[safeIndex]
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onTap = { recordInteraction() },
                                                onDoubleTap = {
                                                    recordInteraction()
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    updateNavigationState(navigationState.collapseToDual())
                                                }
                                            )
                                        }
                                ) {
                                    // True Fullscreen Composable
                                    StandbyWidgetRegistry.RenderFullscreen(
                                        widgetId = activeWidgetId,
                                        accentColor = accentColor,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Floating Exit / Collapse Button
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(top = 18.dp, start = 20.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(StandbyCardBgSecondary.copy(alpha = 0.88f))
                                            .border(1.dp, StandbyBorderSubtle, RoundedCornerShape(16.dp))
                                            .clickable {
                                                recordInteraction()
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                updateNavigationState(navigationState.collapseToDual())
                                            }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FullscreenExit,
                                                contentDescription = "Collapse to Dual Bento",
                                                tint = TextPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Dual Bento",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                StandbyScreen.HERO_CLOCK -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { recordInteraction() })
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        heroClockContent()
                    }
                }
                StandbyScreen.NOW_PLAYING -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { recordInteraction() })
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        nowPlayingContent()
                    }
                }
            }
        }

        // Floating Auto-Hiding Top Navigation Menu
        StandbyTopNavigationMenu(
            visible = (isNavVisible || isEditMode) && navigationState.displayMode == WidgetDisplayMode.DUAL,
            currentScreen = screens[horizontalPagerState.currentPage],
            isEditMode = isEditMode,
            accentColor = accentColor,
            onSelectScreen = { targetScreen ->
                recordInteraction()
                coroutineScope.launch {
                    horizontalPagerState.animateScrollToPage(targetScreen.ordinal)
                }
            },
            onToggleEditMode = {
                recordInteraction()
                onToggleEditMode()
            },
            onOpenSettings = {
                recordInteraction()
                onOpenSettings()
            },
            onResetAllDefaults = {
                recordInteraction()
                onResetSlotDefaults(0)
                onResetSlotDefaults(1)
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp, start = 16.dp, end = 16.dp)
        )

        // Subtle Top Pill Handle (when navigation is hidden, gives subtle hint to tap to show)
        AnimatedVisibility(
            visible = !isNavVisible && !isEditMode && navigationState.displayMode == WidgetDisplayMode.DUAL,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .zIndex(15f)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0x33FFFFFF))
                    .clickable { recordInteraction() }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0x88FFFFFF))
                )
            }
        }

        // Sleek iOS-style horizontal page indicator anchored at bottom edge
        AnimatedVisibility(
            visible = (isNavVisible || isEditMode) && navigationState.displayMode == WidgetDisplayMode.DUAL,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        ) {
            HorizontalPagerIndicator(
                pageCount = screens.size,
                currentPage = horizontalPagerState.currentPage
            )
        }
    }
}

