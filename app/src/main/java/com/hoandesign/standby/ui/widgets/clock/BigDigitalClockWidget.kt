package com.hoandesign.standby.ui.widgets.clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.hoandesign.standby.util.SystemIntents
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.hoandesign.standby.model.ClockTime
import com.hoandesign.standby.ui.theme.NightRed
import com.hoandesign.standby.ui.theme.OledBlack
import com.hoandesign.standby.ui.theme.StandbyCardBgSecondary
import com.hoandesign.standby.ui.theme.StandbyTheme
import com.hoandesign.standby.ui.theme.TextPrimary
import com.hoandesign.standby.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Oversized Digital Typography Clock Widget (iOS 18 StandBy style).
 *
 * Features:
 * - Massive bold typography scaled responsively to fill the container without clipping.
 * - Stacked (hours over minutes) or inline layout depending on available aspect ratio.
 * - Next system alarm indicator badge (e.g. "⏰ 6:30 AM").
 * - Date and weather banner (e.g. "MON 5 · 76°").
 * - Selectable accent color tinting with automatic OLED deep-red Night Mode switching.
 *
 * @param modifier Root modifier.
 * @param clockTime Optional static or externally provided time snapshot.
 * @param dateText Text displayed in the date/weather banner.
 * @param alarmText Text displayed in the alarm indicator.
 * @param accentColor Custom tint color; defaults to [StandbyTheme.accentColor].
 * @param is24Hour If true, displays 24-hour time format (00..23).
 */
@Composable
fun BigDigitalClockWidget(
    modifier: Modifier = Modifier,
    clockTime: ClockTime? = null,
    dateText: String = "",
    alarmText: String = "6:30 AM",
    accentColor: Color = StandbyTheme.accentColor,
    is24Hour: Boolean = false
) {
    val isNightMode = StandbyTheme.isNightMode
    val activeColor = if (isNightMode) NightRed else accentColor

    val today = remember { java.time.LocalDate.now() }
    val dayOfWeek = today.dayOfWeek.name.take(3)
    val dayOfMonth = today.dayOfMonth
    val isCelsius = StandbyTheme.temperatureUnit == com.hoandesign.standby.model.TemperatureUnit.CELSIUS
    val resolvedDateText = if (dateText.isNotBlank()) dateText else {
        val tempDisplay = if (isCelsius) "24°C" else "76°F"
        "$dayOfWeek $dayOfMonth · $tempDisplay"
    }

    // Live clock update loop (1-second ticking cadence)
    val time by produceState(
        initialValue = clockTime ?: ClockTime.now(is24Hour = is24Hour),
        key1 = clockTime,
        key2 = is24Hour
    ) {
        if (clockTime != null) {
            value = clockTime
            return@produceState
        }
        while (isActive) {
            value = ClockTime.now(is24Hour = is24Hour)
            delay(1000L)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        val width = maxWidth
        val height = maxHeight
        val isWide = width.value >= height.value * 1.18f

        val hoursString = time.formattedHours(is24Hour = is24Hour)
        val minutesString = time.formattedMinutes()

        if (isWide) {
            // Horizontal Wide Layout: Single-line "09:41"
            val maxFontFromWidth = (width.value - 32f) / 2.75f
            val maxFontFromHeight = (height.value - 54f) * 0.72f
            val timeFontSize = minOf(maxFontFromWidth, maxFontFromHeight).coerceIn(24f, 160f).sp
            val metaFontSize = (height.value * 0.08f).coerceIn(10f, 16f).sp

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row: Date Banner on Left, Alarm on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DateBannerChip(
                        text = resolvedDateText,
                        fontSize = metaFontSize,
                        isNightMode = isNightMode
                    )

                    if (alarmText.isNotBlank()) {
                        AlarmIndicatorChip(
                            alarmText = alarmText,
                            fontSize = metaFontSize,
                            tint = activeColor,
                            isNightMode = isNightMode
                        )
                    }
                }

                // Center Massive Digital Digits: "09:41"
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$hoursString:$minutesString",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Black,
                            fontSize = timeFontSize,
                            lineHeight = timeFontSize,
                            letterSpacing = (-0.05).em,
                            color = activeColor,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
            }
        } else {
            // Stacked Vertical Layout: Hours over Minutes, perfectly centered & proportional
            val maxFontFromWidth = (width.value - 28f) / 1.35f
            val maxFontFromHeight = (height.value - 64f) / 1.75f
            val digitFontSize = minOf(maxFontFromWidth, maxFontFromHeight).coerceIn(22f, 130f).sp
            val metaFontSize = (height.value * 0.075f).coerceIn(10f, 14f).sp

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Row: Date Banner & Alarm Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DateBannerChip(
                        text = resolvedDateText,
                        fontSize = metaFontSize,
                        isNightMode = isNightMode
                    )

                    if (alarmText.isNotBlank()) {
                        AlarmIndicatorChip(
                            alarmText = alarmText,
                            fontSize = metaFontSize,
                            tint = activeColor,
                            isNightMode = isNightMode
                        )
                    }
                }

                // Center Stacked Massive Digital Digits
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = hoursString,
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Black,
                                fontSize = digitFontSize,
                                lineHeight = digitFontSize * 0.90f,
                                letterSpacing = (-0.05).em,
                                color = activeColor,
                                textAlign = TextAlign.Center
                            )
                        )
                        Text(
                            text = minutesString,
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Black,
                                fontSize = digitFontSize,
                                lineHeight = digitFontSize * 0.90f,
                                letterSpacing = (-0.05).em,
                                color = activeColor,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

/**
 * Clean status chip for displaying current date and weather temperature.
 */
@Composable
private fun DateBannerChip(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    isNightMode: Boolean
) {
    val context = LocalContext.current
    val textColor = if (isNightMode) NightRed else TextPrimary
    Text(
        text = text.uppercase(),
        style = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            letterSpacing = 0.08.em,
            color = textColor
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { SystemIntents.launchSystemCalendar(context) }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

/**
 * Minimalist alarm chip with sleek vector clock icon and alarm time (no generic emoji).
 */
@Composable
private fun AlarmIndicatorChip(
    alarmText: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    tint: Color,
    isNightMode: Boolean
) {
    val context = LocalContext.current
    val chipBg = if (isNightMode) Color(0x33FF453A) else StandbyCardBgSecondary
    val contentColor = if (isNightMode) NightRed else TextSecondary
    val iconSizeDp = with(LocalDensity.current) { (fontSize * 1.05f).toDp() }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(chipBg)
            .clickable { SystemIntents.launchAlarmClock(context) }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Alarm,
                contentDescription = "Alarm",
                tint = contentColor,
                modifier = Modifier.size(iconSizeDp)
            )
            Text(
                text = alarmText,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = fontSize,
                    color = contentColor
                )
            )
        }
    }
}

@Composable
private fun VectorAlarmIcon(
    size: Dp,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w / 2f, h / 2f + h * 0.05f)
        val radius = w * 0.36f
        val strokeWidth = 1.3.dp.toPx()

        // Clock Body Ring
        drawCircle(
            color = tint,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )

        // Clock Hands (pointing at 10:10)
        drawLine(
            color = tint,
            start = center,
            end = Offset(center.x - radius * 0.45f, center.y - radius * 0.28f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = center,
            end = Offset(center.x + radius * 0.45f, center.y - radius * 0.28f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Twin Bells
        val bellDist = radius * 1.06f
        val bellRadius = radius * 0.26f
        val leftBellCenter = Offset(center.x - bellDist * 0.707f, center.y - bellDist * 0.707f)
        val rightBellCenter = Offset(center.x + bellDist * 0.707f, center.y - bellDist * 0.707f)

        drawCircle(color = tint, radius = bellRadius, center = leftBellCenter)
        drawCircle(color = tint, radius = bellRadius, center = rightBellCenter)
    }
}
