package com.hoandesign.standby.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 2x2 grid container designed specifically for unfolded squarish foldable displays
 * (e.g., Galaxy Z Fold 8, Honor Magic V3, OnePlus Open), filling the 4 quadrants
 * with zero wasted space or pillarboxing.
 *
 * Styled using StandbyCardBg, StandbyBorder, and smooth rounded corners.
 */
@Composable
fun QuadBentoContainer(
    slotTopLeft: @Composable () -> Unit,
    slotTopRight: @Composable () -> Unit,
    slotBottomLeft: @Composable () -> Unit,
    slotBottomRight: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    spacing: Dp = 12.dp
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            StandbyCardContainer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                cornerRadius = cornerRadius
            ) {
                slotTopLeft()
            }

            StandbyCardContainer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                cornerRadius = cornerRadius
            ) {
                slotTopRight()
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            StandbyCardContainer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                cornerRadius = cornerRadius
            ) {
                slotBottomLeft()
            }

            StandbyCardContainer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                cornerRadius = cornerRadius
            ) {
                slotBottomRight()
            }
        }
    }
}
