package com.hoandesign.standby.ui.layout

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
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
import androidx.compose.ui.draw.shadow
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
import com.hoandesign.standby.ui.theme.StandbyBorderSubtle
import com.hoandesign.standby.ui.theme.StandbyCardBgSecondary
import com.hoandesign.standby.ui.theme.TextPrimary
import com.hoandesign.standby.ui.theme.TextSecondary

/**
 * Edit mode list displaying the stack of widgets in a slot.
 *
 * Built to Apple StandBy / watchOS Smart Stack standards:
 * - Subtle iOS-style jiggle animation (±0.8° to ±1.0° wobble with phase offset).
 * - Floating top-left remove badge with high-contrast white ring and drop shadow.
 * - Non-colliding widget container scaled to 0.93f so headers ("SEPTEMBER", "BATTERY")
 *   are never covered or truncated.
 * - Glassmorphic drag handle affordance on right edge.
 * - Drag-and-drop reordering with spring elevation and haptics.
 * - Polished "+ Add Widget" glass card.
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
    val infiniteTransition = rememberInfiniteTransition(label = "editModeJiggle")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp)
        ) {
            itemsIndexed(widgetIds, key = { _, id -> id.name }) { index, widgetId ->
                val isDragged = draggedIndex == index

                // Apple StandBy jiggle animation: alternating wobble with index phase variation
                val jiggleAngle by infiniteTransition.animateFloat(
                    initialValue = if (index % 2 == 0) -0.85f else 0.85f,
                    targetValue = if (index % 2 == 0) 0.85f else -0.85f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 130 + (index % 3) * 20,
                            easing = LinearEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "jiggle_$index"
                )

                val scale by animateFloatAsState(
                    targetValue = if (isDragged) 1.04f else 0.93f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "dragScale"
                )
                val elevation by animateFloatAsState(
                    targetValue = if (isDragged) 20f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "dragElevation"
                )
                val yOffset = if (isDragged) dragOffset else 0f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(184.dp)
                        .zIndex(if (isDragged) 10f else 1f)
                ) {
                    // Widget Card Container
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationY = yOffset
                                rotationZ = if (isDragged) 0f else jiggleAngle
                                shadowElevation = elevation
                                shape = RoundedCornerShape(22.dp)
                                clip = true
                            }
                            .border(
                                width = if (isDragged) 1.5.dp else 1.dp,
                                color = if (isDragged) accentColor else StandbyBorderSubtle,
                                shape = RoundedCornerShape(22.dp)
                            )
                            .background(StandbyCardBgSecondary, RoundedCornerShape(22.dp))
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
                                        val itemHeight = currentItemInfo?.size?.toFloat() ?: (184.dp.toPx() + 16.dp.toPx())
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
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(end = 24.dp) // breathing room for drag handle
                        )

                        // Subtle Drag Handle Affordance on the right edge
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 10.dp)
                                .size(width = 24.dp, height = 36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DragHandle,
                                contentDescription = "Drag to reorder",
                                tint = Color(0x99FFFFFF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Floating Apple-Style Remove (-) Badge
                    // Positioned at top-left, floating outside card corner with white ring and drop shadow
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 2.dp, y = (-2).dp)
                            .zIndex(20f)
                            .graphicsLayer {
                                translationY = yOffset
                            }
                            .shadow(elevation = 6.dp, shape = CircleShape)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF3B30)) // Apple Vibrant System Red
                            .border(2.dp, Color.White, CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (widgetIds.size > 1) {
                                    onRemoveWidget(index)
                                } else {
                                    Toast.makeText(context, "At least 1 widget required in stack", Toast.LENGTH_SHORT).show()
                                }
                            },
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

            // Polished "+ Add Widget" Glass Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .graphicsLayer {
                            scaleX = 0.93f
                            scaleY = 0.93f
                        }
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0x18FFFFFF))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(22.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            onOpenWidgetPicker()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.22f)),
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
                        Text(
                            text = "Browse 15 StandBy modules",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
