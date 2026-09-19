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
 * Classic Swiss/Bauhaus Analog Clock Widget.
 *
 * Features:
 * - Precision Canvas-rendered dial with 12 numeral markers and 60 tick lines.
 * - Prominent hour ticks and subtle hairline minute ticks.
 * - Crisp baton hour and minute hands with rounded caps.
 * - Signature continuous sweeping orange second hand with counterweight tail and center pivot cap.
 * - Configurable city timezone label (e.g. "CUPERTINO" or "HANOI").
 * - Full OLED pitch-black background with seamless deep red Night Mode adaptation.
 *
 * @param modifier Root modifier.
 * @param cityName City or timezone label displayed on the dial face.
 * @param smoothSweep If true, sweeps the second hand at 60fps (16ms loop); if false, ticks once per second.
 * @param clockTime Optional static or externally provided time snapshot (ideal for tests and previews).
 */
@Composable
fun AnalogClockWidget(
    modifier: Modifier = Modifier,
    cityName: String = "CUPERTINO",
    smoothSweep: Boolean = true,
    clockTime: ClockTime? = null
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

    // Color palette resolution based on active Night Mode state
    val dialBackgroundColor = OledBlack
    val primaryTickColor = if (isNightMode) NightRed else TextPrimary
    val minuteTickColor = if (isNightMode) NightRedDim else TextTertiary
    val numeralColor = if (isNightMode) NightRed else TextPrimary
    val cityLabelColor = if (isNightMode) NightRedDim else TextSecondary
    val hourHandColor = if (isNightMode) NightRed else TextPrimary
    val minuteHandColor = if (isNightMode) NightRed else TextPrimary
    val secondHandColor = if (isNightMode) NightRedTint else AccentOrange

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
            val dialRadius = min(size.width, size.height) / 2f * 0.94f

            // 1. Draw 60 Dial Ticks (12 major hour ticks, 48 minor minute ticks)
            val hourTickLength = dialRadius * 0.09f
            val minuteTickLength = dialRadius * 0.045f
            val hourTickStroke = 3.2.dp.toPx()
            val minuteTickStroke = 1.4.dp.toPx()

            for (i in 0 until 60) {
                val isHour = i % 5 == 0
                val angleDeg = i * 6.0
                val angleRad = Math.toRadians(angleDeg - 90.0)

                val tickLength = if (isHour) hourTickLength else minuteTickLength
                val strokeWidth = if (isHour) hourTickStroke else minuteTickStroke
                val color = if (isHour) primaryTickColor else minuteTickColor

                val startR = dialRadius - tickLength
                val endR = dialRadius

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

            // 2. Draw 12 Dial Numerals (Bauhaus / Swiss geometric layout)
            val numeralRadius = dialRadius * 0.74f
            val numeralFontSize = (dialRadius * 0.13f).sp

            val numeralStyle = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = numeralFontSize,
                color = numeralColor
            )

            for (hour in 1..12) {
                val angleDeg = hour * 30.0
                val angleRad = Math.toRadians(angleDeg - 90.0)

                val targetX = center.x + (numeralRadius * cos(angleRad)).toFloat()
                val targetY = center.y + (numeralRadius * sin(angleRad)).toFloat()

                val textLayout = textMeasurer.measure(
                    text = hour.toString(),
                    style = numeralStyle
                )

                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(
                        targetX - textLayout.size.width / 2f,
                        targetY - textLayout.size.height / 2f
                    )
                )
            }

            // 3. Draw City / Timezone Label (e.g. "CUPERTINO")
            if (cityName.isNotBlank()) {
                val cityFontSize = (dialRadius * 0.065f).sp
                val cityStyle = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = cityFontSize,
                    color = cityLabelColor,
                    letterSpacing = 0.12.sp
                )
                val cityLayout = textMeasurer.measure(
                    text = cityName.uppercase(),
                    style = cityStyle
                )
                val cityY = center.y + dialRadius * 0.36f
                drawText(
                    textLayoutResult = cityLayout,
                    topLeft = Offset(
                        center.x - cityLayout.size.width / 2f,
                        cityY - cityLayout.size.height / 2f
                    )
                )
            }

            // 4. Draw Baton Hour Hand
            val hourHandLength = dialRadius * 0.52f
            val hourHandWidth = dialRadius * 0.052f
            val hourHandTail = dialRadius * 0.09f

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

            // 5. Draw Baton Minute Hand
            val minuteHandLength = dialRadius * 0.78f
            val minuteHandWidth = dialRadius * 0.036f
            val minuteHandTail = dialRadius * 0.09f

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

            // 6. Draw Continuous Sweeping Orange Second Hand
            val secondHandAngle = time.secondAngle(smooth = smoothSweep)
            val secondHandLength = dialRadius * 0.88f
            val secondHandWidth = 2.0.dp.toPx()
            val secondHandTail = dialRadius * 0.20f
            val counterweightRadius = dialRadius * 0.042f

            withTransform({
                rotate(degrees = secondHandAngle, pivot = center)
            }) {
                // Slender needle shaft extending past center pivot
                drawLine(
                    color = secondHandColor,
                    start = Offset(center.x, center.y + secondHandTail),
                    end = Offset(center.x, center.y - secondHandLength),
                    strokeWidth = secondHandWidth,
                    cap = StrokeCap.Round
                )

                // Classic counterweight circle on tail
                drawCircle(
                    color = secondHandColor,
                    radius = counterweightRadius,
                    center = Offset(center.x, center.y + secondHandTail * 0.65f)
                )
            }

            // 7. Draw Center Pivot Hub Cap
            val outerHubRadius = dialRadius * 0.045f
            val innerHubRadius = dialRadius * 0.018f

            drawCircle(
                color = secondHandColor,
                radius = outerHubRadius,
                center = center
            )
            drawCircle(
                color = dialBackgroundColor,
                radius = innerHubRadius,
                center = center
            )
        }
    }
}
