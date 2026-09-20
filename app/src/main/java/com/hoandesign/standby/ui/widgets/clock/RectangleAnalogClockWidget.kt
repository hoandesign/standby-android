package com.hoandesign.standby.ui.widgets.clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoandesign.standby.model.ClockTime
import com.hoandesign.standby.ui.theme.NightRed
import com.hoandesign.standby.ui.theme.NightRedDim
import com.hoandesign.standby.ui.theme.OledBlack
import com.hoandesign.standby.ui.theme.StandbyTheme
import com.hoandesign.standby.ui.theme.TextPrimary
import com.hoandesign.standby.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/**
 * Authentic Apple iOS StandBy Squircle Analog Clock Widget.
 *
 * Implements continuous-curvature superellipse geometry (|x/a|^n + |y/b|^n = 1),
 * subtle perimeter minute ticks, 12 inward radial index rays, bold cardinal
 * numerals (12, 3, 6, 9), white rounded baton hands, and a hollow orange pinion hub.
 * Floats borderless on pure OLED black (#000000).
 */
@Composable
fun RectangleAnalogClockWidget(
    modifier: Modifier = Modifier,
    initialTime: ClockTime? = null,
    accentColor: Color = Color(0xFFFF9500), // Iconic Apple StandBy Orange
    smoothSweep: Boolean = true,
    cityLabel: String = "",
    dateText: String = ""
) {
    var currentTime by remember {
        mutableStateOf(initialTime ?: ClockTime.now())
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            val cal = Calendar.getInstance()
            currentTime = ClockTime(
                hours = cal.get(Calendar.HOUR),
                minutes = cal.get(Calendar.MINUTE),
                seconds = cal.get(Calendar.SECOND),
                millis = cal.get(Calendar.MILLISECOND)
            )
            delay(if (smoothSweep) 16L else 1000L)
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val isNightMode = StandbyTheme.isNightMode

    val dialBg = OledBlack
    val primaryColor = if (isNightMode) NightRed else TextPrimary
    val secondaryColor = if (isNightMode) NightRedDim else TextTertiary.copy(alpha = 0.65f)
    val secondColor = if (isNightMode) NightRed else accentColor

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(dialBg)
    ) {
        val availableWidth = constraints.maxWidth.toFloat()
        val availableHeight = constraints.maxHeight.toFloat()

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h / 2f)

            // Symmetrical square bounding box for squircle
            val side = min(w, h)
            val padding = 16.dp.toPx()
            val baseRadius = (side / 2f) - padding

            val squircleExponent = 4.2f

            // 1. Draw 60 Subtle Perimeter Minute Ticks along Squircle Track
            for (i in 0 until 60) {
                val isHour = i % 5 == 0
                val angleDeg = i * 6.0
                val angleRad = Math.toRadians(angleDeg - 90.0)
                val cosA = cos(angleRad).toFloat()
                val sinA = sin(angleRad).toFloat()

                val rOuter = rSquircle(angleRad, baseRadius, squircleExponent)
                val tickLen = if (isHour) 9.dp.toPx() else 5.dp.toPx()
                val tickStroke = if (isHour) 1.6.dp.toPx() else 1.0.dp.toPx()
                val tickColor = if (isHour) primaryColor.copy(alpha = 0.85f) else secondaryColor

                val outerX = center.x + rOuter * cosA
                val outerY = center.y + rOuter * sinA
                val innerX = center.x + (rOuter - tickLen) * cosA
                val innerY = center.y + (rOuter - tickLen) * sinA

                drawLine(
                    color = tickColor,
                    start = Offset(innerX, innerY),
                    end = Offset(outerX, outerY),
                    strokeWidth = tickStroke,
                    cap = StrokeCap.Round
                )
            }

            // 2. Draw 12 Major Inward Radial Rays
            // Cardinal directions (0, 15, 30, 45 -> 12, 3, 6, 9) have short outer anchor rays
            // Non-cardinal directions (1, 2, 4, 5, 7, 8, 10, 11) have long prominent radial rays
            for (hourIndex in 0 until 12) {
                val isCardinal = hourIndex % 3 == 0 // 0=12, 3=3, 6=6, 9=9
                val angleDeg = hourIndex * 30.0
                val angleRad = Math.toRadians(angleDeg - 90.0)
                val cosA = cos(angleRad).toFloat()
                val sinA = sin(angleRad).toFloat()

                val rOuter = rSquircle(angleRad, baseRadius, squircleExponent)

                if (isCardinal) {
                    // Short outer anchor ray at cardinal positions
                    val rayLen = 14.dp.toPx()
                    drawLine(
                        color = primaryColor,
                        start = Offset(center.x + (rOuter - rayLen) * cosA, center.y + (rOuter - rayLen) * sinA),
                        end = Offset(center.x + rOuter * cosA, center.y + rOuter * sinA),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                } else {
                    // Long, authoritative radial ray pointing inward towards center
                    val rayLen = baseRadius * 0.35f
                    drawLine(
                        color = primaryColor,
                        start = Offset(center.x + (rOuter - rayLen) * cosA, center.y + (rOuter - rayLen) * sinA),
                        end = Offset(center.x + rOuter * cosA, center.y + rOuter * sinA),
                        strokeWidth = 2.6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 3. Draw Bold Geometric Cardinal Numerals (12, 3, 6, 9)
            val numFontSize = (baseRadius * 0.32f).coerceIn(20f, 68f).sp
            val numStyle = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = numFontSize,
                color = primaryColor
            )

            // "12" Top
            val m12 = textMeasurer.measure("12", style = numStyle)
            val r12 = rSquircle(Math.toRadians(-90.0), baseRadius, squircleExponent)
            drawText(
                textLayoutResult = m12,
                topLeft = Offset(center.x - m12.size.width / 2f, center.y - r12 + 20.dp.toPx())
            )

            // "6" Bottom
            val m6 = textMeasurer.measure("6", style = numStyle)
            val r6 = rSquircle(Math.toRadians(90.0), baseRadius, squircleExponent)
            drawText(
                textLayoutResult = m6,
                topLeft = Offset(center.x - m6.size.width / 2f, center.y + r6 - m6.size.height - 20.dp.toPx())
            )

            // "9" Left
            val m9 = textMeasurer.measure("9", style = numStyle)
            val r9 = rSquircle(Math.toRadians(180.0), baseRadius, squircleExponent)
            drawText(
                textLayoutResult = m9,
                topLeft = Offset(center.x - r9 + 20.dp.toPx(), center.y - m9.size.height / 2f)
            )

            // "3" Right
            val m3 = textMeasurer.measure("3", style = numStyle)
            val r3 = rSquircle(0.0, baseRadius, squircleExponent)
            drawText(
                textLayoutResult = m3,
                topLeft = Offset(center.x + r3 - m3.size.width - 20.dp.toPx(), center.y - m3.size.height / 2f)
            )

            // Optional subtle branding or date (if explicitly provided)
            if (cityLabel.isNotEmpty()) {
                val badgeStyle = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 0.1.sp,
                    color = secondaryColor
                )
                val mBrand = textMeasurer.measure(cityLabel, style = badgeStyle)
                drawText(
                    textLayoutResult = mBrand,
                    topLeft = Offset(center.x - mBrand.size.width / 2f, center.y - baseRadius * 0.40f)
                )
            }
            if (dateText.isNotEmpty()) {
                val dateStyle = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = primaryColor
                )
                val mDate = textMeasurer.measure(dateText, style = dateStyle)
                drawText(
                    textLayoutResult = mDate,
                    topLeft = Offset(center.x - mDate.size.width / 2f, center.y + baseRadius * 0.38f)
                )
            }

            // 4. White Rounded Baton Clock Hands with Depth Shadow
            // Hour Hand
            val hourLen = baseRadius * 0.46f
            val hourW = 6.5.dp.toPx()
            val hourTail = 10.dp.toPx()

            withTransform({
                rotate(degrees = currentTime.hourAngle, pivot = center)
            }) {
                // Drop shadow
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.65f),
                    topLeft = Offset(center.x - hourW / 2f + 1.dp.toPx(), center.y - hourLen + 1.5.dp.toPx()),
                    size = Size(hourW, hourLen + hourTail),
                    cornerRadius = CornerRadius(hourW / 2f, hourW / 2f)
                )
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(center.x - hourW / 2f, center.y - hourLen),
                    size = Size(hourW, hourLen + hourTail),
                    cornerRadius = CornerRadius(hourW / 2f, hourW / 2f)
                )
            }

            // Minute Hand
            val minuteLen = baseRadius * 0.72f
            val minuteW = 4.8.dp.toPx()
            val minuteTail = 12.dp.toPx()

            withTransform({
                rotate(degrees = currentTime.minuteAngle, pivot = center)
            }) {
                // Drop shadow
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.65f),
                    topLeft = Offset(center.x - minuteW / 2f + 1.dp.toPx(), center.y - minuteLen + 1.5.dp.toPx()),
                    size = Size(minuteW, minuteLen + minuteTail),
                    cornerRadius = CornerRadius(minuteW / 2f, minuteW / 2f)
                )
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(center.x - minuteW / 2f, center.y - minuteLen),
                    size = Size(minuteW, minuteLen + minuteTail),
                    cornerRadius = CornerRadius(minuteW / 2f, minuteW / 2f)
                )
            }

            // Second Hand: Razor-Sharp Vibrant Orange Needle
            val secondAngle = currentTime.secondAngle(smooth = smoothSweep)
            val secondLen = baseRadius * 0.86f
            val secondW = 1.8.dp.toPx()
            val secondTail = 14.dp.toPx()

            withTransform({
                rotate(degrees = secondAngle, pivot = center)
            }) {
                // Drop shadow
                drawLine(
                    color = Color.Black.copy(alpha = 0.45f),
                    start = Offset(center.x + 1.dp.toPx(), center.y + secondTail + 1.dp.toPx()),
                    end = Offset(center.x + 1.dp.toPx(), center.y - secondLen + 1.dp.toPx()),
                    strokeWidth = secondW,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = secondColor,
                    start = Offset(center.x, center.y + secondTail),
                    end = Offset(center.x, center.y - secondLen),
                    strokeWidth = secondW,
                    cap = StrokeCap.Round
                )
            }

            // 5. Authentic Center Pinion Hub: Orange Ring with Hollow Pitch-Black Core
            val hubRadius = 6.5.dp.toPx()
            drawCircle(
                color = secondColor,
                radius = hubRadius,
                center = center,
                style = Stroke(width = 2.2.dp.toPx())
            )
            drawCircle(
                color = dialBg,
                radius = hubRadius - 2.2.dp.toPx(),
                center = center
            )
        }
    }
}

/**
 * Calculates radial distance from center to the boundary of a continuous-curvature squircle
 * using the Lamé curve / superellipse equation: |x/a|^n + |y/b|^n = 1.
 */
private fun rSquircle(thetaRad: Double, radius: Float, exponent: Float = 4.2f): Float {
    val cosT = abs(cos(thetaRad)).toFloat()
    val sinT = abs(sin(thetaRad)).toFloat()
    val denom = (cosT.pow(exponent) + sinT.pow(exponent)).pow(1f / exponent)
    return if (denom > 1e-4f) radius / denom else radius
}
