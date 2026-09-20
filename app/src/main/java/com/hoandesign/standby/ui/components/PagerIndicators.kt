package com.hoandesign.standby.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
 * iOS-style horizontal pager indicator with smooth pill-shaped animations
 * and translucent frosted capsule backdrop for guaranteed contrast.
 *
 * @param pageCount Total number of horizontal pages.
 * @param currentPage Zero-based index of the currently active page.
 * @param modifier Modifier applied to the indicator container.
 * @param activeColor Color of the selected indicator pill.
 * @param inactiveColor Color of inactive indicator dots.
 * @param indicatorHeight Height of the indicator pills/dots.
 * @param activeWidth Width of the selected elongated pill.
 * @param inactiveWidth Width of inactive circular dots.
 * @param spacing Spacing between adjacent indicator pills.
 * @param hasBackdrop Whether to encapsulate the dots in a frosted glass capsule.
 */
@Composable
fun HorizontalPagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.White.copy(alpha = 0.35f),
    indicatorHeight: Dp = 5.dp,
    activeWidth: Dp = 16.dp,
    inactiveWidth: Dp = 5.dp,
    spacing: Dp = 5.dp,
    hasBackdrop: Boolean = true
) {
    if (pageCount <= 0) return

    val contentModifier = if (hasBackdrop) {
        modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .border(0.5.dp, Color(0x33FFFFFF), CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    } else {
        modifier
    }

    Row(
        modifier = contentModifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isSelected) activeWidth else inactiveWidth,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "horizontal_indicator_width_$index"
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) activeColor else inactiveColor,
                animationSpec = tween(durationMillis = 200),
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
 * Subtle vertical indicator showing position within a vertical widget stack.
 *
 * Anchored to the outer edge/gutter of a widget slot, it provides feedback
 * with sleek animated vertical stretch on the active dot, protected by
 * a frosted dark capsule backdrop.
 *
 * @param pageCount Total number of widgets in the vertical stack.
 * @param currentPage Zero-based index of the currently active widget in the stack.
 * @param modifier Modifier applied to the indicator container.
 * @param activeColor Color of the selected active dot.
 * @param inactiveColor Color of inactive dots in the stack.
 * @param dotWidth Width of the indicator dots.
 * @param activeHeight Height of the active elongated dot.
 * @param inactiveHeight Height of inactive dots.
 * @param spacing Spacing between adjacent vertical dots.
 * @param hasBackdrop Whether to encapsulate the dots in a frosted glass capsule.
 */
@Composable
fun VerticalPagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.White.copy(alpha = 0.35f),
    dotWidth: Dp = 4.dp,
    activeHeight: Dp = 14.dp,
    inactiveHeight: Dp = 4.dp,
    spacing: Dp = 4.dp,
    hasBackdrop: Boolean = true
) {
    if (pageCount <= 0) return

    val contentModifier = if (hasBackdrop) {
        modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .border(0.5.dp, Color(0x33FFFFFF), CircleShape)
            .padding(horizontal = 4.dp, vertical = 6.dp)
    } else {
        modifier
    }

    Column(
        modifier = contentModifier,
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val height by animateDpAsState(
                targetValue = if (isSelected) activeHeight else inactiveHeight,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "vertical_indicator_height_$index"
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) activeColor else inactiveColor,
                animationSpec = tween(durationMillis = 200),
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
