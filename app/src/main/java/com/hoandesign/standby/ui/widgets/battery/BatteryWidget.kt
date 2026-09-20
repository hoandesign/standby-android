package com.hoandesign.standby.ui.widgets.battery

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.hoandesign.standby.data.BatteryMonitor
import com.hoandesign.standby.model.BatteryState
import com.hoandesign.standby.ui.theme.AccentGreen
import com.hoandesign.standby.ui.theme.NightRed
import com.hoandesign.standby.ui.theme.NightRedDim
import com.hoandesign.standby.ui.theme.StandbyBorder
import com.hoandesign.standby.ui.theme.StandbyCardBgSecondary
import com.hoandesign.standby.ui.theme.StandbyTheme
import com.hoandesign.standby.ui.theme.TextPrimary
import com.hoandesign.standby.ui.theme.TextSecondary
import com.hoandesign.standby.ui.theme.TextTertiary
import kotlin.math.min

/**
 * Authentic Apple StandBy Circular Arc Battery & Power Widget.
 *
 * Features:
 * - Iconic Apple StandBy circular arc gauge with rounded stroke cap.
 * - Central bold percentage with glowing lightning bolt indicator.
 * - Dynamic charging status pill ("⚡ Fast Charging", "⚡ Qi Wireless", or "On Battery").
 * - Sub-info row with device name, battery capacity (e.g. 4700 mAh), and time to full.
 * - Seamless OLED Night Mode adaptation.
 *
 * @param modifier Root modifier.
 * @param initialBatteryState Optional fixed state for previews and tests.
 * @param isFullscreen If true, expands to full-screen dual power dashboard.
 */
@Composable
fun BatteryWidget(
    modifier: Modifier = Modifier,
    initialBatteryState: BatteryState? = null,
    isFullscreen: Boolean = false
) {
    val context = LocalContext.current
    val monitor = remember(context) { BatteryMonitor(context) }
    val flow = remember(monitor) { monitor.batteryStateFlow() }
    val liveState by flow.collectAsState(initial = monitor.getCurrentBatteryState())
    val batteryState = initialBatteryState ?: liveState

    val isNightMode = StandbyTheme.isNightMode
    val activeAccent = if (isNightMode) NightRed else AccentGreen

    // Charging pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "BatteryPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ChargePulseAlpha"
    )

    if (isFullscreen) {
        FullscreenBatteryDashboard(
            batteryState = batteryState,
            activeAccent = activeAccent,
            isNightMode = isNightMode,
            pulseAlpha = pulseAlpha,
            modifier = modifier
        )
    } else {
        CompactCircularBatteryWidget(
            batteryState = batteryState,
            activeAccent = activeAccent,
            isNightMode = isNightMode,
            pulseAlpha = pulseAlpha,
            modifier = modifier
        )
    }
}

/**
 * Compact circular arc gauge fitting snugly into dual bento slot card.
 */
@Composable
private fun CompactCircularBatteryWidget(
    batteryState: BatteryState,
    activeAccent: Color,
    isNightMode: Boolean,
    pulseAlpha: Float,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        val availableHeight = maxHeight
        val availableWidth = maxWidth

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Header: "BATTERY" tag + Charging Speed Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BATTERY",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.08.em,
                        color = if (isNightMode) NightRed else TextTertiary
                    )
                )

                // Charging Speed Chip
                val pillBg = if (isNightMode) {
                    if (batteryState.isCharging) Color(0x33FF453A) else StandbyCardBgSecondary
                } else {
                    if (batteryState.isCharging) activeAccent.copy(alpha = 0.18f) else StandbyCardBgSecondary
                }
                val pillBorder = if (isNightMode) {
                    if (batteryState.isCharging) Color(0x66FF453A) else StandbyBorder
                } else {
                    if (batteryState.isCharging) activeAccent.copy(alpha = 0.4f) else StandbyBorder
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(pillBg)
                        .border(1.dp, pillBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = batteryState.chargeSpeed,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = if (batteryState.isCharging) activeAccent else TextSecondary
                        )
                    )
                }
            }

            // 2. Center: Iconic Apple StandBy Circular Arc Gauge
            val ringDiameter = min(availableWidth.value * 0.58f, availableHeight.value * 0.58f).coerceIn(80f, 150f).dp

            Box(
                modifier = Modifier
                    .size(ringDiameter),
                contentAlignment = Alignment.Center
            ) {
                val fraction = (batteryState.percentage / 100f).coerceIn(0f, 1f)
                val effectiveAlpha = if (batteryState.isCharging) pulseAlpha else 1.0f

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = size.width * 0.11f
                    val arcSize = size.width - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                    // Track background ring
                    drawArc(
                        color = if (isNightMode) Color(0x22FF453A) else Color(0x2AFFFFFF),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress arc ring
                    if (fraction > 0f) {
                        val arcSweep = 360f * fraction
                        drawArc(
                            color = activeAccent.copy(alpha = effectiveAlpha),
                            startAngle = -90f,
                            sweepAngle = arcSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Inner content: Percentage & optional bolt
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (batteryState.isCharging) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Charging",
                            tint = activeAccent,
                            modifier = Modifier.size((ringDiameter.value * 0.18f).dp)
                        )
                    }

                    Text(
                        text = "${batteryState.percentage}%",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Black,
                            fontSize = (ringDiameter.value * 0.28f).sp,
                            letterSpacing = (-0.02).em,
                            color = activeAccent
                        )
                    )
                }
            }

            // 3. Sub-info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${batteryState.capacityMah} mAh",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = if (isNightMode) Color(0xAAFF453A) else TextSecondary
                    )
                )

                val statusDetail = when {
                    batteryState.percentage >= 100 -> "Fully Charged"
                    batteryState.isCharging && batteryState.timeToFullMinutes != null ->
                        "Full in ${batteryState.timeToFullMinutes} min"
                    batteryState.isCharging -> "Charging"
                    else -> "Discharging"
                }

                Text(
                    text = statusDetail,
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = if (isNightMode) Color(0xAAFF453A) else TextSecondary
                    )
                )
            }
        }
    }
}

/**
 * Fullscreen power dashboard with large dual circular rings and detailed charging telemetry.
 */
@Composable
private fun FullscreenBatteryDashboard(
    batteryState: BatteryState,
    activeAccent: Color,
    isNightMode: Boolean,
    pulseAlpha: Float,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        val isPortrait = maxHeight > maxWidth || maxWidth < 560.dp
        val ringSize = if (isPortrait) 160.dp else 200.dp
        val cardWidth = if (isPortrait) Modifier.fillMaxWidth() else Modifier.width(280.dp)

        @Composable
        fun BatteryRingContent() {
            Box(
                modifier = Modifier.size(ringSize),
                contentAlignment = Alignment.Center
            ) {
                val fraction = (batteryState.percentage / 100f).coerceIn(0f, 1f)
                val effectiveAlpha = if (batteryState.isCharging) pulseAlpha else 1.0f

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 16.dp.toPx()
                    val arcSize = size.width - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                    drawArc(
                        color = if (isNightMode) Color(0x22FF453A) else Color(0x2AFFFFFF),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    if (fraction > 0f) {
                        drawArc(
                            color = activeAccent.copy(alpha = effectiveAlpha),
                            startAngle = -90f,
                            sweepAngle = 360f * fraction,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (batteryState.isCharging) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Charging",
                            tint = activeAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = "${batteryState.percentage}%",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Black,
                            fontSize = 38.sp,
                            color = activeAccent
                        )
                    )
                    Text(
                        text = "Pixel 9",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    )
                }
            }
        }

        @Composable
        fun TelemetryCardsContent() {
            Column(
                modifier = cardWidth,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "POWER TELEMETRY",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.08.em,
                        color = TextTertiary
                    )
                )

                // Charging Speed Pill Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(StandbyCardBgSecondary)
                        .border(1.dp, StandbyBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "CHARGING STATE",
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextTertiary
                            )
                        )
                        Text(
                            text = batteryState.chargeSpeed,
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (batteryState.isCharging) activeAccent else TextPrimary
                            )
                        )
                        val statusDetail = when {
                            batteryState.percentage >= 100 -> "Fully Charged"
                            batteryState.isCharging && batteryState.timeToFullMinutes != null ->
                                "Estimated full in ${batteryState.timeToFullMinutes} minutes"
                            batteryState.isCharging -> "Docked and charging"
                            else -> "Running on battery power"
                        }
                        Text(
                            text = statusDetail,
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        )
                    }
                }

                // Battery Capacity Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(StandbyCardBgSecondary)
                        .border(1.dp, StandbyBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL CAPACITY",
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextTertiary
                                )
                            )
                            Text(
                                text = "${batteryState.capacityMah} mAh",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }

                        Text(
                            text = "HEALTHY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeAccent
                        )
                    }
                }
            }
        }

        if (isPortrait) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                BatteryRingContent()
                TelemetryCardsContent()
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BatteryRingContent()
                TelemetryCardsContent()
            }
        }
    }
}
