package com.hoandesign.standby.ui.widgets.timer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.hoandesign.standby.util.SystemIntents
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
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
import java.util.Locale

/**
 * Preset data class representing a standard focus / Pomodoro timer duration.
 */
data class TimerPreset(
    val label: String,
    val durationSeconds: Int
)

/**
 * Standard quick timer presets aligned with desk productivity.
 */
val DefaultTimerPresets = listOf(
    TimerPreset("5 min", 5 * 60),
    TimerPreset("15 min", 15 * 60),
    TimerPreset("25 min (Pomodoro)", 25 * 60),
    TimerPreset("45 min", 45 * 60)
)

/**
 * Interactive Desk & Pomodoro Focus Timer Widget.
 *
 * Features:
 * - Circular animated progress ring with remaining time in `MM:SS`.
 * - Quick preset chips: `5 min`, `15 min`, `25 min (Pomodoro)`, `45 min`.
 * - Start / Pause toggle and Reset controls with tactile haptic feedback.
 * - Dynamic color tinting respecting StandBy theme and seamless Red Night Mode adaptation.
 *
 * @param modifier Root modifier.
 * @param presets List of selectable presets; defaults to [DefaultTimerPresets].
 * @param initialPresetIndex Index of preset selected by default (default is 25 min Pomodoro).
 * @param accentColor Custom accent color override; defaults to [StandbyTheme.accentColor].
 */
@Composable
fun DeskTimerWidget(
    modifier: Modifier = Modifier,
    presets: List<TimerPreset> = DefaultTimerPresets,
    initialPresetIndex: Int = 2,
    accentColor: Color = StandbyTheme.accentColor
) {
    val isNightMode = StandbyTheme.isNightMode
    val activeAccent = if (isNightMode) NightRed else accentColor
    val haptic = LocalHapticFeedback.current

    val safeIndex = initialPresetIndex.coerceIn(0, presets.lastIndex)
    var selectedPreset by remember(presets) { mutableStateOf(presets[safeIndex]) }
    var totalSeconds by remember(selectedPreset) { mutableIntStateOf(selectedPreset.durationSeconds) }
    var remainingSeconds by remember(selectedPreset) { mutableIntStateOf(selectedPreset.durationSeconds) }
    var isRunning by remember { mutableStateOf(false) }

    // Countdown ticking loop
    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
            if (remainingSeconds == 0) {
                isRunning = false
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                } catch (_: Throwable) {
                }
            }
        }
    }

    // Animated progress along circular arc
    val progress = if (totalSeconds > 0) {
        (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "DeskTimerProgress"
    )

    // Formatted time MM:SS
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formattedTime = remember(remainingSeconds) {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    val context = LocalContext.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        val availableHeight = maxHeight
        val ringSize = (availableHeight * 0.46f).coerceIn(84.dp, 150.dp)
        val timeFontSize = (ringSize.value * 0.23f).coerceIn(20f, 32f).sp
        val statusFontSize = (ringSize.value * 0.08f).coerceIn(9f, 12f).sp
        val strokeWidth = (ringSize.value * 0.055f).coerceIn(5f, 9f).dp

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Preset Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                presets.forEach { preset ->
                    val isSelected = preset == selectedPreset
                    val chipBg = when {
                        isSelected && isNightMode -> NightRedDim
                        isSelected -> activeAccent.copy(alpha = 0.22f)
                        else -> StandbyCardBgSecondary
                    }
                    val chipBorder = when {
                        isSelected -> activeAccent
                        isNightMode -> Color(0x33FF453A)
                        else -> StandbyBorder
                    }
                    val chipTextColor = when {
                        isSelected && isNightMode -> NightRed
                        isSelected -> activeAccent
                        isNightMode -> Color(0xAAFF453A)
                        else -> TextSecondary
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(chipBg)
                            .border(1.dp, chipBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                selectedPreset = preset
                                totalSeconds = preset.durationSeconds
                                remainingSeconds = preset.durationSeconds
                                isRunning = false
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = preset.label,
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                color = chipTextColor
                            )
                        )
                    }
                }
            }

            // Circular Animated Progress Ring
            Box(
                modifier = Modifier
                    .size(ringSize)
                    .clip(CircleShape)
                    .clickable { SystemIntents.launchTimer(context) },
                contentAlignment = Alignment.Center
            ) {
                val trackColor = if (isNightMode) NightRedDim else Color(0xFF232326)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokePx = strokeWidth.toPx()

                    // Background Track Ring
                    drawCircle(
                        color = trackColor,
                        style = Stroke(width = strokePx)
                    )

                    // Active Countdown Arc
                    drawArc(
                        color = activeAccent,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = formattedTime,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = timeFontSize,
                            letterSpacing = (-0.02).em,
                            color = if (isNightMode) NightRed else TextPrimary
                        )
                    )

                    val statusLabel = when {
                        remainingSeconds == 0 -> "COMPLETED"
                        isRunning -> "FOCUS"
                        else -> "PAUSED"
                    }

                    Text(
                        text = statusLabel,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = statusFontSize,
                            letterSpacing = 0.08.em,
                            color = when {
                                remainingSeconds == 0 -> activeAccent
                                isNightMode -> Color(0x88FF453A)
                                else -> TextTertiary
                            }
                        )
                    )
                }
            }

            // Controls Row: Start / Pause Toggle + Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isNightMode) Color(0x22FF453A) else StandbyCardBgSecondary)
                        .border(1.dp, if (isNightMode) Color(0x44FF453A) else StandbyBorder, RoundedCornerShape(16.dp))
                        .clickable {
                            try {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            } catch (_: Throwable) {
                            }
                            isRunning = false
                            remainingSeconds = totalSeconds
                        }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reset",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (isNightMode) Color(0xAAFF453A) else TextSecondary
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Start / Pause Button
                val startBg = if (isRunning) {
                    if (isNightMode) Color(0x33FF453A) else activeAccent.copy(alpha = 0.25f)
                } else {
                    activeAccent
                }
                val startBorder = if (isRunning) activeAccent else Color.Transparent
                val startTextColor = if (isRunning) {
                    activeAccent
                } else {
                    Color.Black
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(startBg)
                        .border(1.dp, startBorder, RoundedCornerShape(16.dp))
                        .clickable {
                            try {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            } catch (_: Throwable) {
                            }
                            if (remainingSeconds == 0) {
                                remainingSeconds = totalSeconds
                            }
                            isRunning = !isRunning
                        }
                        .padding(horizontal = 20.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isRunning) "Pause" else "Start",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = startTextColor
                        )
                    )
                }
            }
        }
    }
}
