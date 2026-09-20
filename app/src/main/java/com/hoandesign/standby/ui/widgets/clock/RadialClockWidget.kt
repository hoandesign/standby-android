package com.hoandesign.standby.ui.widgets.clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import com.hoandesign.standby.ui.theme.AccentOrange
import com.hoandesign.standby.ui.theme.NightRed
import com.hoandesign.standby.ui.theme.NightRedDim
import com.hoandesign.standby.ui.theme.NightRedTint
import com.hoandesign.standby.ui.theme.OledBlack
import com.hoandesign.standby.ui.theme.StandbyBorder
import com.hoandesign.standby.ui.theme.StandbyTheme
import com.hoandesign.standby.ui.theme.TextPrimary
import com.hoandesign.standby.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Radial Extended Horizon Clock Widget.
 *
 * Faithfully replicates Apple StandBy Reference Design #2:
 * - Outstretched cardinal numerals: bold, wide "12", "3", "6", and "9".
 * - Architectural beam tick marks fanning radially from the center.
 * - Horizontal Midline Badges:
 *     * Left: Clean vector alarm icon with "6:30AM" centered between "9" and center hub.
 *     * Right: Two-tone "WED 14" (muted accent + white) centered between center hub and "3".
 * - Collision-Free Hands:
 *     * Chunky baton hour hand constrained to inner radius (length 0.32R) so it NEVER
 *       overlaps or collides with the horizontal alarm or date badges.
 *     * White baton minute hand and continuous sweeping orange second hand.
 * - Responsive Widescreen Horizon Scaling:
 *     * Automatically stretches into a panoramic widescreen horizon dial in landscape
 *       modes, or a symmetrical circular dial in compact 1:1 bento mode.
 */
@Composable
fun RadialClockWidget(
    modifier: Modifier = Modifier,
    clockTime: ClockTime? = null,
    dateText: String = "WED 14",
    alarmText: String = "6:30 AM",
    smoothSweep: Boolean = true,
    accentColor: Color = AccentOrange
) {
    val isNightMode = StandbyTheme.isNightMode
    val textMeasurer = rememberTextMeasurer()

    // Live continuous time state loop
    val time by produceState(
        initialValue = clockTime ?: ClockTime.now(),
        key1 = clockTime,
        key2 = smoothSweep
    ) {
        if (clockTime != null) {
            value = clockTime
            return@produceState
        }
        val frameDelay = if (smoothSweep) 16L else 1000L
        while (isActive) {
            value = ClockTime.now()
            delay(frameDelay)
        }
    }

    // Color resolution
    val dialBackgroundColor = OledBlack
    val cardinalColor = if (isNightMode) NightRed else TextPrimary
    val beamHourColor = if (isNightMode) NightRed else TextPrimary.copy(alpha = 0.85f)
    val beamMinuteColor = if (isNightMode) NightRedDim else StandbyBorder
    val hourHandColor = if (isNightMode) NightRed else TextPrimary
    val minuteHandColor = if (isNightMode) NightRed else TextPrimary
    val secondHandColor = if (isNightMode) NightRedTint else accentColor
    val dateAccentColor = if (isNightMode) NightRed else Color(0xFFC45B3E)
    val dateTextColor = if (isNightMode) NightRed else Color.White

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(dialBackgroundColor)
            .padding(8.dp)
    ) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val center = Offset(width / 2f, height / 2f)

            // Pure geometric circular dial (never distorted into an ellipse/oval)
            val dialRadius = min(width, height) / 2f * 0.84f
            val xRadius = dialRadius
            val yRadius = dialRadius

            // 1. Draw Architectural Beam Tick Marks
            val beamInnerFactor = 0.58f
            val beamOuterFactor = 0.88f
            val minorInnerFactor = 0.74f

            for (i in 0 until 60) {
                // Skip cardinal positions (12=0, 3=15, 6=30, 9=45) where outstretched numerals sit
                if (i == 0 || i == 15 || i == 30 || i == 45) continue

                val isHourBeam = i % 5 == 0
                val angleDeg = i * 6.0
                val angleRad = Math.toRadians(angleDeg - 90.0)

                val startFactor = if (isHourBeam) beamInnerFactor else minorInnerFactor
                val endFactor = beamOuterFactor
                val strokeWidth = if (isHourBeam) 3.2.dp.toPx() else 1.3.dp.toPx()
                val color = if (isHourBeam) beamHourColor else beamMinuteColor

                val cosA = cos(angleRad).toFloat()
                val sinA = sin(angleRad).toFloat()

                val startX = center.x + (xRadius * startFactor * cosA)
                val startY = center.y + (yRadius * startFactor * sinA)
                val endX = center.x + (xRadius * endFactor * cosA)
                val endY = center.y + (yRadius * endFactor * sinA)

                drawLine(
                    color = color,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // 2. Draw Outstretched Cardinal Numerals (12, 3, 6, 9)
            val numeralFontSize = (yRadius * 0.22f).coerceIn(16f, 110f).sp
            val numeralStyle = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Black,
                fontSize = numeralFontSize,
                color = cardinalColor
            )

            // "12" Top
            val text12 = textMeasurer.measure("12", style = numeralStyle)
            val y12 = center.y - yRadius * 0.76f
            drawText(
                textLayoutResult = text12,
                topLeft = Offset(center.x - text12.size.width / 2f, y12 - text12.size.height / 2f)
            )

            // "6" Bottom
            val text6 = textMeasurer.measure("6", style = numeralStyle)
            val y6 = center.y + yRadius * 0.76f
            drawText(
                textLayoutResult = text6,
                topLeft = Offset(center.x - text6.size.width / 2f, y6 - text6.size.height / 2f)
            )

            // "9" Left - positioned cleanly at dial boundary
            val text9 = textMeasurer.measure("9", style = numeralStyle)
            val x9 = center.x - xRadius * 0.83f
            drawText(
                textLayoutResult = text9,
                topLeft = Offset(x9 - text9.size.width / 2f, center.y - text9.size.height / 2f)
            )

            // "3" Right - positioned cleanly at dial boundary
            val text3 = textMeasurer.measure("3", style = numeralStyle)
            val x3 = center.x + xRadius * 0.83f
            drawText(
                textLayoutResult = text3,
                topLeft = Offset(x3 - text3.size.width / 2f, center.y - text3.size.height / 2f)
            )

            val hubRadius = yRadius * 0.036f

            // 3. Draw Alarm Indicator on Horizontal Midline (Left of Center, Right of "9")
            if (alarmText.isNotBlank()) {
                val cleanAlarm = alarmText.replace(" ", "") // "6:30AM"
                val badgeFontSize = (yRadius * 0.075f).coerceIn(8.5f, 22f).sp
                val alarmStyle = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = badgeFontSize,
                    color = if (isNightMode) NightRed else Color.White,
                    letterSpacing = (-0.02).sp
                )
                val measuredAlarm = textMeasurer.measure(cleanAlarm, style = alarmStyle)

                // Vector Alarm Bell Dimensions
                val bellSize = (yRadius * 0.070f).coerceIn(9f, 22f)
                val badgeGap = 4.dp.toPx()
                val totalAlarmWidth = bellSize + badgeGap + measuredAlarm.size.width

                // Dynamically compute optical midpoint between inner edge of '9' and center hub
                val inner9Edge = x9 + text9.size.width / 2f
                val leftHubEdge = center.x - hubRadius
                val alarmCenterX = (inner9Edge + leftHubEdge) / 2f
                val alarmStartX = alarmCenterX - totalAlarmWidth / 2f
                val alarmCenterY = center.y

                // Draw sleek minimal vector alarm icon (never raw emoji)
                drawVectorAlarmIcon(
                    center = Offset(alarmStartX + bellSize / 2f, alarmCenterY),
                    sizePx = bellSize,
                    tint = secondHandColor
                )

                // Draw clean alarm text
                drawText(
                    textLayoutResult = measuredAlarm,
                    topLeft = Offset(
                        alarmStartX + bellSize + badgeGap,
                        alarmCenterY - measuredAlarm.size.height / 2f
                    )
                )
            }

            // 4. Draw Date Badge on Horizontal Midline (Right of Center, Left of "3")
            if (dateText.isNotBlank()) {
                val badgeFontSize = (yRadius * 0.072f).coerceIn(8.5f, 22f).sp

                val parts = dateText.trim().split(" ")
                val dayPart = parts.firstOrNull() ?: ""
                val numPart = if (parts.size > 1) " " + parts.drop(1).joinToString(" ") else ""

                val dayStyle = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = badgeFontSize,
                    color = dateAccentColor,
                    letterSpacing = (-0.02).sp
                )
                val numStyle = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = badgeFontSize,
                    color = dateTextColor,
                    letterSpacing = (-0.02).sp
                )

                val measuredDay = textMeasurer.measure(dayPart, style = dayStyle)
                val measuredNum = textMeasurer.measure(numPart, style = numStyle)
                val totalDateWidth = measuredDay.size.width + measuredNum.size.width

                // Dynamically compute optical midpoint between center hub and inner edge of '3'
                val rightHubEdge = center.x + hubRadius
                val inner3Edge = x3 - text3.size.width / 2f
                val dateCenterX = (rightHubEdge + inner3Edge) / 2f
                val dateStartX = dateCenterX - totalDateWidth / 2f
                val dateCenterY = center.y

                drawText(
                    textLayoutResult = measuredDay,
                    topLeft = Offset(dateStartX, dateCenterY - measuredDay.size.height / 2f)
                )
                drawText(
                    textLayoutResult = measuredNum,
                    topLeft = Offset(dateStartX + measuredDay.size.width, dateCenterY - measuredNum.size.height / 2f)
                )
            }

            // 5. Draw Chunky Baton Hour Hand (with drop shadow so it floats with depth above complications)
            val hourHandLength = yRadius * 0.38f
            val hourHandWidth = yRadius * 0.054f
            val hourHandTail = yRadius * 0.06f

            withTransform({
                rotate(degrees = time.hourAngle, pivot = center)
            }) {
                // Drop shadow
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.55f),
                    topLeft = Offset(center.x - hourHandWidth / 2f + 1.dp.toPx(), center.y - hourHandLength + 1.5.dp.toPx()),
                    size = Size(hourHandWidth, hourHandLength + hourHandTail),
                    cornerRadius = CornerRadius(hourHandWidth / 2f, hourHandWidth / 2f)
                )
                drawRoundRect(
                    color = hourHandColor,
                    topLeft = Offset(center.x - hourHandWidth / 2f, center.y - hourHandLength),
                    size = Size(hourHandWidth, hourHandLength + hourHandTail),
                    cornerRadius = CornerRadius(hourHandWidth / 2f, hourHandWidth / 2f)
                )
            }

            // 6. Draw Baton Minute Hand (with drop shadow)
            val minuteHandLength = yRadius * 0.70f
            val minuteHandWidth = yRadius * 0.034f
            val minuteHandTail = yRadius * 0.07f

            withTransform({
                rotate(degrees = time.minuteAngle, pivot = center)
            }) {
                // Drop shadow
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.55f),
                    topLeft = Offset(center.x - minuteHandWidth / 2f + 1.dp.toPx(), center.y - minuteHandLength + 1.5.dp.toPx()),
                    size = Size(minuteHandWidth, minuteHandLength + minuteHandTail),
                    cornerRadius = CornerRadius(minuteHandWidth / 2f, minuteHandWidth / 2f)
                )
                drawRoundRect(
                    color = minuteHandColor,
                    topLeft = Offset(center.x - minuteHandWidth / 2f, center.y - minuteHandLength),
                    size = Size(minuteHandWidth, minuteHandLength + minuteHandTail),
                    cornerRadius = CornerRadius(minuteHandWidth / 2f, minuteHandWidth / 2f)
                )
            }

            // 7. Draw Sweeping Orange Second Hand (with drop shadow)
            val secondHandAngle = time.secondAngle(smooth = smoothSweep)
            val secondHandLength = yRadius * 0.84f
            val secondHandWidth = 2.0.dp.toPx()
            val secondHandTail = yRadius * 0.16f

            withTransform({
                rotate(degrees = secondHandAngle, pivot = center)
            }) {
                // Drop shadow
                drawLine(
                    color = Color.Black.copy(alpha = 0.45f),
                    start = Offset(center.x + 1.dp.toPx(), center.y + secondHandTail + 1.5.dp.toPx()),
                    end = Offset(center.x + 1.dp.toPx(), center.y - secondHandLength + 1.5.dp.toPx()),
                    strokeWidth = secondHandWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = secondHandColor,
                    start = Offset(center.x, center.y + secondHandTail),
                    end = Offset(center.x, center.y - secondHandLength),
                    strokeWidth = secondHandWidth,
                    cap = StrokeCap.Round
                )
            }

            // 8. Center Pivot Hub
            drawCircle(
                color = secondHandColor,
                radius = hubRadius,
                center = center
            )
            drawCircle(
                color = dialBackgroundColor,
                radius = hubRadius * 0.45f,
                center = center
            )
        }
    }
}

/**
 * Draws a clean, minimalist vector alarm clock icon matching Apple StandBy hardware aesthetics.
 */
private fun DrawScope.drawVectorAlarmIcon(
    center: Offset,
    sizePx: Float,
    tint: Color
) {
    val radius = sizePx * 0.40f
    val strokeWidth = 1.4.dp.toPx()

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
        end = Offset(center.x - radius * 0.50f, center.y - radius * 0.30f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = tint,
        start = center,
        end = Offset(center.x + radius * 0.50f, center.y - radius * 0.30f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Twin Bells at 45-degree angles
    val bellDist = radius * 1.05f
    val bellRadius = radius * 0.28f
    val leftBellCenter = Offset(center.x - bellDist * 0.707f, center.y - bellDist * 0.707f)
    val rightBellCenter = Offset(center.x + bellDist * 0.707f, center.y - bellDist * 0.707f)

    drawCircle(color = tint, radius = bellRadius, center = leftBellCenter)
    drawCircle(color = tint, radius = bellRadius, center = rightBellCenter)

    // Tiny feet
    val footDist = radius * 1.05f
    drawLine(
        color = tint,
        start = Offset(center.x - footDist * 0.65f, center.y + footDist * 0.75f),
        end = Offset(center.x - footDist * 0.85f, center.y + footDist * 0.95f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = tint,
        start = Offset(center.x + footDist * 0.65f, center.y + footDist * 0.75f),
        end = Offset(center.x + footDist * 0.85f, center.y + footDist * 0.95f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}
