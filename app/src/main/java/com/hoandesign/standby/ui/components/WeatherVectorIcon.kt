package com.hoandesign.standby.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hoandesign.standby.ui.theme.AccentAmber
import com.hoandesign.standby.ui.theme.AccentCyan
import com.hoandesign.standby.ui.theme.NightRed
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-fidelity, minimalist vector weather iconography designed to Apple StandBy standards.
 * Eliminates generic colored OS emojis with sharp, consistent 1.5dp architectural strokes.
 */
@Composable
fun WeatherVectorIcon(
    condition: String,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    tint: Color = Color.White,
    isNightMode: Boolean = false
) {
    val activeTint = if (isNightMode) NightRed else tint
    val sunTint = if (isNightMode) NightRed else AccentAmber
    val rainTint = if (isNightMode) NightRed else AccentCyan

    val condLower = condition.lowercase()

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w / 2f, h / 2f)
        val stroke = 1.6.dp.toPx()

        when {
            condLower.contains("clear") || condLower.contains("sunny") || condLower.contains("☀️") -> {
                drawSun(center = center, radius = w * 0.22f, strokeWidth = stroke, tint = sunTint)
            }
            condLower.contains("partly") || condLower.contains("mainly clear") || condLower.contains("🌤") || condLower.contains("⛅") -> {
                drawPartlyCloudy(w = w, h = h, strokeWidth = stroke, cloudTint = activeTint, sunTint = sunTint)
            }
            condLower.contains("rain") || condLower.contains("drizzle") || condLower.contains("shower") || condLower.contains("🌧") || condLower.contains("🌦") -> {
                drawRain(w = w, h = h, strokeWidth = stroke, cloudTint = activeTint, rainTint = rainTint)
            }
            condLower.contains("thunder") || condLower.contains("⛈") -> {
                drawThunderstorm(w = w, h = h, strokeWidth = stroke, cloudTint = activeTint, boltTint = sunTint)
            }
            condLower.contains("snow") || condLower.contains("🌨") -> {
                drawSnow(center = center, size = w * 0.7f, strokeWidth = stroke, tint = activeTint)
            }
            condLower.contains("fog") || condLower.contains("mist") || condLower.contains("🌫") -> {
                drawFog(center = center, width = w * 0.75f, strokeWidth = stroke, tint = activeTint)
            }
            else -> {
                // Cloudy / Overcast default
                drawCloud(center = Offset(center.x, center.y + h * 0.05f), scale = w * 0.75f, strokeWidth = stroke, tint = activeTint)
            }
        }
    }
}

private fun DrawScope.drawSun(center: Offset, radius: Float, strokeWidth: Float, tint: Color) {
    // Sun disc
    drawCircle(
        color = tint,
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidth)
    )

    // 8 architectural radiating rays
    val rayInner = radius * 1.35f
    val rayOuter = radius * 1.75f
    for (i in 0 until 8) {
        val angle = Math.toRadians((i * 45.0) - 90.0)
        val cosA = cos(angle).toFloat()
        val sinA = sin(angle).toFloat()
        drawLine(
            color = tint,
            start = Offset(center.x + rayInner * cosA, center.y + rayInner * sinA),
            end = Offset(center.x + rayOuter * cosA, center.y + rayOuter * sinA),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawCloud(center: Offset, scale: Float, strokeWidth: Float, tint: Color) {
    val cloudPath = Path().apply {
        val left = center.x - scale * 0.42f
        val right = center.x + scale * 0.42f
        val bottom = center.y + scale * 0.20f

        // Bottom baseline
        moveTo(left + scale * 0.15f, bottom)
        lineTo(right - scale * 0.15f, bottom)

        // Right puff
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                right - scale * 0.30f, bottom - scale * 0.30f,
                right, bottom
            ),
            startAngleDegrees = 90f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )

        // Top large dome
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                center.x - scale * 0.24f, bottom - scale * 0.54f,
                center.x + scale * 0.24f, bottom - scale * 0.06f
            ),
            startAngleDegrees = 10f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )

        // Left puff
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                left, bottom - scale * 0.26f,
                left + scale * 0.26f, bottom
            ),
            startAngleDegrees = 260f,
            sweepAngleDegrees = -160f,
            forceMoveTo = false
        )
        close()
    }

    drawPath(path = cloudPath, color = tint.copy(alpha = 0.12f))
    drawPath(path = cloudPath, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
}

private fun DrawScope.drawPartlyCloudy(w: Float, h: Float, strokeWidth: Float, cloudTint: Color, sunTint: Color) {
    // Sun peeking top-right
    val sunCenter = Offset(w * 0.68f, h * 0.32f)
    drawCircle(
        color = sunTint,
        radius = w * 0.18f,
        center = sunCenter,
        style = Stroke(width = strokeWidth)
    )

    // 4 visible sun rays pointing outward
    val rayAngles = listOf(-90.0, -45.0, 0.0, 45.0)
    val rIn = w * 0.24f
    val rOut = w * 0.32f
    for (deg in rayAngles) {
        val rad = Math.toRadians(deg)
        val cosA = cos(rad).toFloat()
        val sinA = sin(rad).toFloat()
        drawLine(
            color = sunTint,
            start = Offset(sunCenter.x + rIn * cosA, sunCenter.y + rIn * sinA),
            end = Offset(sunCenter.x + rOut * cosA, sunCenter.y + rOut * sinA),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }

    // Cloud in front (slightly offset to bottom-left)
    drawCloud(center = Offset(w * 0.44f, h * 0.58f), scale = w * 0.70f, strokeWidth = strokeWidth, tint = cloudTint)
}

private fun DrawScope.drawRain(w: Float, h: Float, strokeWidth: Float, cloudTint: Color, rainTint: Color) {
    drawCloud(center = Offset(w * 0.50f, h * 0.40f), scale = w * 0.72f, strokeWidth = strokeWidth, tint = cloudTint)

    // 3 sleek diagonal raindrops
    val drops = listOf(
        Offset(w * 0.36f, h * 0.70f),
        Offset(w * 0.50f, h * 0.73f),
        Offset(w * 0.64f, h * 0.70f)
    )
    val dropLenX = -3.dp.toPx()
    val dropLenY = 8.dp.toPx()

    for (d in drops) {
        drawLine(
            color = rainTint,
            start = d,
            end = Offset(d.x + dropLenX, d.y + dropLenY),
            strokeWidth = strokeWidth * 0.95f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawThunderstorm(w: Float, h: Float, strokeWidth: Float, cloudTint: Color, boltTint: Color) {
    drawCloud(center = Offset(w * 0.50f, h * 0.38f), scale = w * 0.72f, strokeWidth = strokeWidth, tint = cloudTint)

    // Sharp lightning bolt path
    val bolt = Path().apply {
        moveTo(w * 0.52f, h * 0.60f)
        lineTo(w * 0.44f, h * 0.75f)
        lineTo(w * 0.54f, h * 0.75f)
        lineTo(w * 0.46f, h * 0.95f)
    }
    drawPath(path = bolt, color = boltTint, style = Stroke(width = strokeWidth * 1.1f, cap = StrokeCap.Round))
}

private fun DrawScope.drawSnow(center: Offset, size: Float, strokeWidth: Float, tint: Color) {
    val r = size / 2f
    for (i in 0 until 3) {
        val angle = Math.toRadians(i * 60.0)
        val cosA = cos(angle).toFloat()
        val sinA = sin(angle).toFloat()
        drawLine(
            color = tint,
            start = Offset(center.x - r * cosA, center.y - r * sinA),
            end = Offset(center.x + r * cosA, center.y + r * sinA),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawFog(center: Offset, width: Float, strokeWidth: Float, tint: Color) {
    val y1 = center.y - 6.dp.toPx()
    val y2 = center.y
    val y3 = center.y + 6.dp.toPx()
    val halfW = width / 2f

    drawLine(color = tint, start = Offset(center.x - halfW * 0.85f, y1), end = Offset(center.x + halfW * 0.85f, y1), strokeWidth = strokeWidth, cap = StrokeCap.Round)
    drawLine(color = tint, start = Offset(center.x - halfW, y2), end = Offset(center.x + halfW, y2), strokeWidth = strokeWidth, cap = StrokeCap.Round)
    drawLine(color = tint, start = Offset(center.x - halfW * 0.70f, y3), end = Offset(center.x + halfW * 0.70f, y3), strokeWidth = strokeWidth, cap = StrokeCap.Round)
}
