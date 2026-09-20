package com.hoandesign.standby.ui.widgets.system

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.hoandesign.standby.data.SystemTelemetryHelper
import com.hoandesign.standby.ui.theme.AccentBlue
import com.hoandesign.standby.ui.theme.AccentCyan
import com.hoandesign.standby.ui.theme.AccentGreen
import com.hoandesign.standby.ui.theme.AccentPurple
import com.hoandesign.standby.ui.theme.NightRed
import com.hoandesign.standby.ui.theme.NightRedDim
import com.hoandesign.standby.ui.theme.OledBlack
import com.hoandesign.standby.ui.theme.StandbyBorder
import com.hoandesign.standby.ui.theme.StandbyCardBgSecondary
import com.hoandesign.standby.ui.theme.StandbyTheme
import com.hoandesign.standby.ui.theme.TextPrimary
import com.hoandesign.standby.ui.theme.TextSecondary
import com.hoandesign.standby.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * System Bento Widget presenting live Android hardware metrics.
 *
 * Real Data Features:
 * - Live Connectivity Status: Real Wi-Fi, Bluetooth, and Cellular detection.
 * - Live Storage: Real disk metrics from StatFs (used vs available GB & %).
 * - Live RAM: Real memory usage from ActivityManager.MemoryInfo.
 * - Live Brightness: Real-time window brightness slider control.
 * - OLED pitch-black contrast and Red Night Mode compatibility.
 */
@Composable
fun SystemBentoWidget(
    modifier: Modifier = Modifier,
    initialBrightness: Float = 0.75f,
    onBrightnessChange: ((Float) -> Unit)? = null
) {
    val context = LocalContext.current
    var storage by remember { mutableStateOf(SystemTelemetryHelper.getStorageMetrics()) }
    var memory by remember { mutableStateOf(SystemTelemetryHelper.getMemoryMetrics(context)) }
    var connectivity by remember { mutableStateOf(SystemTelemetryHelper.getNetworkConnectivity(context)) }
    var brightness by remember { mutableFloatStateOf(initialBrightness) }

    // Periodically refresh telemetry every 10 seconds
    LaunchedEffect(Unit) {
        while (isActive) {
            storage = SystemTelemetryHelper.getStorageMetrics()
            memory = SystemTelemetryHelper.getMemoryMetrics(context)
            connectivity = SystemTelemetryHelper.getNetworkConnectivity(context)
            delay(10_000L)
        }
    }

    val isNightMode = StandbyTheme.isNightMode
    val activeAccent = if (isNightMode) NightRed else AccentPurple
    val ringTrackColor = if (isNightMode) Color(0x22FF453A) else StandbyCardBgSecondary

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
            // Header Row: Live Connectivity Pills (Wi-Fi, Bluetooth, Cell)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wi-Fi Pill
                ConnectivityPill(
                    icon = "📶",
                    label = if (connectivity.isWifiActive) "Wi-Fi" else "Off",
                    isActive = connectivity.isWifiActive,
                    accentColor = if (isNightMode) NightRed else AccentCyan,
                    isNightMode = isNightMode,
                    onClick = {
                        connectivity = SystemTelemetryHelper.getNetworkConnectivity(context)
                    },
                    modifier = Modifier.weight(1f)
                )

                // Bluetooth Pill
                ConnectivityPill(
                    icon = "ᛒ",
                    label = if (connectivity.isBluetoothActive) "BT" else "Off",
                    isActive = connectivity.isBluetoothActive,
                    accentColor = if (isNightMode) NightRed else AccentBlue,
                    isNightMode = isNightMode,
                    onClick = {
                        connectivity = SystemTelemetryHelper.getNetworkConnectivity(context)
                    },
                    modifier = Modifier.weight(1f)
                )

                // Cell Pill
                ConnectivityPill(
                    icon = if (connectivity.isCellularActive) "5G" else "Cell",
                    label = if (connectivity.isCellularActive) "Active" else "Standby",
                    isActive = connectivity.isCellularActive || connectivity.isConnected,
                    accentColor = if (isNightMode) NightRed else AccentGreen,
                    isNightMode = isNightMode,
                    onClick = {
                        connectivity = SystemTelemetryHelper.getNetworkConnectivity(context)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Center: Live Disk Storage Progress Ring & RAM Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Circular Ring: Real Storage Usage Fraction
                val ringSize = if (availableHeight < 150.dp) 52.dp else 64.dp
                val usedRatio = (storage.usedPercent / 100f).coerceIn(0f, 1f)

                Box(
                    modifier = Modifier.size(ringSize),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 7.dp.toPx()
                        val padding = strokeWidth / 2f
                        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                        val arcTopLeft = Offset(padding, padding)

                        // Background track
                        drawArc(
                            color = ringTrackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = arcTopLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )

                        // Active progress arc reflecting actual storage used
                        drawArc(
                            color = activeAccent,
                            startAngle = -90f,
                            sweepAngle = 360f * usedRatio,
                            useCenter = false,
                            topLeft = arcTopLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Text(
                        text = "${storage.remainPercent}%",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (availableHeight < 150.dp) 11.sp else 13.sp,
                            color = if (isNightMode) NightRed else TextPrimary
                        )
                    )
                }

                // Live Storage & RAM Details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = storage.availableGbFormatted,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (availableHeight < 150.dp) 16.sp else 18.sp,
                            color = if (isNightMode) NightRed else TextPrimary
                        )
                    )

                    Text(
                        text = "Free · of ${storage.totalGbFormatted}",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 11.sp,
                            color = if (isNightMode) Color(0x99FF453A) else TextSecondary
                        )
                    )

                    // RAM Gauge Mini Bar with real memory metrics
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isNightMode) Color(0x33FF453A) else StandbyCardBgSecondary)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(memory.usedRatio)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isNightMode) NightRed else AccentCyan)
                            )
                        }

                        Text(
                            text = "RAM ${memory.usedGbFormatted} / ${memory.totalGbFormatted}",
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontSize = 10.sp,
                                color = if (isNightMode) Color(0x77FF453A) else TextTertiary
                            )
                        )
                    }
                }
            }

            // Bottom: Brightness Quick-Slider with live window brightness adjustment
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isNightMode) Color(0x22FF453A) else StandbyCardBgSecondary)
                    .border(
                        1.dp,
                        if (isNightMode) NightRedDim else StandbyBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🔆",
                    fontSize = 12.sp
                )

                Slider(
                    value = brightness,
                    onValueChange = { newVal ->
                        brightness = newVal
                        onBrightnessChange?.invoke(newVal)
                        (context as? Activity)?.window?.let { win ->
                            val lp = win.attributes
                            lp.screenBrightness = newVal.coerceIn(0.01f, 1.0f)
                            win.attributes = lp
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = if (isNightMode) NightRed else TextPrimary,
                        activeTrackColor = if (isNightMode) NightRed else activeAccent,
                        inactiveTrackColor = if (isNightMode) Color(0x44FF453A) else StandbyBorder
                    )
                )

                Text(
                    text = "☀️",
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * Reusable connectivity status badge pill (Wi-Fi, Bluetooth, Cell).
 */
@Composable
private fun ConnectivityPill(
    icon: String,
    label: String,
    isActive: Boolean,
    accentColor: Color,
    isNightMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pillBg = if (isNightMode) {
        if (isActive) Color(0x33FF453A) else StandbyCardBgSecondary
    } else {
        if (isActive) accentColor.copy(alpha = 0.16f) else StandbyCardBgSecondary
    }

    val pillBorder = if (isNightMode) {
        if (isActive) Color(0x66FF453A) else StandbyBorder
    } else {
        if (isActive) accentColor.copy(alpha = 0.38f) else StandbyBorder
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(pillBg)
            .border(1.dp, pillBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = icon,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) (if (isNightMode) NightRed else accentColor) else TextTertiary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = if (isActive) (if (isNightMode) NightRed else TextPrimary) else TextTertiary
            )
        )
    }
}
