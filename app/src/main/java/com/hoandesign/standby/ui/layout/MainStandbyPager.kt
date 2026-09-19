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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.hoandesign.standby.model.LayoutArchetype
import com.hoandesign.standby.model.NavigationState
import com.hoandesign.standby.model.StandbyScreen
import com.hoandesign.standby.model.WidgetDisplayMode
import com.hoandesign.standby.model.collapseToDual
import com.hoandesign.standby.model.expandSlot
import com.hoandesign.standby.model.setScreen
import com.hoandesign.standby.model.toggleDisplayMode
import com.hoandesign.standby.ui.components.HorizontalPagerIndicator
import com.hoandesign.standby.ui.components.VerticalPagerIndicator
import com.hoandesign.standby.ui.theme.StandbyBackground
import kotlinx.coroutines.flow.drop

/**
 * Root Dual-Axis Swiping Navigation Engine for StandBy Android.
 *
 * Coordinates horizontal navigation across high-level StandBy screens:
 * - Page 0 ([StandbyScreen.DUAL_WIDGET]): Adaptive dual-slot widgets with independent vertical
 *   stack paging, snap haptics, and single/dual toggle animation.
 * - Page 1 ([StandbyScreen.HERO_CLOCK]): Immersive full-screen Hero Clock suite.
 * - Page 2 ([StandbyScreen.NOW_PLAYING]): Full-screen Media & Music playback interface.
 *
 * @param archetype Active layout archetype determining responsive proportions, padding, and corner radius.
 * @param leftSlotWidgets List of composable widgets hosted in the primary/left vertical stack slot.
 * @param rightSlotWidgets List of composable widgets hosted in the secondary/right vertical stack slot.
 * @param heroClockContent Composable content rendered on the Hero Clock screen.
 * @param nowPlayingContent Composable content rendered on the Now Playing media screen.
 * @param modifier Modifier applied to the root container.
 * @param initialNavigationState Initial navigation state (screen, display mode, expanded slot).
 * @param onNavigationStateChange Optional listener for navigation state updates.
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
                            WidgetDisplayMode.SINGLE_EXPANDED -> {
                                val isLeftSlot = navigationState.expandedSlotIndex == 0
                                val activeWidgets = if (isLeftSlot) safeLeftWidgets else safeRightWidgets
                                val activePagerState = if (isLeftSlot) leftPagerState else rightPagerState

                                val horizontalPadding = when (archetype) {
                                    LayoutArchetype.ULTRA_TALL_LANDSCAPE -> 24.dp
                                    LayoutArchetype.STANDARD_LANDSCAPE -> 16.dp
                                    LayoutArchetype.SQUARISH_FOLDABLE -> 12.dp
                                    LayoutArchetype.TALL_PORTRAIT -> 16.dp
                                }
                                val verticalPadding = when (archetype) {
                                    LayoutArchetype.SQUARISH_FOLDABLE -> 12.dp
                                    else -> 16.dp
                                }
                                val cornerRadius = if (archetype == LayoutArchetype.SQUARISH_FOLDABLE) 20.dp else 24.dp

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                                ) {
                                    StandbyCardContainer(
                                        modifier = Modifier.fillMaxSize(),
                                        cornerRadius = cornerRadius
                                    ) {
                                        WidgetSlotStack(
                                            widgets = activeWidgets,
                                            pagerState = activePagerState,
                                            onToggleExpand = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                updateNavigationState(navigationState.collapseToDual())
                                            }
                                        )
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
 * Hosts an independent vertical widget stack with snap scrolling, subtle vertical position dots,
 * and double-tap / long-press expansion gesture detection.
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
