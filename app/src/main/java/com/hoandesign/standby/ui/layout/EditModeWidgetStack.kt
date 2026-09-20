package com.hoandesign.standby.ui.layout

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.hoandesign.standby.model.StandbyWidgetId
import com.hoandesign.standby.model.StandbyWidgetRegistry
import com.hoandesign.standby.ui.theme.NightRed
import com.hoandesign.standby.ui.theme.StandbyBorderSubtle
import com.hoandesign.standby.ui.theme.StandbyCardBgSecondary
import com.hoandesign.standby.ui.theme.TextPrimary
import com.hoandesign.standby.ui.theme.TextSecondary

/**
 * Edit mode list displaying the stack of widgets in a slot.
 *
 * Supports:
 * - Drag-and-drop reordering with long-press elevation and haptic feedback.
 * - Prominent remove badge with minimum-1 guard.
 * - "+ Add Widget" card at end of stack to open the catalog.
 */
@Composable
fun EditModeWidgetStack(
    slotTitle: String,
    widgetIds: List<StandbyWidgetId>,
    accentColor: Color,
    onRemoveWidget: (Int) -> Unit,
    onOpenWidgetPicker: () -> Unit,
    onReorder: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    val listState = rememberLazyListState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
        ) {
            itemsIndexed(widgetIds, key = { _, id -> id.name }) { index, widgetId ->
                val isDragged = draggedIndex == index

                val scale by animateFloatAsState(
                    targetValue = if (isDragged) 1.04f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "dragScale"
                )
                val elevation by animateFloatAsState(
                    targetValue = if (isDragged) 16f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "dragElevation"
                )
                val yOffset = if (isDragged) dragOffset else 0f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .zIndex(if (isDragged) 2f else 0f)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationY = yOffset
                            shadowElevation = elevation
                            shape = RoundedCornerShape(20.dp)
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

                                    val currentItemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                                    val itemHeight = currentItemInfo?.size?.toFloat() ?: (180.dp.toPx() + 14.dp.toPx())
                                    if (itemHeight > 0f) {
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
                                }
                            )
                        }
                ) {
                    StandbyWidgetRegistry.RenderCompact(
                        widgetId = widgetId,
                        accentColor = accentColor,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Remove (-) badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(NightRed)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (widgetIds.size > 1) {
                                    onRemoveWidget(index)
                                } else {
                                    Toast.makeText(context, "At least 1 widget required in stack", Toast.LENGTH_SHORT).show()
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
                        .height(110.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(StandbyCardBgSecondary)
                        .border(1.dp, StandbyBorderSubtle, RoundedCornerShape(20.dp))
                        .clickable { onOpenWidgetPicker() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Widget",
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = "Add Widget",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}
