package com.hoandesign.standby.ui.widgets.calendar

import android.app.KeyguardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.hoandesign.standby.data.CalendarRepository
import com.hoandesign.standby.model.CalendarEvent
import com.hoandesign.standby.model.formatEventCountdown
import com.hoandesign.standby.ui.theme.NightRed
import com.hoandesign.standby.ui.theme.NightRedDim
import com.hoandesign.standby.ui.theme.OledBlack
import com.hoandesign.standby.ui.theme.StandbyBorder
import com.hoandesign.standby.ui.theme.StandbyCardBgSecondary
import com.hoandesign.standby.ui.theme.StandbyTheme
import com.hoandesign.standby.ui.theme.TextPrimary
import com.hoandesign.standby.ui.theme.TextSecondary
import com.hoandesign.standby.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Agenda Widget presenting an upcoming schedule timeline.
 *
 * Features:
 * - Prominent "Next Meeting" banner featuring a real-time countdown pill ("In 20m", "In 1h 15m").
 * - Vertical timeline items with colored category bars, meeting titles, time ranges, and locations.
 * - Automatic live countdown ticking and full adaptation to Red Night Mode.
 *
 * @param modifier Root modifier.
 * @param events Custom list of events; defaults to [CalendarRepository.getUpcomingEvents].
 * @param onEventClick Optional callback when an event card is clicked.
 */
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Composable
fun ScheduleWidget(
    modifier: Modifier = Modifier,
    events: List<CalendarEvent>? = null,
    onEventClick: ((CalendarEvent) -> Unit)? = null
) {
    val context = LocalContext.current
    val repository = remember(context) { CalendarRepository(context) }
    
    val keyguardManager = remember(context) { context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager }
    val isLocked = remember(context) { keyguardManager.isDeviceLocked }
    
    var hasPermission by remember { mutableStateOf(repository.hasCalendarPermission()) }
    val calendarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    val eventList = events ?: remember(context, hasPermission, isLocked) {
        if (isLocked || !hasPermission) emptyList() else repository.getUpcomingEvents()
    }

    val isNightMode = StandbyTheme.isNightMode

    // Periodically update current epoch to keep countdown pill accurate
    val currentEpoch by produceState(initialValue = System.currentTimeMillis()) {
        while (isActive) {
            delay(15_000L) // Refresh countdown every 15 seconds
            value = System.currentTimeMillis()
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(14.dp)
    ) {
        if (eventList.isEmpty()) {
            EmptyScheduleView(
                isNightMode = isNightMode,
                isLocked = isLocked,
                hasPermission = hasPermission,
                onRequestPermission = {
                    calendarLauncher.launch(Manifest.permission.READ_CALENDAR)
                }
            )
        } else {
            val nextEvent = eventList.first()
            val timelineEvents = eventList.drop(1)

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Next Meeting Banner with Countdown Pill
                NextEventBanner(
                    event = nextEvent,
                    currentEpoch = currentEpoch,
                    isNightMode = isNightMode,
                    onClick = { onEventClick?.invoke(nextEvent) }
                )

                // Timeline List of Upcoming Events
                if (timelineEvents.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(timelineEvents, key = { it.id }) { event ->
                            TimelineEventRow(
                                event = event,
                                isNightMode = isNightMode,
                                onClick = { onEventClick?.invoke(event) }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No more events today",
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontSize = 12.sp,
                                color = if (isNightMode) Color(0x66FF453A) else TextTertiary
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Prominent banner card highlighting the next immediate meeting.
 */
@Composable
private fun NextEventBanner(
    event: CalendarEvent,
    currentEpoch: Long,
    isNightMode: Boolean,
    onClick: () -> Unit
) {
    val countdownText = formatEventCountdown(event.startEpochMillis, currentEpoch)
    val bannerBg = if (isNightMode) Color(0x22FF453A) else StandbyCardBgSecondary
    val bannerBorder = if (isNightMode) Color(0x44FF453A) else StandbyBorder
    val activeColor = if (isNightMode) NightRed else event.color

    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bannerBg)
            .border(1.dp, bannerBorder, shape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Top Header: Label + Countdown Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "UP NEXT",
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.08.em,
                    color = if (isNightMode) NightRed else TextTertiary
                )
            )

            // Countdown Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isNightMode) NightRedDim else activeColor.copy(alpha = 0.22f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = countdownText,
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = activeColor
                    )
                )
            }
        }

        // Title
        Text(
            text = event.title,
            style = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (isNightMode) NightRed else TextPrimary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Time Range & Location
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${event.startTimeFormatted} - ${event.endTimeFormatted}",
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = if (isNightMode) Color(0xAAFF453A) else TextSecondary
                )
            )

            if (!event.location.isNullOrBlank()) {
                Text(
                    text = "·",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 12.sp,
                        color = if (isNightMode) Color(0x66FF453A) else TextTertiary
                    )
                )
                Text(
                    text = event.location,
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = if (isNightMode) Color(0x88FF453A) else TextTertiary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Individual timeline item in the vertical agenda schedule.
 */
@Composable
private fun TimelineEventRow(
    event: CalendarEvent,
    isNightMode: Boolean,
    onClick: () -> Unit
) {
    val barColor = if (isNightMode) NightRed else event.color

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical Category Bar
        Box(
            modifier = Modifier
                .width(3.5.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(barColor)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = event.title,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = if (isNightMode) Color(0xEEFF453A) else TextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${event.startTimeFormatted} - ${event.endTimeFormatted}",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 11.sp,
                        color = if (isNightMode) Color(0x99FF453A) else TextSecondary
                    )
                )

                if (!event.location.isNullOrBlank()) {
                    Text(
                        text = "·",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 11.sp,
                            color = if (isNightMode) Color(0x55FF453A) else TextTertiary
                        )
                    )
                    Text(
                        text = event.location,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 11.sp,
                            color = if (isNightMode) Color(0x77FF453A) else TextTertiary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Placeholder view when no events are scheduled.
 */
@Composable
private fun EmptyScheduleView(
    isNightMode: Boolean,
    isLocked: Boolean,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit = {}
) {
    val title = when {
        isLocked -> "CALENDAR LOCKED"
        !hasPermission -> "📅 CONNECT CALENDAR"
        else -> "NO EVENTS TODAY"
    }

    val subtitle = when {
        isLocked -> "Unlock device to sync calendar"
        !hasPermission -> "Tap to grant calendar access"
        else -> "Your schedule is clear"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = !hasPermission && !isLocked) { onRequestPermission() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.08.em,
                    color = if (isNightMode) NightRed else if (!hasPermission) com.hoandesign.standby.ui.theme.AccentOrange else TextTertiary
                )
            )
            Text(
                text = subtitle,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = if (isNightMode) Color(0x99FF453A) else TextSecondary
                )
            )
        }
    }
}
