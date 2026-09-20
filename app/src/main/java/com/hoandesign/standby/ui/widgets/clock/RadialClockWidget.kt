package com.hoandesign.standby.ui.widgets.clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import com.hoandesign.standby.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Radial Extended Horizon Clock Widget.
 *
 * Faithfully replicates StandBy reference design #2:
 * - Outstretched cardinal numerals: bold, wide "12", "3", "6", and "9".
 * - Angled architectural beam tick marks fanning radially from the center.
 * - Integrated Date badge (e.g. "WED 14") in the dial upper quadrant.
 * - Next Alarm indicator (e.g. "⏰ 6:30 AM") in the dial lower quadrant.
 * - Baton hands with 60fps continuous sweeping orange second hand.
 * - Deep red Night Mode adaptation for OLED bedside visibility.
 *
 * @param modifier Root modifier.
 * @param clockTime Optional static or externally provided time snapshot.
 * @param dateText Text displayed in the date badge (e.g. "WED 14").
 * @param alarmText Text displayed in the alarm badge (e.g. "6:30 AM").
 * @param smoothSweep If true, sweeps second hand at 60fps; otherwise ticks per second.
 * @param accentColor Accent color for second hand; defaults to [AccentOrange].
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
    val badgeColor = if (isNightMode) NightRed else TextSecondary
    val hourHandColor = if (isNightMode) NightRed else TextPrimary
    val minuteHandColor = if (isNightMode) NightRed else TextPrimary
    val secondHandColor = if (isNightMode) NightRedTint else accentColor

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(dialBackgroundColor)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val dialRadius = min(size.width, size.height) / 2f * 0.88f

            // 1. Draw Angled Beam Tick Marks
            val beamInnerRadius = dialRadius * 0.60f
            val beamOuterRadius = dialRadius * 0.88f
            val minorInnerRadius = dialRadius * 0.74f

            for (i in 0 until 60) {
                // Skip cardinal positions (12=0, 3=15, 6=30, 9=45) where outstretched numerals live
                if (i == 0 || i == 15 || i == 30 || i == 45) continue

                val isHourBeam = i % 5 == 0
                val angleDeg = i * 6.0
                val angleRad = Math.toRadians(angleDeg - 90.0)

                val startR = if (isHourBeam) beamInnerRadius else minorInnerRadius
                val endR = beamOuterRadius
                val strokeWidth = if (isHourBeam) 3.5.dp.toPx() else 1.4.dp.toPx()
                val color = if (isHourBeam) beamHourColor else beamMinuteColor

                val startX = center.x + (startR * cos(angleRad)).toFloat()
                val startY = center.y + (startR * sin(angleRad)).toFloat()
                val endX = center.x + (endR * cos(angleRad)).toFloat()
                val endY = center.y + (endR * sin(angleRad)).toFloat()

                drawLine(
                    color = color,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // 2. Draw Outstretched Cardinal Numerals (12, 3, 6, 9)
            val numeralFontSize = (dialRadius * 0.20f).sp
            val numeralStyle = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Black,
                fontSize = numeralFontSize,
                color = cardinalColor
            )

            // "12" Top
            val text12 = textMeasurer.measure("12", style = numeralStyle)
            val y12 = center.y - dialRadius * 0.74f
            drawText(
                textLayoutResult = text12,
                topLeft = Offset(center.x - text12.size.width / 2f, y12 - text12.size.height / 2f)
            )

            // "3" Right
            val text3 = textMeasurer.measure("3", style = numeralStyle)
            val x3 = center.x + dialRadius * 0.74f
            drawText(
                textLayoutResult = text3,
                topLeft = Offset(x3 - text3.size.width / 2f, center.y - text3.size.height / 2f)
            )

            // "6" Bottom
            val text6 = textMeasurer.measure("6", style = numeralStyle)
            val y6 = center.y + dialRadius * 0.74f
            drawText(
                textLayoutResult = text6,
                topLeft = Offset(center.x - text6.size.width / 2f, y6 - text6.size.height / 2f)
            )

            // "9" Left
            val text9 = textMeasurer.measure("9", style = numeralStyle)
            val x9 = center.x - dialRadius * 0.74f
            drawText(
                textLayoutResult = text9,
                topLeft = Offset(x9 - text9.size.width / 2f, center.y - text9.size.height / 2f)
            )

            // 3. Draw Date Badge ("WED 14") in Upper Dial Quadrant
            if (dateText.isNotBlank()) {
                val dateFontSize = (dialRadius * 0.08f).coerceAtLeast(10f).sp
                val dateStyle = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = dateFontSize,
                    color = badgeColor,
                    letterSpacing = 0.06.sp
                )
                val measuredDate = textMeasurer.measure(dateText.uppercase(), style = dateStyle)
                val dateCenterX = center.x + dialRadius * 0.32f
                val dateCenterY = center.y - dialRadius * 0.32f
                drawText(
                    textLayoutResult = measuredDate,
                    topLeft = Offset(
                        dateCenterX - measuredDate.size.width / 2f,
                        dateCenterY - measuredDate.size.height / 2f
                    )
                )
            }

            // 4. Draw Alarm Indicator ("⏰ 6:30 AM") in Lower Dial Quadrant
            if (alarmText.isNotBlank()) {
                val alarmFontSize = (dialRadius * 0.075f).coerceAtLeast(9.5f).sp
                val alarmStyle = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = alarmFontSize,
                    color = badgeColor
                )
                val measuredAlarm = textMeasurer.measure("⏰ $alarmText", style = alarmStyle)
                val alarmCenterX = center.x - dialRadius * 0.32f
                val alarmCenterY = center.y + dialRadius * 0.32f
                drawText(
                    textLayoutResult = measuredAlarm,
                    topLeft = Offset(
                        alarmCenterX - measuredAlarm.size.width / 2f,
                        alarmCenterY - measuredAlarm.size.height / 2f
                    )
                )
            }

            // 5. Draw Baton Hour Hand
            val hourHandLength = dialRadius * 0.50f
            val hourHandWidth = dialRadius * 0.052f
            val hourHandTail = dialRadius * 0.08f

            withTransform({
                rotate(degrees = time.hourAngle, pivot = center)
            }) {
                drawRoundRect(
                    color = hourHandColor,
                    topLeft = Offset(center.x - hourHandWidth / 2f, center.y - hourHandLength),
                    size = Size(hourHandWidth, hourHandLength + hourHandTail),
                    cornerRadius = CornerRadius(hourHandWidth / 2f, hourHandWidth / 2f)
                )
            }

            // 6. Draw Baton Minute Hand
            val minuteHandLength = dialRadius * 0.76f
            val minuteHandWidth = dialRadius * 0.035f
            val minuteHandTail = dialRadius * 0.08f

            withTransform({
                rotate(degrees = time.minuteAngle, pivot = center)
            }) {
                drawRoundRect(
                    color = minuteHandColor,
                    topLeft = Offset(center.x - minuteHandWidth / 2f, center.y - minuteHandLength),
                    size = Size(minuteHandWidth, minuteHandLength + minuteHandTail),
                    cornerRadius = CornerRadius(minuteHandWidth / 2f, minuteHandWidth / 2f)
                )
            }

            // 7. Draw Sweeping Orange Second Hand
            val secondHandAngle = time.secondAngle(smooth = smoothSweep)
            val secondHandLength = dialRadius * 0.86f
            val secondHandWidth = 2.0.dp.toPx()
            val secondHandTail = dialRadius * 0.18f

            withTransform({
                rotate(degrees = secondHandAngle, pivot = center)
            }) {
                drawLine(
                    color = secondHandColor,
                    start = Offset(center.x, center.y + secondHandTail),
                    end = Offset(center.x, center.y - secondHandLength),
                    strokeWidth = secondHandWidth,
                    cap = StrokeCap.Round
                )
            }

            // 8. Center Pivot Hub
            val hubRadius = dialRadius * 0.038f
            drawCircle(
                color = secondHandColor,
                radius = hubRadius,
                center = center
            )
            drawCircle(
                color = dialBackgroundColor,
                radius = hubRadius * 0.40f,
                center = center
            )
        }
    }
}
