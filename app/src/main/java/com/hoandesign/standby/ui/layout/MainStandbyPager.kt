package com.hoandesign.standby.ui.layout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.draw.shadow
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.Restore
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StandbyBackground)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { recordInteraction() }
                )
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
                                            onExitEditMode = {
                                                recordInteraction()
                                                onToggleEditMode()
                                            },
                                            onReorderWidgets = { from, to ->
                                                recordInteraction()
                                                onReorderSlotWidgets(0, from, to)
                                            },
                                            onResetDefaults = {
                                                recordInteraction()
                                                onResetSlotDefaults(0)
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
                                            onExitEditMode = {
                                                recordInteraction()
                                                onToggleEditMode()
                                            },
                                            onReorderWidgets = { from, to ->
                                                recordInteraction()
                                                onReorderSlotWidgets(1, from, to)
                                            },
                                            onResetDefaults = {
                                                recordInteraction()
                                                onResetSlotDefaults(1)
                                            },
                                            onUserInteraction = { recordInteraction() }
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
            visible = isNavVisible || isEditMode,
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
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp, start = 16.dp, end = 16.dp)
        )

        // Subtle Top Pill Handle (when navigation is hidden, gives subtle hint to tap to show)
        AnimatedVisibility(
            visible = !isNavVisible && !isEditMode,
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
            visible = isNavVisible || isEditMode,
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
    onReorderWidgets: ((Int, Int) -> Unit)? = null,
    onResetDefaults: (() -> Unit)? = null,
    onUserInteraction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                if (!isEditMode) {
                    detectTapGestures(
                        onTap = { onUserInteraction() },
                        onDoubleTap = {
                            onUserInteraction()
                            onToggleExpand()
                        },
                        onLongPress = {
                            onUserInteraction()
                            onToggleExpand()
                        }
                    )
                }
            }
    ) {
        if (isEditMode) {
            EditModeWidgetStack(
                widgetIds = widgetIds,
                accentColor = accentColor,
                onRemoveWidget = onRemoveWidget,
                onOpenWidgetPicker = onOpenWidgetPicker,
                onReorder = { from, to -> onReorderWidgets?.invoke(from, to) }
            )
        } else {
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
                }
            }
        }

        if (widgetIds.size > 1 && !isEditMode) {
            VerticalPagerIndicator(
                pageCount = widgetIds.size,
                currentPage = pagerState.currentPage.coerceIn(0, widgetIds.size - 1),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            )
        }

        // Edit Mode overlay (Header)
        if (isEditMode) {
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
                    // Restore default button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(StandbyCardBgSecondary)
                            .border(1.dp, StandbyBorderSubtle, RoundedCornerShape(10.dp))
                            .clickable { onResetDefaults?.invoke() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(imageVector = Icons.Default.Restore, contentDescription = "Reset", tint = TextPrimary, modifier = Modifier.size(12.dp))
                            Text(text = "Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
        }
    }
}

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

/**
 * Auto-hiding top navigation menu floating over the fullscreen Bento box.
 *
 * Provides:
 * - STANDBY brand micro-label / EDITING status badge.
 * - Quick-jump tabs for [ Bento ] [ Clock ] [ Music ] screens.
 * - Contextual action controls: [ Done ] during Edit Mode; [ Edit ] and [ Settings ] during viewing mode.
 * - Smooth slide-in/slide-out animations and auto-hide on inactivity.
 */
@Composable
private fun StandbyTopNavigationMenu(
    visible: Boolean,
    currentScreen: StandbyScreen,
    isEditMode: Boolean,
    accentColor: Color,
    onSelectScreen: (StandbyScreen) -> Unit,
    onToggleEditMode: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
        modifier = modifier.zIndex(20f)
    ) {
        if (isEditMode) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(StandbyCardBgSecondary.copy(alpha = 0.94f))
                    .border(1.dp, StandbyBorderSubtle, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "EDITING BENTO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = accentColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(accentColor)
                            .clickable { onToggleEditMode() }
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Done",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(StandbyCardBgSecondary.copy(alpha = 0.94f))
                    .border(1.dp, StandbyBorderSubtle, RoundedCornerShape(22.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Left: STANDBY brand title
                    Text(
                        text = "STANDBY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp,
                        color = TextTertiary
                    )

                    // Center: Screen Navigation Tabs ([ Bento ] [ Clock ] [ Music ])
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x22FFFFFF))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StandbyScreen.entries.forEach { screen ->
                            val isSelected = currentScreen == screen
                            val tabTitle = when (screen) {
                                StandbyScreen.DUAL_WIDGET -> "Bento"
                                StandbyScreen.HERO_CLOCK -> "Clock"
                                StandbyScreen.NOW_PLAYING -> "Music"
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) StandbyCardBg.copy(alpha = 0.95f) else Color.Transparent
                                    )
                                    .border(
                                        width = if (isSelected) 0.5.dp else 0.dp,
                                        color = if (isSelected) StandbyBorderSubtle else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSelectScreen(screen) }
                                    .padding(horizontal = 9.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = tabTitle,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) TextPrimary else TextSecondary
                                )
                            }
                        }
                    }

                    // Right: Contextual Controls (Edit button when on Bento page, Settings icon)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (currentScreen == StandbyScreen.DUAL_WIDGET) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x22FFFFFF))
                                    .clickable { onToggleEditMode() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Customize",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Edit",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        // Settings Icon
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0x22FFFFFF))
                                .clickable { onOpenSettings() }
                                .padding(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun EditModeWidgetStack(
    widgetIds: List<StandbyWidgetId>,
    accentColor: Color,
    onRemoveWidget: (Int) -> Unit,
    onOpenWidgetPicker: () -> Unit,
    onReorder: (Int, Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
    
    val listState = rememberLazyListState()
    
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 38.dp, bottom = 36.dp, start = 10.dp, end = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 100.dp)
    ) {
        itemsIndexed(widgetIds, key = { _, id -> id.name }) { index, widgetId ->
            val isDragged = draggedIndex == index
            
            val scale by animateFloatAsState(
                targetValue = if (isDragged) 1.04f else 1f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
            val elevation by animateFloatAsState(
                targetValue = if (isDragged) 16f else 0f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
            val yOffset = if (isDragged) dragOffset else 0f
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Approximate height for testing
                    .zIndex(if (isDragged) 1f else 0f)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationY = yOffset
                        shadowElevation = elevation
                        shape = RoundedCornerShape(24.dp)
                        clip = true
                    }
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                draggedIndex = index
                                dragOffset = 0f
                            },
                            onDragEnd = {
                                draggedIndex = null
                                dragOffset = 0f
                            },
                            onDragCancel = {
                                draggedIndex = null
                                dragOffset = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount.y
                                
                                // Simple swap logic
                                val itemHeight = 200.dp.toPx() + 16.dp.toPx()
                                val targetSwap = (dragOffset / itemHeight).toInt()
                                if (targetSwap != 0) {
                                    val newIndex = (index + targetSwap).coerceIn(0, widgetIds.size - 1)
                                    if (newIndex != index) {
                                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                        onReorder(index, newIndex)
                                        draggedIndex = newIndex
                                        dragOffset -= targetSwap * itemHeight
                                    }
                                }
                            }
                        )
                    }
            ) {
                StandbyWidgetRegistry.RenderCompact(
                    widgetId = widgetId,
                    accentColor = accentColor,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Remove button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(com.hoandesign.standby.ui.theme.NightRed)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (widgetIds.size > 1) {
                                onRemoveWidget(index)
                            } else {
                                Toast.makeText(context, "Cannot remove the last widget", Toast.LENGTH_SHORT).show()
                            }
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
        
        // Add Widget Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(com.hoandesign.standby.ui.theme.StandbyCardBgSecondary)
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
                        color = com.hoandesign.standby.ui.theme.TextPrimary
                    )
                }
            }
        }
    }
}
