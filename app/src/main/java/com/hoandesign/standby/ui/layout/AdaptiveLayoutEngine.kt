package com.hoandesign.standby.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hoandesign.standby.model.LayoutArchetype
import com.hoandesign.standby.model.LayoutDimensions
import com.hoandesign.standby.model.calculateLayoutDimensions
import com.hoandesign.standby.model.determineLayoutArchetype
import com.hoandesign.standby.ui.theme.StandbyBackground

/**
 * Main adaptive layout engine for StandBy Android.
 *
 * Uses [BoxWithConstraints] to dynamically measure available window width and height,
 * calculates the active [LayoutArchetype], and renders the appropriate layout container:
 * - Landscape displays: [DualStackContainer] side-by-side with ratio-scaled padding.
 * - Tall portrait displays: [DualStackContainer] stacked top/bottom with 4:5 vertical proportions.
 * - Squarish foldable displays: [QuadBentoContainer] 2x2 grid if 4 slots are provided,
 *   or [DualStackContainer] if only 2 primary slots are provided.
 */
@Composable
fun AdaptiveStandbyLayout(
    slot1: @Composable () -> Unit,
    slot2: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    slot3: (@Composable () -> Unit)? = null,
    slot4: (@Composable () -> Unit)? = null
) {
    AdaptiveStandbyLayout(modifier = modifier) { archetype, _ ->
        if (archetype == LayoutArchetype.SQUARISH_FOLDABLE && slot3 != null && slot4 != null) {
            QuadBentoContainer(
                slotTopLeft = slot1,
                slotTopRight = slot2,
                slotBottomLeft = slot3,
                slotBottomRight = slot4,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            DualStackContainer(
                archetype = archetype,
                slot1 = slot1,
                slot2 = slot2,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Flexible content overload of [AdaptiveStandbyLayout] providing direct access to the
 * measured [LayoutArchetype] and [LayoutDimensions].
 */
@Composable
fun AdaptiveStandbyLayout(
    modifier: Modifier = Modifier,
    content: @Composable (archetype: LayoutArchetype, dimensions: LayoutDimensions) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(StandbyBackground)
    ) {
        val widthDp = maxWidth.value
        val heightDp = maxHeight.value
        val dimensions = calculateLayoutDimensions(widthDp, heightDp)

        content(dimensions.archetype, dimensions)
    }
}
