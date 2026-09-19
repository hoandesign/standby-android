package com.hoandesign.standby.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * iOS-style horizontal pager indicator with smooth pill-shaped animations.
 *
 * The active page is rendered as an elongated, bright pill, while inactive
 * pages remain subtle circular dots with dim opacity.
 *
 * @param pageCount Total number of horizontal pages.
 * @param currentPage Zero-based index of the currently active page.
 * @param modifier Modifier applied to the indicator container row.
 * @param activeColor Color of the selected indicator pill.
 * @param inactiveColor Color of inactive indicator dots (defaults to 0.3 opacity white).
 * @param indicatorHeight Height of the indicator pills/dots.
 * @param activeWidth Width of the selected elongated pill.
 * @param inactiveWidth Width of inactive circular dots.
 * @param spacing Spacing between adjacent indicator pills.
 */
@Composable
fun HorizontalPagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.White.copy(alpha = 0.3f),
    indicatorHeight: Dp = 6.dp,
    activeWidth: Dp = 18.dp,
    inactiveWidth: Dp = 6.dp,
    spacing: Dp = 6.dp
) {
    if (pageCount <= 0) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isSelected) activeWidth else inactiveWidth,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                label = "horizontal_indicator_width_$index"
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) activeColor else inactiveColor,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                label = "horizontal_indicator_color_$index"
            )

            Box(
                modifier = Modifier
                    .size(width = width, height = indicatorHeight)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

/**
 * Subtle vertical dot indicator showing position within a vertical widget stack.
 *
 * Anchored to the side of a widget slot, it provides visual feedback
 * for smart stack scrolling with sleek animated vertical stretch on the active dot.
 *
 * @param pageCount Total number of widgets in the vertical stack.
 * @param currentPage Zero-based index of the currently active widget in the stack.
 * @param modifier Modifier applied to the indicator column.
 * @param activeColor Color of the selected active dot.
 * @param inactiveColor Color of inactive dots in the stack.
 * @param dotWidth Width of the indicator dots.
 * @param activeHeight Height of the active elongated dot.
 * @param inactiveHeight Height of inactive dots.
 * @param spacing Spacing between adjacent vertical dots.
 */
@Composable
fun VerticalPagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White.copy(alpha = 0.9f),
    inactiveColor: Color = Color.White.copy(alpha = 0.25f),
    dotWidth: Dp = 5.dp,
    activeHeight: Dp = 12.dp,
    inactiveHeight: Dp = 5.dp,
    spacing: Dp = 5.dp
) {
    if (pageCount <= 0) return

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val height by animateDpAsState(
                targetValue = if (isSelected) activeHeight else inactiveHeight,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                label = "vertical_indicator_height_$index"
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) activeColor else inactiveColor,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                label = "vertical_indicator_color_$index"
            )

            Box(
                modifier = Modifier
                    .size(width = dotWidth, height = height)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
