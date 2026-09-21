package com.hoandesign.standby.ui.widgets.vibes

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.hoandesign.standby.ui.theme.AccentCyan
import com.hoandesign.standby.ui.theme.AccentGreen
import com.hoandesign.standby.ui.theme.AccentOrange
import com.hoandesign.standby.ui.theme.NightRed
import com.hoandesign.standby.ui.theme.NightRedDim
import com.hoandesign.standby.ui.theme.OledBlack
import com.hoandesign.standby.ui.theme.StandbyBorder
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import com.hoandesign.standby.ui.theme.StandbyCardBgSecondary
import com.hoandesign.standby.ui.theme.StandbyTheme
import com.hoandesign.standby.ui.theme.TextPrimary
import com.hoandesign.standby.ui.theme.TextSecondary
import com.hoandesign.standby.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Available bedside ambient relaxation soundscapes using Google Material Design Icons.
 */
enum class Soundscape(val title: String, val icon: ImageVector) {
    RAIN("Rain", Icons.Rounded.WaterDrop),
    CAMPFIRE("Campfire", Icons.Rounded.LocalFireDepartment),
    WIND("Wind", Icons.Rounded.Air)
}

/**
 * Bedside relaxation soundscape widget with animated soundwave visualizer
 * and sleep timer selector.
 *
 * Features:
 * - Ambient soundscapes: Gentle Rain, Warm Campfire, Night Wind.
 * - Multi-bar animated soundwave visualizer that bounces dynamically when playing.
 * - Sleep timer presets: Off, 15m, 30m, 60m with live countdown ticking.
 * - Play/Pause audio control toggle.
 * - Red Night Mode adaptation for sleep preservation.
 *
 * @param modifier Root modifier.
 * @param initialSoundscape Default soundscape (defaults to Gentle Rain).
 */
@Composable
fun VibesWidget(
    modifier: Modifier = Modifier,
    initialSoundscape: Soundscape = Soundscape.RAIN
) {
    var selectedSoundscape by remember { mutableStateOf(initialSoundscape) }
    var isPlaying by remember { mutableStateOf(true) }
    var sleepTimerMinutes by remember { mutableIntStateOf(30) }
    var remainingSeconds by remember { mutableIntStateOf(30 * 60) }

    val isNightMode = StandbyTheme.isNightMode
    val activeAccent = when {
        isNightMode -> NightRed
        selectedSoundscape == Soundscape.RAIN -> AccentCyan
        selectedSoundscape == Soundscape.CAMPFIRE -> AccentOrange
        else -> AccentGreen
    }

    // Sleep timer countdown ticker
    LaunchedEffect(isPlaying, sleepTimerMinutes) {
        if (sleepTimerMinutes > 0) {
            remainingSeconds = sleepTimerMinutes * 60
            while (isActive && isPlaying && remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
            }
            if (remainingSeconds <= 0 && isPlaying) {
                isPlaying = false // Auto-turn off when sleep timer elapses
            }
        }
    }

    // Animated soundwave bars
    val infiniteTransition = rememberInfiniteTransition(label = "VibesWave")
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(tween(650, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w1"
    )
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 34f,
        animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w2"
    )
    val wave3 by infiniteTransition.animateFloat(
        initialValue = 5f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w3"
    )
    val wave4 by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 36f,
        animationSpec = infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w4"
    )
    val wave5 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 26f,
        animationSpec = infiniteRepeatable(tween(720, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w5"
    )
    val wave6 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 32f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w6"
    )
    val wave7 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(850, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w7"
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
            // Header: Category Label & Soundscape Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VIBES",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.08.em,
                        color = if (isNightMode) NightRed else TextTertiary
                    )
                )

                // Soundscape Choice Pills (Rain, Campfire, Wind)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Soundscape.entries.forEach { scape ->
                        val isSelected = scape == selectedSoundscape
                        val pillBg = if (isNightMode) {
                            if (isSelected) Color(0x33FF453A) else StandbyCardBgSecondary
                        } else {
                            if (isSelected) activeAccent.copy(alpha = 0.18f) else StandbyCardBgSecondary
                        }
                        val pillBorder = if (isNightMode) {
                            if (isSelected) Color(0x66FF453A) else StandbyBorder
                        } else {
                            if (isSelected) activeAccent.copy(alpha = 0.45f) else StandbyBorder
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(pillBg)
                                .border(1.dp, pillBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedSoundscape = scape }
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = scape.icon,
                                contentDescription = scape.title,
                                tint = if (isSelected) (if (isNightMode) NightRed else activeAccent) else TextTertiary,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = scape.title,
                                style = TextStyle(
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    color = if (isSelected) (if (isNightMode) NightRed else TextPrimary) else TextTertiary
                                )
                            )
                        }
                    }
                }
            }

            // Center Section: Soundwave Bar Visualizer & Play/Pause Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Soundwave Bars
                val barHeights = if (isPlaying) {
                    listOf(wave1, wave2, wave3, wave4, wave5, wave6, wave7)
                } else {
                    listOf(5f, 5f, 5f, 5f, 5f, 5f, 5f)
                }

                Row(
                    modifier = Modifier.height(44.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    barHeights.forEach { heightVal ->
                        Box(
                            modifier = Modifier
                                .width(4.5.dp)
                                .height(heightVal.dp)
                                .clip(RoundedCornerShape(2.5.dp))
                                .background(activeAccent)
                        )
                    }
                }

                // Play / Pause Circle Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isNightMode) NightRedDim else StandbyCardBgSecondary)
                        .border(
                            1.5.dp,
                            if (isNightMode) NightRed else activeAccent.copy(alpha = 0.7f),
                            CircleShape
                        )
                        .clickable { isPlaying = !isPlaying },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = if (isNightMode) NightRed else TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Bottom Section: Sleep Timer Selector (Off, 15m, 30m, 60m)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (sleepTimerMinutes > 0 && isPlaying) {
                        val m = remainingSeconds / 60
                        val s = remainingSeconds % 60
                        "Off in ${m}:${String.format("%02d", s)}"
                    } else {
                        "SLEEP TIMER"
                    },
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = if (isNightMode) Color(0xAAFF453A) else TextSecondary
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0 to "Off", 15 to "15m", 30 to "30m", 60 to "60m").forEach { (minutes, label) ->
                        val isTimerSelected = sleepTimerMinutes == minutes
                        val timerBg = if (isNightMode) {
                            if (isTimerSelected) Color(0x33FF453A) else StandbyCardBgSecondary
                        } else {
                            if (isTimerSelected) activeAccent.copy(alpha = 0.18f) else StandbyCardBgSecondary
                        }
                        val timerBorder = if (isNightMode) {
                            if (isTimerSelected) Color(0x66FF453A) else StandbyBorder
                        } else {
                            if (isTimerSelected) activeAccent.copy(alpha = 0.45f) else StandbyBorder
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(timerBg)
                                .border(1.dp, timerBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    sleepTimerMinutes = minutes
                                    remainingSeconds = minutes * 60
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = TextStyle(
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    color = if (isTimerSelected) (if (isNightMode) NightRed else TextPrimary) else TextTertiary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
