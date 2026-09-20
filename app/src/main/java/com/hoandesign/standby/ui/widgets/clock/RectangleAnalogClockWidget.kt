package com.hoandesign.standby.ui.widgets.clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.hoandesign.standby.ui.theme.StandbyBorder
import com.hoandesign.standby.ui.theme.StandbyBorderSubtle
import com.hoandesign.standby.ui.theme.StandbyCardBgSecondary
import com.hoandesign.standby.ui.theme.StandbyTheme
import com.hoandesign.standby.ui.theme.TextPrimary
import com.hoandesign.standby.ui.theme.TextSecondary
import com.hoandesign.standby.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Architectural Rectangular Analog Clock widget (Cartier Tank / Apple StandBy bespoke).
 *
 * Maximizes screen real estate in both compact bento modules and fullscreen widescreen displays
 * by fitting the outer track to the rectangular bounds without wasting circular corner space.
 */
@Composable
fun RectangleAnalogClockWidget(
    modifier: Modifier = Modifier,
    initialTime: ClockTime? = null,
    accentColor: Color = StandbyTheme.accentColor,
    smoothSweep: Boolean = true,
    cityLabel: String = "STANDBY",
    dateText: String = "WED 14"
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
    val secondaryColor = if (isNightMode) NightRedDim else TextTertiary
    val borderColor = if (isNightMode) NightRedDim else StandbyBorderSubtle
    val secondColor = if (isNightMode) NightRed else accentColor

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(dialBg)
            .padding(10.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h / 2f)

            val insetX = 14.dp.toPx()
            val insetY = 14.dp.toPx()
            val trackW = (w - insetX * 2f) / 2f
            val trackH = (h - insetY * 2f) / 2f
            val cornerRadius = 24.dp.toPx()

            // 1. Draw Outer Rounded Rectangle Perimeter Track
            drawRoundRect(
                color = borderColor,
                topLeft = Offset(center.x - trackW, center.y - trackH),
                size = Size(trackW * 2f, trackH * 2f),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = 1.2.dp.toPx())
            )

            // 2. Draw 60 Minute & Hour Ticks Projected onto Rectangle
            for (i in 0 until 60) {
                // Skip cardinal locations where numerals sit
                if (i == 0 || i == 15 || i == 30 || i == 45) continue

                val isHour = i % 5 == 0
                val angleDeg = i * 6.0
                val angleRad = Math.toRadians(angleDeg - 90.0)
                val cosA = cos(angleRad).toFloat()
                val sinA = sin(angleRad).toFloat()

                // Ray intersection with rectangle bounds
                val dist = rayIntersectRectangle(cosA, sinA, trackW, trackH)

                val tickLen = if (isHour) 12.dp.toPx() else 6.dp.toPx()
                val strokeW = if (isHour) 2.5.dp.toPx() else 1.2.dp.toPx()
                val tickColor = if (isHour) primaryColor else secondaryColor

                val outerX = center.x + dist * cosA
                val outerY = center.y + dist * sinA
                val innerX = center.x + (dist - tickLen) * cosA
                val innerY = center.y + (dist - tickLen) * sinA

                drawLine(
                    color = tickColor,
                    start = Offset(innerX, innerY),
                    end = Offset(outerX, outerY),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }

            // 3. Draw Bold Cardinal Numerals (12, 3, 6, 9)
            val numFontSize = (min(trackW, trackH) * 0.32f).coerceIn(16f, 72f).sp
            val numStyle = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Black,
                fontSize = numFontSize,
                color = primaryColor
            )

            // "12" Top
            val m12 = textMeasurer.measure("12", style = numStyle)
            drawText(
                textLayoutResult = m12,
                topLeft = Offset(center.x - m12.size.width / 2f, center.y - trackH + 8.dp.toPx())
            )

            // "6" Bottom
            val m6 = textMeasurer.measure("6", style = numStyle)
            drawText(
                textLayoutResult = m6,
                topLeft = Offset(center.x - m6.size.width / 2f, center.y + trackH - m6.size.height - 8.dp.toPx())
            )

            // "9" Left
            val m9 = textMeasurer.measure("9", style = numStyle)
            drawText(
                textLayoutResult = m9,
                topLeft = Offset(center.x - trackW + 10.dp.toPx(), center.y - m9.size.height / 2f)
            )

            // "3" Right
            val m3 = textMeasurer.measure("3", style = numStyle)
            drawText(
                textLayoutResult = m3,
                topLeft = Offset(center.x + trackW - m3.size.width - 10.dp.toPx(), center.y - m3.size.height / 2f)
            )

            // 4. City / Model Branding & Date Complication
            val badgeStyle = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.12.sp,
                color = secondaryColor
            )
            val mBrand = textMeasurer.measure(cityLabel, style = badgeStyle)
            drawText(
                textLayoutResult = mBrand,
                topLeft = Offset(center.x - mBrand.size.width / 2f, center.y - trackH * 0.42f)
            )

            val dateStyle = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (isNightMode) NightRed else primaryColor
            )
            val mDate = textMeasurer.measure(dateText, style = dateStyle)
            drawText(
                textLayoutResult = mDate,
                topLeft = Offset(center.x - mDate.size.width / 2f, center.y + trackH * 0.38f)
            )

            // 5. Clock Hands with Depth Shadow
            val baseRadius = min(trackW, trackH)

            // Hour Hand
            val hourLen = baseRadius * 0.48f
            val hourW = 6.dp.toPx()
            val hourTail = 12.dp.toPx()

            withTransform({
                rotate(degrees = currentTime.hourAngle, pivot = center)
            }) {
                // Drop shadow
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.6f),
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
            val minuteLen = baseRadius * 0.76f
            val minuteW = 4.dp.toPx()
            val minuteTail = 14.dp.toPx()

            withTransform({
                rotate(degrees = currentTime.minuteAngle, pivot = center)
            }) {
                // Drop shadow
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.6f),
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

            // Second Hand
            val secondAngle = currentTime.secondAngle(smooth = smoothSweep)
            val secondLen = baseRadius * 0.88f
            val secondW = 2.dp.toPx()
            val secondTail = 18.dp.toPx()

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
                drawCircle(
                    color = secondColor,
                    radius = 4.dp.toPx(),
                    center = Offset(center.x, center.y - secondLen * 0.65f)
                )
            }

            // Center Pinion Hub
            val hubRadius = 6.dp.toPx()
            drawCircle(
                color = secondColor,
                radius = hubRadius,
                center = center
            )
            drawCircle(
                color = dialBg,
                radius = hubRadius * 0.45f,
                center = center
            )
        }
    }
}

/**
 * Calculates ray intersection distance from center (0,0) with direction (cosA, sinA)
 * to the boundary of a rectangle with half-dimensions [halfW, halfH].
 */
private fun rayIntersectRectangle(cosA: Float, sinA: Float, halfW: Float, halfH: Float): Float {
    val tx = if (abs(cosA) > 1e-6f) halfW / abs(cosA) else Float.MAX_VALUE
    val ty = if (abs(sinA) > 1e-6f) halfH / abs(sinA) else Float.MAX_VALUE
    return min(tx, ty)
}
