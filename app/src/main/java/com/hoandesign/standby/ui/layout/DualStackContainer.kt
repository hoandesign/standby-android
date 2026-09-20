package com.hoandesign.standby.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hoandesign.standby.model.LayoutArchetype
import com.hoandesign.standby.ui.theme.StandbyBorder
import com.hoandesign.standby.ui.theme.StandbyCardBg

/**
 * Reusable container for StandBy cards.
 * In Borderless Bento mode (default), modules float borderless on pure OLED black,
 * exactly matching Apple StandBy hardware presentation.
 */
@Composable
fun StandbyCardContainer(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    contentAlignment: Alignment = Alignment.Center,
    borderless: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val containerModifier = if (borderless) {
        modifier
            .clip(shape)
            .background(Color.Transparent)
    } else {
        modifier
            .clip(shape)
            .background(StandbyCardBg)
            .border(
                width = 1.dp,
                color = StandbyBorder,
                shape = shape
            )
    }
    Box(
        modifier = containerModifier,
        contentAlignment = contentAlignment
    ) {
        content()
    }
}

/**
 * Dual-slot container supporting landscape side-by-side flex slots and tall portrait 4:5 stacked slots.
 *
 * - When [archetype.isLandscape]: Side-by-side Row with 2 flex slots, ratio-scaled horizontal padding,
 *   and subtle center divider/gutter so widescreen never feels disconnected or empty.
 * - When [archetype] is [LayoutArchetype.TALL_PORTRAIT]: Column with Top slot (4/9 height) and Bottom slot
 *   (5/9 height) with 4:5 vertical proportions, filling the tall screen height symmetrically without dead space.
 * - Styled using [StandbyCardBg], [StandbyBorder], and rounded corners (20.dp - 24.dp).
 */
@Composable
fun DualStackContainer(
    archetype: LayoutArchetype,
    slot1: @Composable () -> Unit,
    slot2: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLandscape = archetype.isLandscape || archetype == LayoutArchetype.SQUARISH_FOLDABLE
    val cornerRadius = if (archetype == LayoutArchetype.SQUARISH_FOLDABLE) 20.dp else 24.dp

    if (isLandscape) {
        val horizontalPadding = when (archetype) {
            LayoutArchetype.ULTRA_TALL_LANDSCAPE -> 24.dp
            LayoutArchetype.STANDARD_LANDSCAPE -> 16.dp
            LayoutArchetype.SQUARISH_FOLDABLE -> 12.dp
            else -> 16.dp
        }
        val verticalPadding = when (archetype) {
            LayoutArchetype.SQUARISH_FOLDABLE -> 8.dp
            else -> 10.dp
        }
        val gutterWidth = when (archetype) {
            LayoutArchetype.SQUARISH_FOLDABLE -> 12.dp
            else -> 16.dp
        }

        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            horizontalArrangement = Arrangement.spacedBy(gutterWidth),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StandbyCardContainer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                cornerRadius = cornerRadius
            ) {
                slot1()
            }

            StandbyCardContainer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                cornerRadius = cornerRadius
            ) {
                slot2()
            }
        }
    } else {
        // TALL_PORTRAIT: Symmetrical, balanced vertical dual cards
        val gutterHeight = 14.dp
        val portraitCornerRadius = 28.dp

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(gutterHeight),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StandbyCardContainer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                cornerRadius = portraitCornerRadius
            ) {
                slot1()
            }

            StandbyCardContainer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                cornerRadius = portraitCornerRadius
            ) {
                slot2()
            }
        }
    }
}
