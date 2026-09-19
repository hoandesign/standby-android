package com.hoandesign.standby.ui.widgets.calendar

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.hoandesign.standby.model.CalendarMonth
import com.hoandesign.standby.ui.theme.NightRed
import com.hoandesign.standby.ui.theme.OledBlack
import com.hoandesign.standby.ui.theme.StandbyTheme
import com.hoandesign.standby.ui.theme.TextPrimary
import com.hoandesign.standby.ui.theme.TextSecondary
import java.util.Locale

/**
 * Faithful reproduction of iOS 18 StandBy Month Calendar Widget.
 *
 * Visual highlights:
 * - Uppercase bold red month title (e.g. "APRIL" or "SEPTEMBER") in [NightRed] (`#FF453A`).
 * - Day-of-week header row (`S  M  T  W  T  F  S`) in subtle secondary text.
 * - Monthly date grid with today circled in a filled red pill/circle (`#FF453A`) with white bold text.
 * - Flawlessly adapts to Red Night Mode with dimmed red tones for OLED dark environments.
 *
 * @param modifier Root modifier.
 * @param calendarMonth Month model to display; defaults to current system month.
 * @param onDateClick Optional callback when a date cell is tapped.
 */
@Composable
fun MonthCalendarWidget(
    modifier: Modifier = Modifier,
    calendarMonth: CalendarMonth? = null,
    onDateClick: ((Int) -> Unit)? = null
) {
    val month = calendarMonth ?: remember { CalendarMonth.now() }
    val isNightMode = StandbyTheme.isNightMode

    // Day of week single-character headers starting on Sunday
    val daysOfWeek = remember { listOf("S", "M", "T", "W", "T", "F", "S") }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        val availableHeight = maxHeight
        val titleFontSize = (availableHeight.value * 0.10f).coerceIn(14f, 20f).sp
        val headerFontSize = (availableHeight.value * 0.065f).coerceIn(10f, 13f).sp
        val dateFontSize = (availableHeight.value * 0.075f).coerceIn(11f, 14f).sp
        val pillSize = (availableHeight.value * 0.13f).coerceIn(24f, 30f).dp

        val weeks = remember(month) { month.grid.chunked(7) }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Month Title: Uppercase Red e.g. "SEPTEMBER"
            Text(
                text = month.monthName.uppercase(Locale.US),
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = titleFontSize,
                    letterSpacing = 0.05.em,
                    color = NightRed
                ),
                modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
            )

            // Day of Week Header Row: S  M  T  W  T  F  S
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                daysOfWeek.forEach { dayLetter ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayLetter,
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = headerFontSize,
                                color = if (isNightMode) Color(0x99FF453A) else TextSecondary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Monthly Date Grid: 4 to 6 rows evenly distributed
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                weeks.forEach { week ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        week.forEach { dayNumber ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (dayNumber != null) {
                                    val isToday = dayNumber == month.todayDayOfMonth
                                    val clickableModifier = if (onDateClick != null) {
                                        Modifier.clickable { onDateClick(dayNumber) }
                                    } else {
                                        Modifier
                                    }

                                    if (isToday) {
                                        // Today: Circled in filled red pill (#FF453A) with white bold text
                                        Box(
                                            modifier = Modifier
                                                .size(pillSize)
                                                .clip(CircleShape)
                                                .background(NightRed)
                                                .then(clickableModifier),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dayNumber.toString(),
                                                style = TextStyle(
                                                    fontFamily = FontFamily.Default,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = dateFontSize,
                                                    color = Color.White
                                                )
                                            )
                                        }
                                    } else {
                                        // Standard date cell
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .then(clickableModifier),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dayNumber.toString(),
                                                style = TextStyle(
                                                    fontFamily = FontFamily.Default,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = dateFontSize,
                                                    color = if (isNightMode) Color(0xCCFF453A) else TextPrimary
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Pad row with empty cells if the week has fewer than 7 days
                        if (week.size < 7) {
                            repeat(7 - week.size) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
