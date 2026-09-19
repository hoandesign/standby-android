package com.hoandesign.standby.ui.widgets.clock

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    dateText: String = "MON 5 · 76°",
    alarmText: String = "6:30 AM",
    accentColor: Color = StandbyTheme.accentColor,
    is24Hour: Boolean = false
) {
    val isNightMode = StandbyTheme.isNightMode
    val activeColor = if (isNightMode) NightRed else accentColor

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
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val width = maxWidth
        val height = maxHeight
        val isWide = width > height * 1.5f

        val hoursString = time.formattedHours(is24Hour = is24Hour)
        val minutesString = time.formattedMinutes()

        if (isWide) {
            // Horizontal Wide Layout (e.g. Fullscreen Hero Clock or Wide Slot)
            val timeFontSize = (height.value * 0.58f).sp
            val metaFontSize = (height.value * 0.11f).coerceIn(12f, 20f).sp

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row: Date Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DateBannerChip(
                        text = dateText,
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

                Spacer(modifier = Modifier.height(4.dp))
            }
        } else {
            // Stacked Vertical Layout (iOS StandBy Dual Card style)
            // Hours stacked on top of Minutes
            val digitFontSize = (height.value * 0.38f).coerceAtLeast(44f).sp
            val metaFontSize = (height.value * 0.085f).coerceIn(11f, 15f).sp

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                // Top: Date Banner
                DateBannerChip(
                    text = dateText,
                    fontSize = metaFontSize,
                    isNightMode = isNightMode
                )

                // Center: Stacked Digits
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = hoursString,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Black,
                            fontSize = digitFontSize,
                            lineHeight = digitFontSize * 0.88f,
                            letterSpacing = (-0.05).em,
                            color = activeColor
                        )
                    )
                    Text(
                        text = minutesString,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Black,
                            fontSize = digitFontSize,
                            lineHeight = digitFontSize * 0.88f,
                            letterSpacing = (-0.05).em,
                            color = activeColor
                        )
                    )
                }

                // Bottom: Next Alarm Indicator
                if (alarmText.isNotBlank()) {
                    AlarmIndicatorChip(
                        alarmText = alarmText,
                        fontSize = metaFontSize,
                        tint = activeColor,
                        isNightMode = isNightMode
                    )
                }
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
    val textColor = if (isNightMode) NightRed else TextPrimary
    Text(
        text = text.uppercase(),
        style = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            letterSpacing = 0.08.em,
            color = textColor
        )
    )
}

/**
 * Minimalist alarm chip with clock icon and alarm time.
 */
@Composable
private fun AlarmIndicatorChip(
    alarmText: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    tint: Color,
    isNightMode: Boolean
) {
    val chipBg = if (isNightMode) Color(0x33FF453A) else StandbyCardBgSecondary
    val contentColor = if (isNightMode) NightRed else TextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(chipBg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "⏰",
                fontSize = fontSize * 0.95f
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
