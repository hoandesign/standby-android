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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Authentic Apple iOS StandBy Rounded-Rectangle Analog Clock Widget.
 *
 * Fills the full width and height of the container, utilizing a modern rounded
 * rectangle dial with subtle corner curvature. Features:
 * - 60 perimeter ticks following the rounded-rectangle track (perpendicular to straight edges).
 * - 8 long inward radial hour rays pointing directly toward the center hub.
 * - Bold cardinal numerals (12, 3, 6, 9) with ample breathing space.
 * - White rounded baton hands with depth shadows.
 * - Iconic Apple StandBy orange second needle with hollow pinion hub.
 * - Floats borderless on pure OLED pitch-black (#000000).
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
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h / 2f)

            // Full-bleed utilization: uses available width and height with a tight 8dp optical margin
            val padding = 8.dp.toPx()
            val halfW = (w / 2f) - padding
            val halfH = (h / 2f) - padding

            if (halfW <= 20f || halfH <= 20f) return@Canvas

            val minDim = min(halfW, halfH)
            // Tighter corner radius for modern rectangle geometry
            val cornerRadius = (minDim * 0.12f).coerceIn(12.dp.toPx(), 32.dp.toPx())

            // 1. Draw 60 Perimeter Minute Ticks along the Rounded Rectangle Track
            for (i in 0 until 60) {
                val isHour = i % 5 == 0
                val angleDeg = i * 6.0
                val angleRad = Math.toRadians(angleDeg - 90.0)

                val bp = intersectRoundedRect(angleRad, halfW, halfH, cornerRadius, center)
                val tickLen = if (isHour) 8.5.dp.toPx() else 4.5.dp.toPx()
                val tickStroke = if (isHour) 1.6.dp.toPx() else 1.0.dp.toPx()
                val tickColor = if (isHour) primaryColor.copy(alpha = 0.85f) else secondaryColor

                val outer = bp.point
                val inner = Offset(
                    outer.x + bp.normal.x * tickLen,
                    outer.y + bp.normal.y * tickLen
                )

                drawLine(
                    color = tickColor,
                    start = inner,
                    end = outer,
                    strokeWidth = tickStroke,
                    cap = StrokeCap.Round
                )
            }

            // 2. Draw 12 Major Hour Markers
            // Non-cardinal hours (1, 2, 4, 5, 7, 8, 10, 11) have long prominent radial rays
            // pointing directly toward the center hub.
            // Cardinal hours (0=12, 3=3, 6=6, 9=9) have outer anchor ticks.
            for (hourIndex in 0 until 12) {
                val isCardinal = hourIndex % 3 == 0 // 0=12, 3=3, 6=6, 9=9
                val angleDeg = hourIndex * 30.0
                val angleRad = Math.toRadians(angleDeg - 90.0)

                val bp = intersectRoundedRect(angleRad, halfW, halfH, cornerRadius, center)

                if (isCardinal) {
                    // Refined anchor tick at cardinal positions
                    val rayLen = 9.dp.toPx()
                    val outer = bp.point
                    val inner = Offset(
                        outer.x + bp.normal.x * rayLen,
                        outer.y + bp.normal.y * rayLen
                    )
                    drawLine(
                        color = primaryColor,
                        start = inner,
                        end = outer,
                        strokeWidth = 2.4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                } else {
                    // Long prominent radial ray pointing directly toward the center hub
                    val toCenter = Offset(center.x - bp.point.x, center.y - bp.point.y)
                    val dist = sqrt(toCenter.x * toCenter.x + toCenter.y * toCenter.y)
                    if (dist > 1e-3f) {
                        val dir = Offset(toCenter.x / dist, toCenter.y / dist)
                        val rayLen = minDim * 0.36f
                        val rayStart = bp.point
                        val rayEnd = Offset(bp.point.x + dir.x * rayLen, bp.point.y + dir.y * rayLen)

                        drawLine(
                            color = primaryColor,
                            start = rayStart,
                            end = rayEnd,
                            strokeWidth = 2.4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // 3. Draw Bold Geometric Cardinal Numerals (12, 3, 6, 9)
            val numFontSize = (minDim * 0.32f).coerceIn(22f, 66f).sp
            val numStyle = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = numFontSize,
                color = primaryColor
            )

            val numeralSpacing = (minDim * 0.12f).coerceIn(12.dp.toPx(), 24.dp.toPx())

            // "12" Top
            val m12 = textMeasurer.measure("12", style = numStyle)
            drawText(
                textLayoutResult = m12,
                topLeft = Offset(
                    center.x - m12.size.width / 2f,
                    center.y - halfH + numeralSpacing
                )
            )

            // "6" Bottom
            val m6 = textMeasurer.measure("6", style = numStyle)
            drawText(
                textLayoutResult = m6,
                topLeft = Offset(
                    center.x - m6.size.width / 2f,
                    center.y + halfH - m6.size.height - numeralSpacing
                )
            )

            // "9" Left
            val m9 = textMeasurer.measure("9", style = numStyle)
            drawText(
                textLayoutResult = m9,
                topLeft = Offset(
                    center.x - halfW + numeralSpacing,
                    center.y - m9.size.height / 2f
                )
            )

            // "3" Right
            val m3 = textMeasurer.measure("3", style = numStyle)
            drawText(
                textLayoutResult = m3,
                topLeft = Offset(
                    center.x + halfW - m3.size.width - numeralSpacing,
                    center.y - m3.size.height / 2f
                )
            )

            // Optional subtle city or date labels
            if (cityLabel.isNotEmpty()) {
                val maxLabelWidth = (halfW * 1.2f).toInt().coerceAtLeast(40)
                val badgeStyle = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 0.1.sp,
                    color = secondaryColor
                )
                val mBrand = textMeasurer.measure(
                    text = cityLabel,
                    style = badgeStyle,
                    constraints = androidx.compose.ui.unit.Constraints(maxWidth = maxLabelWidth),
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    maxLines = 1
                )
                drawText(
                    textLayoutResult = mBrand,
                    topLeft = Offset(center.x - mBrand.size.width / 2f, center.y - minDim * 0.42f)
                )
            }
            if (dateText.isNotEmpty()) {
                val maxLabelWidth = (halfW * 1.2f).toInt().coerceAtLeast(40)
                val dateStyle = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = primaryColor
                )
                val mDate = textMeasurer.measure(
                    text = dateText,
                    style = dateStyle,
                    constraints = androidx.compose.ui.unit.Constraints(maxWidth = maxLabelWidth),
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    maxLines = 1
                )
                drawText(
                    textLayoutResult = mDate,
                    topLeft = Offset(center.x - mDate.size.width / 2f, center.y + minDim * 0.40f)
                )
            }

            // 4. White Rounded Baton Clock Hands with Depth Shadows
            // Hour Hand (Constant Radius based on dial bounds)
            val hourAngle = currentTime.hourAngle
            val hourLen = minDim * 0.48f
            val hourW = 6.5.dp.toPx()
            val hourTail = 10.dp.toPx()

            withTransform({
                rotate(degrees = hourAngle, pivot = center)
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

            // Minute Hand (Constant Radius based on dial bounds)
            val minuteAngle = currentTime.minuteAngle
            val minuteLen = minDim * 0.74f
            val minuteW = 4.8.dp.toPx()
            val minuteTail = 12.dp.toPx()

            withTransform({
                rotate(degrees = minuteAngle, pivot = center)
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

            // Second Hand: Razor-Sharp Vibrant Orange Needle (Constant Radius)
            val secondAngle = currentTime.secondAngle(smooth = smoothSweep)
            val secondLen = minDim * 0.88f
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
            val hubRadius = 6.dp.toPx()
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
 * Representation of a boundary intersection point and its inward-pointing surface normal.
 */
private data class RectBoundaryPoint(
    val point: Offset,
    val normal: Offset
)

/**
 * Calculates the exact intersection of a ray from [center] at angle [angleRad]
 * with a rounded rectangle of half-dimensions [halfW], [halfH] and corner radius [cornerRadius].
 * Returns the intersection point and the inward-pointing normal vector.
 */
private fun intersectRoundedRect(
    angleRad: Double,
    halfW: Float,
    halfH: Float,
    cornerRadius: Float,
    center: Offset
): RectBoundaryPoint {
    val dx = cos(angleRad).toFloat()
    val dy = sin(angleRad).toFloat()
    val r = cornerRadius.coerceAtMost(min(halfW, halfH))
    val straightHalfW = halfW - r
    val straightHalfH = halfH - r

    // 1. Right straight edge
    if (dx > 1e-5f) {
        val t = halfW / dx
        val y = t * dy
        if (abs(y) <= straightHalfH) {
            return RectBoundaryPoint(
                point = Offset(center.x + halfW, center.y + y),
                normal = Offset(-1f, 0f)
            )
        }
    }

    // 2. Left straight edge
    if (dx < -1e-5f) {
        val t = -halfW / dx
        val y = t * dy
        if (abs(y) <= straightHalfH) {
            return RectBoundaryPoint(
                point = Offset(center.x - halfW, center.y + y),
                normal = Offset(1f, 0f)
            )
        }
    }

    // 3. Bottom straight edge
    if (dy > 1e-5f) {
        val t = halfH / dy
        val x = t * dx
        if (abs(x) <= straightHalfW) {
            return RectBoundaryPoint(
                point = Offset(center.x + x, center.y + halfH),
                normal = Offset(0f, -1f)
            )
        }
    }

    // 4. Top straight edge
    if (dy < -1e-5f) {
        val t = -halfH / dy
        val x = t * dx
        if (abs(x) <= straightHalfW) {
            return RectBoundaryPoint(
                point = Offset(center.x + x, center.y - halfH),
                normal = Offset(0f, 1f)
            )
        }
    }

    // 5. Corner Arcs
    val cx = if (dx > 0) straightHalfW else -straightHalfW
    val cy = if (dy > 0) straightHalfH else -straightHalfH

    val dot = dx * cx + dy * cy
    val c2 = cx * cx + cy * cy
    val disc = dot * dot - (c2 - r * r)
    val t = dot + sqrt(max(0f, disc))

    val px = t * dx
    val py = t * dy

    val nx = cx - px
    val ny = cy - py
    val len = sqrt(nx * nx + ny * ny)
    val norm = if (len > 1e-4f) Offset(nx / len, ny / len) else Offset(-dx, -dy)

    return RectBoundaryPoint(
        point = Offset(center.x + px, center.y + py),
        normal = norm
    )
}
