package com.hoandesign.standby.ui.layout

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.hoandesign.standby.model.StandbyWidgetId
import com.hoandesign.standby.model.StandbyWidgetRegistry
import com.hoandesign.standby.ui.components.VerticalPagerIndicator

/**
 * Dynamic Bento slot card with vertical pagination across configured widgets,
 * Edit Mode overlay with "+ Add" and "-" remove controls, and Fullscreen toggle.
 */
@Composable
fun DynamicSlotCard(
    slotIndex: Int,
    slotTitle: String,
    widgetIds: List<StandbyWidgetId>,
    pagerState: PagerState,
    accentColor: Color,
    isEditMode: Boolean,
    onOpenWidgetPicker: () -> Unit,
    onRemoveWidget: (Int) -> Unit,
    onToggleExpand: () -> Unit,
    onReorderWidgets: ((Int, Int) -> Unit)? = null,
    onUserInteraction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(isEditMode) {
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
                slotTitle = slotTitle,
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
                if (pageIndex in widgetIds.indices) {
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
    }
}
