package com.hoandesign.standby.ui.widgets.battery

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.hoandesign.standby.ui.theme.OledBlack
import com.hoandesign.standby.ui.theme.StandbyBorder
import com.hoandesign.standby.ui.theme.StandbyCardBgSecondary
import com.hoandesign.standby.ui.theme.StandbyTheme
import com.hoandesign.standby.ui.theme.TextSecondary
import com.hoandesign.standby.ui.theme.TextTertiary

/**
 * Battery & Fast Charging Widget matching reference screenshot #4.
 *
 * Features:
 * - Header status pill ("⚡ Fast Charging", "⚡ Wireless Qi", or "On Battery").
 * - Prominent battery percentage numeral in [AccentGreen] (or [NightRed] in night mode).
 * - Smooth horizontal progress bar meter with breathing pulse animation while charging.
 * - Sub-info displaying battery capacity (e.g. `4895 mAh`) and time to full (e.g. `Full in 22 min`).
 * - OLED-friendly styling and red night mode compatibility.
 *
 * @param modifier Root modifier.
 * @param initialBatteryState Optional fixed state for previews and tests.
 */
@Composable
fun BatteryWidget(
    modifier: Modifier = Modifier,
    initialBatteryState: BatteryState? = null
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
        initialValue = 0.72f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ChargePulseAlpha"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(14.dp)
    ) {
        val availableHeight = maxHeight

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Category Label & Charging Speed Pill
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
                        fontSize = 10.sp,
                        letterSpacing = 0.08.em,
                        color = if (isNightMode) NightRed else TextTertiary
                    )
                )

                // Charging Speed Pill
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

            // Large Battery Percentage Numeral
            Text(
                text = "${batteryState.percentage}%",
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Black,
                    fontSize = if (availableHeight < 140.dp) 48.sp else 62.sp,
                    letterSpacing = (-0.03).em,
                    color = activeAccent
                ),
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // Horizontal Progress Bar Meter with Charging Pulse
            val barFraction = (batteryState.percentage / 100f).coerceIn(0f, 1f)
            val effectiveAlpha = if (batteryState.isCharging) pulseAlpha else 1.0f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (isNightMode) Color(0x22FF453A) else StandbyCardBgSecondary)
                    .border(
                        1.dp,
                        if (isNightMode) NightRedDim else StandbyBorder,
                        RoundedCornerShape(7.dp)
                    )
            ) {
                if (barFraction > 0f) {
                    val barBrush = if (batteryState.isCharging) {
                        Brush.horizontalGradient(
                            listOf(
                                activeAccent.copy(alpha = 0.85f * effectiveAlpha),
                                activeAccent.copy(alpha = effectiveAlpha)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(listOf(activeAccent, activeAccent))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(barFraction)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(barBrush)
                    )
                }
            }

            // Sub-info Row: Capacity (4895 mAh) & Time to Full (Full in 22 min)
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
