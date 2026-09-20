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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.hoandesign.standby.model.ClockTime
import com.hoandesign.standby.model.isDaytime
import com.hoandesign.standby.model.solarProgress
import com.hoandesign.standby.ui.theme.AccentAmber
import com.hoandesign.standby.ui.theme.AccentOrange
import com.hoandesign.standby.ui.theme.NightRed
import com.hoandesign.standby.ui.theme.NightRedDim
import com.hoandesign.standby.ui.theme.NightRedTint
import com.hoandesign.standby.ui.theme.OledBlack
import com.hoandesign.standby.ui.theme.StandbyBorder
import com.hoandesign.standby.ui.theme.StandbyTheme
import com.hoandesign.standby.ui.theme.TextPrimary
import com.hoandesign.standby.ui.theme.TextSecondary
import com.hoandesign.standby.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt

/**
 * Solar Horizon Arc Clock Widget.
 *
 * Features:
 * - Visual sky arc charting the sun's trajectory across the celestial horizon dome.
 * - Glowing sun marker with multi-layered radial corona moving dynamically based on solar progress.
 * - Sunrise (e.g. "6:15 AM") and Sunset (e.g. "6:45 PM") milestone indicators.
 * - Horizon baseline and daylight status readouts (e.g. "65% DAYLIGHT ELAPSED").
 * - Prominent digital time readout centered underneath the sky dome.
 * - Automatic OLED Night Mode adaptation turning the solar arc into deep red starlight.
 *
 * @param modifier Root modifier.
 * @param clockTime Optional static or externally provided time snapshot.
 * @param sunriseMinutes Sunrise time in minutes of the day (default 375 = 6:15 AM).
 * @param sunsetMinutes Sunset time in minutes of the day (default 1125 = 6:45 PM).
 * @param sunriseText Text label for sunrise.
 * @param sunsetText Text label for sunset.
 * @param is24Hour If true, displays 24-hour time format.
 * @param accentColor Sun halo accent color; defaults to [AccentAmber].
 */
@Composable
fun SolarArcClockWidget(
    modifier: Modifier = Modifier,
    clockTime: ClockTime? = null,
    sunriseMinutes: Int = 375,
    sunsetMinutes: Int = 1125,
    sunriseText: String = "6:15 AM",
    sunsetText: String = "6:45 PM",
    is24Hour: Boolean = false,
    accentColor: Color = AccentAmber
) {
    val isNightMode = StandbyTheme.isNightMode

    // Live clock update loop (1-second cadence)
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

    val isDay = isDaytime(time.timeMinutesOfDay, sunriseMinutes, sunsetMinutes)
    val progress = solarProgress(time.timeMinutesOfDay, sunriseMinutes, sunsetMinutes)

    // Palette resolution
    val sunColor = if (isNightMode) NightRedTint else accentColor
    val glowColor = if (isNightMode) NightRed else AccentOrange
    val arcColor = if (isNightMode) NightRedDim else StandbyBorder
    val horizonColor = if (isNightMode) NightRedDim else TextTertiary

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        val width = maxWidth
        val height = maxHeight
        val arcCanvasHeight = (height * 0.44f).coerceIn(60.dp, 160.dp)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Sky Arc Canvas with Glowing Sun / Moon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(arcCanvasHeight)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasW = size.width
                    val canvasH = size.height

                    val startX = canvasW * 0.08f
                    val endX = canvasW * 0.92f
                    val horizonY = canvasH * 0.85f
                    val apexY = canvasH * 0.15f

                    // Draw Horizon Baseline (dashed subtle line)
                    drawLine(
                        color = horizonColor,
                        start = Offset(startX, horizonY),
                        end = Offset(endX, horizonY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // Draw Sky Arc Path (Parabolic curve)
                    val arcPath = Path().apply {
                        moveTo(startX, horizonY)
                        // Quadratic bezier control point at top center
                        quadraticTo(
                            x1 = (startX + endX) / 2f,
                            y1 = apexY - (horizonY - apexY), // control point elevated for smooth parabola
                            x2 = endX,
                            y2 = horizonY
                        )
                    }

                    // Stroke the arc
                    drawPath(
                        path = arcPath,
                        color = arcColor,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )

                    // Compute Sun coordinates along parabola: y = horizon - 4 * (horizon - apex) * t * (1 - t)
                    val sunX = startX + progress * (endX - startX)
                    val parabolaElevation = 4f * (horizonY - apexY) * progress * (1f - progress)
                    val sunY = horizonY - parabolaElevation

                    // Draw Sun Glowing Halo Layers
                    if (isDay) {
                        // Outer aura
                        drawCircle(
                            color = glowColor.copy(alpha = 0.18f),
                            radius = 26.dp.toPx(),
                            center = Offset(sunX, sunY)
                        )
                        // Mid corona
                        drawCircle(
                            color = sunColor.copy(alpha = 0.40f),
                            radius = 16.dp.toPx(),
                            center = Offset(sunX, sunY)
                        )
                        // Core body
                        drawCircle(
                            color = sunColor,
                            radius = 8.dp.toPx(),
                            center = Offset(sunX, sunY)
                        )
                        // Center hot spot
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = Offset(sunX, sunY)
                        )
                    } else {
                        // Nighttime: Render subtle crescent / resting twilight sun on horizon
                        val nightRestX = if (time.timeMinutesOfDay < sunriseMinutes) startX else endX
                        val nightRestY = horizonY

                        drawCircle(
                            color = if (isNightMode) NightRedDim else Color(0x6664D2FF),
                            radius = 12.dp.toPx(),
                            center = Offset(nightRestX, nightRestY)
                        )
                        drawCircle(
                            color = if (isNightMode) NightRed else Color(0xFF64D2FF),
                            radius = 6.dp.toPx(),
                            center = Offset(nightRestX, nightRestY)
                        )
                    }
                }

                // Milestone Labels: Sunrise (bottom left) and Sunset (bottom right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WbSunny,
                            contentDescription = "Sunrise",
                            tint = if (isNightMode) NightRedDim else AccentAmber,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = sunriseText,
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = if (isNightMode) NightRedDim else TextSecondary
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bedtime,
                            contentDescription = "Sunset",
                            tint = if (isNightMode) NightRedDim else TextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = sunsetText,
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = if (isNightMode) NightRedDim else TextSecondary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2. Central Digital Time Readout
            val timeFontSize = (height.value * 0.22f).coerceIn(24f, 54f).sp
            val hours = time.formattedHours(is24Hour = is24Hour)
            val minutes = time.formattedMinutes()
            val timeString = "$hours:$minutes"

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = timeString,
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Black,
                        fontSize = timeFontSize,
                        lineHeight = timeFontSize,
                        letterSpacing = (-0.04).em,
                        color = if (isNightMode) NightRed else TextPrimary
                    )
                )
                if (!is24Hour) {
                    Text(
                        text = time.amPm(),
                        modifier = Modifier.padding(start = 6.dp, bottom = 4.dp),
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = (timeFontSize.value * 0.35f).sp,
                            color = if (isNightMode) NightRedDim else TextSecondary
                        )
                    )
                }
            }

            // 3. Solar Status Indicator Tag
            val daylightPercent = (progress * 100f).roundToInt()
            val statusText = if (isDay) {
                "$daylightPercent% DAYLIGHT ELAPSED"
            } else if (time.timeMinutesOfDay < sunriseMinutes) {
                "NIGHT · SUNRISE AT $sunriseText"
            } else {
                "DUSK · SUNSET COMPLETED"
            }

            Text(
                text = statusText,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 0.08.em,
                    color = if (isNightMode) NightRedDim else if (isDay) sunColor else TextSecondary,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}
