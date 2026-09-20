package com.hoandesign.standby.ui.layout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoandesign.standby.model.LayoutArchetype
import com.hoandesign.standby.model.NavigationState
import com.hoandesign.standby.model.StandbyScreen
import com.hoandesign.standby.model.StandbyWidgetId
import com.hoandesign.standby.model.StandbyWidgetRegistry
import com.hoandesign.standby.model.WidgetDisplayMode
import com.hoandesign.standby.model.collapseToDual
import com.hoandesign.standby.model.expandSlot
import com.hoandesign.standby.model.setScreen
import com.hoandesign.standby.model.toggleDisplayMode
import com.hoandesign.standby.ui.components.HorizontalPagerIndicator
import com.hoandesign.standby.ui.components.VerticalPagerIndicator
import com.hoandesign.standby.ui.theme.AccentOrange
import com.hoandesign.standby.ui.theme.NightRed
import com.hoandesign.standby.ui.theme.StandbyBackground
import com.hoandesign.standby.ui.theme.StandbyBorder
import com.hoandesign.standby.ui.theme.StandbyBorderSubtle
import com.hoandesign.standby.ui.theme.StandbyCardBg
import com.hoandesign.standby.ui.theme.StandbyCardBgSecondary
import com.hoandesign.standby.ui.theme.TextPrimary
import com.hoandesign.standby.ui.theme.TextSecondary
import com.hoandesign.standby.ui.theme.TextTertiary
import kotlinx.coroutines.flow.drop

/**
 * Enhanced Dual-Axis Swiping Navigation Engine for StandBy Android.
 *
 * Coordinates:
 * - Page 0 ([StandbyScreen.DUAL_WIDGET]): Dynamic dual-slot bento widgets with:
 *   - Customizable Left and Right widget stacks.
 *   - Edit Mode to add/remove widgets and open the widget picker catalog.
 *   - True Full-Screen Single Slot UI expansion for any widget.
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
    heroClockContent: @Composable () -> Unit,
    nowPlayingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
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

    // When in edit mode, add +1 page at end of stack for "+ Add Widget" card
    val leftPageCount = safeLeftIds.size + if (isEditMode) 1 else 0
    val rightPageCount = safeRightIds.size + if (isEditMode) 1 else 0

    val leftPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { leftPageCount }
    )

    val rightPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { rightPageCount }
    )

    val haptic = LocalHapticFeedback.current

    // Snap haptic feedback on vertical stack page change (Left slot)
    LaunchedEffect(leftPagerState) {
        snapshotFlow { leftPagerState.currentPage }
            .drop(1)
            .collect {
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StandbyBackground)
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
                                AdaptiveStandbyLayout(
                                    slot1 = {
                                        DynamicSlotCard(
                                            slotIndex = 0,
                                            slotTitle = "LEFT BENTO",
                                            widgetIds = safeLeftIds,
                                            pagerState = leftPagerState,
                                            accentColor = accentColor,
                                            isEditMode = isEditMode,
                                            onOpenWidgetPicker = { onOpenWidgetPicker(0) },
                                            onRemoveWidget = { idx -> onRemoveWidgetFromSlot(0, idx) },
                                            onToggleExpand = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                updateNavigationState(navigationState.expandSlot(0))
                                            },
                                            onExitEditMode = onToggleEditMode
                                        )
                                    },
                                    slot2 = {
                                        DynamicSlotCard(
                                            slotIndex = 1,
                                            slotTitle = "RIGHT BENTO",
                                            widgetIds = safeRightIds,
                                            pagerState = rightPagerState,
                                            accentColor = accentColor,
                                            isEditMode = isEditMode,
                                            onOpenWidgetPicker = { onOpenWidgetPicker(1) },
                                            onRemoveWidget = { idx -> onRemoveWidgetFromSlot(1, idx) },
                                            onToggleExpand = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                updateNavigationState(navigationState.expandSlot(1))
                                            },
                                            onExitEditMode = onToggleEditMode
                                        )
                                    },
                                    modifier = Modifier.fillMaxSize()
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
                                                onDoubleTap = {
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
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        heroClockContent()
                    }
                }
                StandbyScreen.NOW_PLAYING -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        nowPlayingContent()
                    }
                }
            }
        }

        // Sleek iOS-style horizontal page indicator anchored at bottom edge
        HorizontalPagerIndicator(
            pageCount = screens.size,
            currentPage = horizontalPagerState.currentPage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}

/**
 * Backward-compatible overload accepting raw composables.
 */
@Composable
fun MainStandbyPager(
    archetype: LayoutArchetype,
    leftSlotWidgets: List<@Composable () -> Unit>,
    rightSlotWidgets: List<@Composable () -> Unit>,
    heroClockContent: @Composable () -> Unit,
    nowPlayingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    initialNavigationState: NavigationState = NavigationState(),
    onNavigationStateChange: ((NavigationState) -> Unit)? = null
) {
    var navigationState by remember { mutableStateOf(initialNavigationState) }
    val screens = StandbyScreen.entries

    val horizontalPagerState = rememberPagerState(
        initialPage = navigationState.currentScreen.ordinal,
        pageCount = { screens.size }
    )

    val safeLeftWidgets = if (leftSlotWidgets.isEmpty()) {
        listOf<@Composable () -> Unit> { Box(Modifier.fillMaxSize()) }
    } else {
        leftSlotWidgets
    }

    val safeRightWidgets = if (rightSlotWidgets.isEmpty()) {
        listOf<@Composable () -> Unit> { Box(Modifier.fillMaxSize()) }
    } else {
        rightSlotWidgets
    }

    val leftPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { safeLeftWidgets.size }
    )

    val rightPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { safeRightWidgets.size }
    )

    val haptic = LocalHapticFeedback.current

    fun updateNavigationState(newState: NavigationState) {
        navigationState = newState
        onNavigationStateChange?.invoke(newState)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StandbyBackground)
    ) {
        HorizontalPager(
            state = horizontalPagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            when (screens[pageIndex]) {
                StandbyScreen.DUAL_WIDGET -> {
                    AdaptiveStandbyLayout(
                        slot1 = {
                            WidgetSlotStack(
                                widgets = safeLeftWidgets,
                                pagerState = leftPagerState,
                                onToggleExpand = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    updateNavigationState(navigationState.expandSlot(0))
                                }
                            )
                        },
                        slot2 = {
                            WidgetSlotStack(
                                widgets = safeRightWidgets,
                                pagerState = rightPagerState,
                                onToggleExpand = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    updateNavigationState(navigationState.expandSlot(1))
                                }
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                StandbyScreen.HERO_CLOCK -> heroClockContent()
                StandbyScreen.NOW_PLAYING -> nowPlayingContent()
            }
        }

        HorizontalPagerIndicator(
            pageCount = screens.size,
            currentPage = horizontalPagerState.currentPage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}

/**
 * Dynamic Bento slot card with vertical pagination across configured widgets,
 * Edit Mode overlay with "+ Add" and "-" remove controls, and Fullscreen toggle.
 */
@Composable
private fun DynamicSlotCard(
    slotIndex: Int,
    slotTitle: String,
    widgetIds: List<StandbyWidgetId>,
    pagerState: PagerState,
    accentColor: Color,
    isEditMode: Boolean,
    onOpenWidgetPicker: () -> Unit,
    onRemoveWidget: (Int) -> Unit,
    onToggleExpand: () -> Unit,
    onExitEditMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onToggleExpand() },
                    onLongPress = { onToggleExpand() }
                )
            }
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            if (pageIndex < widgetIds.size) {
                val widgetId = widgetIds[pageIndex]
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    StandbyWidgetRegistry.RenderCompact(
                        widgetId = widgetId,
                        accentColor = accentColor,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else if (isEditMode) {
                // "+ Add Widget" card at end of stack
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onOpenWidgetPicker() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Widget",
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "Add Widget",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Subtle vertical pager dots indicator
        if (widgetIds.size > 1 && !isEditMode) {
            VerticalPagerIndicator(
                pageCount = widgetIds.size,
                currentPage = pagerState.currentPage.coerceIn(0, widgetIds.size - 1),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            )
        }

        // Edit Mode overlay (Header, Delete badge, Add button, Fullscreen button)
        if (isEditMode) {
            // Header Bar
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = slotTitle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "(${widgetIds.size})",
                        fontSize = 10.sp,
                        color = TextTertiary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // + Add button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor)
                            .clickable { onOpenWidgetPicker() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.Black,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Add",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    // Fullscreen button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(StandbyCardBgSecondary)
                            .border(1.dp, StandbyBorderSubtle, RoundedCornerShape(10.dp))
                            .clickable { onToggleExpand() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Expand Fullscreen",
                            tint = TextPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Remove button on active widget (disabled if only 1 widget left)
            if (pagerState.currentPage < widgetIds.size && widgetIds.size > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(CircleShape)
                        .background(NightRed)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onRemoveWidget(pagerState.currentPage)
                        }
                        .size(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Remove Widget",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Hosts an independent vertical widget stack with snap scrolling.
 */
@Composable
private fun WidgetSlotStack(
    widgets: List<@Composable () -> Unit>,
    pagerState: PagerState,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onToggleExpand() },
                    onLongPress = { onToggleExpand() }
                )
            }
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            if (pageIndex in widgets.indices) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    widgets[pageIndex]()
                }
            }
        }

        if (widgets.size > 1) {
            VerticalPagerIndicator(
                pageCount = widgets.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            )
        }
    }
}
